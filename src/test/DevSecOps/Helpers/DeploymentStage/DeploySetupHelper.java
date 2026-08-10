package DevSecOps.Helpers.DeploymentStage;

import DevSecOps.Helpers.DeploymentStage.DeployStageRequestHelper;
import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static io.restassured.RestAssured.given;

public final class DeploySetupHelper {
    private final Map<String, Object> state;

    public DeploySetupHelper(Map<String, Object> state) { this.state = state; }

    public void loadTestData() {
        try (FileReader reader = new FileReader("test/DevSecOps/devtest/deploy-Kubernetes.json")) {
            state.put("setup", (JSONObject) new JSONParser().parse(reader));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load deploy-Kubernetes.json", exception);
        }
    }

    @SuppressWarnings("unchecked")
    public void createProjectAndWorkstream() {
        JSONObject setup = requiredJson("setup");
        JSONObject names = (JSONObject) setup.get("configurationNames");
        Map<String, String> configs = new LinkedHashMap<>();
        for (Object item : names.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) item;
            configs.put(entry.getKey().toString(), find(ok(configuration(entry.getKey().toString()), 200)
                    .jsonPath().getList(""), "name", entry.getValue(), "id"));
        }
        Map<String, Object> user = ok(get(ApiEndpoints.CURRENT_USER_INFO), 200).jsonPath().getMap("");
        String role = find(ok(get(ApiEndpoints.PROJECT_ROLES), 200).jsonPath().getList(""),
                "key", setup.get("ownerRoleKey"), "id");
        String portfolio = find(ok(portfolio(setup.get("portfolioName").toString()), 200)
                .jsonPath().getList("portfolios"), "title", setup.get("portfolioName"), "id");
        List<Map<String, Object>> fields = ok(fields(), 200).jsonPath().getList("");
        String business = nested(fields, "businessGroup", setup.get("businessGroupName"));
        String customer = nested(fields, "customer", setup.get("customerSegmentName"));
        String suffix = Long.toString(ThreadLocalRandom.current().nextLong(1679616), 36);
        String projectName = setup.get("projectTitlePrefix") + suffix;
        String workstreamName = setup.get("workstreamTitlePrefix") + suffix;
        JSONObject project = DeployRequestBodyHelper.project(setup, user, role, portfolio, business, customer, projectName);
        Response response = ok(post(ApiEndpoints.PROJECTS_V2, project), 201);
        String projectId = required(response, "id");
        ok(given().spec(RequestSpecProvider.get()).pathParam("projectId", projectId)
                .body(project.toJSONString()).patch(ApiEndpoints.PROJECT_BY_ID_V2), 200);
        response = ok(post(ApiEndpoints.WORKSTREAMS_V2,
                DeployRequestBodyHelper.workstream(setup, user, role, projectId, workstreamName)), 201);
        String workstreamId = required(response, "id");
        String releaseId = required(response, "releaseId");
        verifyContext(projectId, workstreamId, releaseId);
        Map<String, String> techIds = createRepositories(setup, projectId, projectName, workstreamId,
                workstreamName, releaseId, portfolio, suffix);
        state.put("testData", DeployStageRequestHelper.runtimeStageData(setup, projectId, projectName,
                workstreamId, workstreamName, releaseId, portfolio));
        state.put("configurationIds", configs); state.put("techStackIds", techIds);
        state.put("kubernetesSettingId", configs.get("KUBERNETES")); state.put("suffix", suffix);
    }

    private void verifyContext(String projectId, String workstreamId, String releaseId) {
        find(ok(given().spec(RequestSpecProvider.get()).pathParam("projectId", projectId)
                .get(ApiEndpoints.PROJECT_WORKSTREAMS), 200).jsonPath().getList(""), "id", workstreamId, "id");
        find(ok(given().spec(RequestSpecProvider.get()).queryParam("projectId", projectId)
                .get(ApiEndpoints.PROJECT_RELEASES), 200).jsonPath().getList(""), "id", releaseId, "id");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> createRepositories(JSONObject setup, String projectId, String projectName,
            String workstreamId, String workstreamName, String releaseId, String portfolio, String suffix) {
        Object techRoot = ok(given().spec(RequestSpecProvider.get()).queryParam("projectId", projectId)
                .queryParam("isSelected", true).get(ApiEndpoints.PROJECT_TECH_STACKS), 200).jsonPath().get("");
        String group = find(ok(given().spec(RequestSpecProvider.get()).queryParam("projectId", projectId)
                .get(ApiEndpoints.PROJECT_REPOSITORY_GROUPS), 200).jsonPath().getList(""),
                "name", setup.get("gitlabGroupName"), "id");
        Map<String, String> ids = new LinkedHashMap<>();
        for (Object value : (JSONArray) setup.get("techStacks")) {
            JSONObject input = (JSONObject) value;
            Map<String, Object> tech = findObject(techRoot, "name", input.get("name"));
            if (tech == null || tech.get("id") == null)
                throw new IllegalStateException("Technology stack not found: " + input.get("name"));
            String id = tech.get("id").toString(); ids.put(input.get("name").toString(), id);
            String repositoryName = randomRepositoryIdentifier();
            String repositoryUid = randomRepositoryIdentifier();
            ok(post(ApiEndpoints.PROJECT_REPOSITORIES, DeployRequestBodyHelper.repository(setup, projectId,
                    projectName, workstreamId, workstreamName, releaseId, portfolio, id,
                    Long.valueOf(group), repositoryName, repositoryUid)), 201);
            waitForRepository(projectId, workstreamId, id);
        }
        return ids;
    }

    private void waitForRepository(String projectId, String workstreamId, String techStackId) {
        JSONObject setup = requiredJson("setup");
        int maxAttempts = ((Number) setup.get("repositoryMaxAttempts")).intValue();
        int pollSeconds = ((Number) setup.get("repositoryPollSeconds")).intValue();
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            String status = ok(given().spec(RequestSpecProvider.get()).pathParam("projectId", projectId)
                    .pathParam("workstreamId", workstreamId).get(ApiEndpoints.REPOSITORY_CREATION_STATUS), 200)
                    .asString().replace("\"", "").trim();
            if ("ACTIVE".equalsIgnoreCase(status)) {
                verifyRepositoryActive(projectId, workstreamId, techStackId);
                return;
            }
            if (!"IN_PROGRESS".equalsIgnoreCase(status)) throw new IllegalStateException("Repository status " + status);
            if (attempt < maxAttempts) pause(pollSeconds);
        }
        throw new IllegalStateException("Repository creation timed out after "
                + (maxAttempts * pollSeconds) + " seconds");
    }

    private void verifyRepositoryActive(String projectId, String workstreamId, String techStackId) {
        List<Map<String, Object>> repositories = ok(given().spec(RequestSpecProvider.get())
                .queryParam("workstreamId", workstreamId).queryParam("limit", 200)
                .queryParam("projectId", projectId).get(ApiEndpoints.PROJECT_REPOSITORIES), 200)
                .jsonPath().getList("projectRepositories");
        for (Map<String, Object> repository : repositories)
            if (techStackId.equals(String.valueOf(repository.get("techStackId")))
                    && "ACTIVE".equalsIgnoreCase(String.valueOf(repository.get("status")))) return;
        throw new IllegalStateException("Active repository not found for tech stack " + techStackId);
    }

    private String randomRepositoryIdentifier() {
        return java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 9);
    }

    private Response configuration(String code) { return given().spec(RequestSpecProvider.get()).queryParam("configCode", code).get(ApiEndpoints.SETTINGS_BY_CONFIG_CODE); }
    private Response portfolio(String name) { return given().spec(RequestSpecProvider.get()).queryParam("limit", 10).queryParam("offset", 0).queryParam("sort", "asc").queryParam("search", name).get(ApiEndpoints.PORTFOLIOS); }
    private Response fields() { return given().spec(RequestSpecProvider.get()).queryParam("isUsageRequired", false).get(ApiEndpoints.DROPDOWN_VALUES); }
    private Response get(String endpoint) { return given().spec(RequestSpecProvider.get()).get(endpoint); }
    private Response post(String endpoint, JSONObject body) { return given().spec(RequestSpecProvider.get()).body(body.toJSONString()).post(endpoint); }
    private Response ok(Response response, int status) { if (response.statusCode() != status) throw new IllegalStateException("Expected HTTP " + status + ": " + response.asString()); return response; }
    private String required(Response response, String path) { String value = response.jsonPath().getString(path); if (value == null || value.isBlank()) throw new IllegalStateException("Missing " + path); return value; }
    private JSONObject requiredJson(String key) { Object value = state.get(key); if (value instanceof JSONObject json) return json; throw new IllegalStateException("Missing " + key); }
    private String find(List<Map<String, Object>> values, String key, Object expected, String output) { for (Map<String, Object> value : values) if (String.valueOf(expected).equalsIgnoreCase(String.valueOf(value.get(key)))) { Object result = value.get(output); if (result != null) return result.toString(); } throw new IllegalStateException("Missing " + expected); }
    @SuppressWarnings("unchecked") private String nested(List<Map<String, Object>> fields, String name, Object expected) { for (Map<String, Object> field : fields) if (name.equals(field.get("objectName")) || name.equals(field.get("code"))) { Object values = field.get("objectValues"); if (!(values instanceof List<?>)) values = field.get("values"); if (values instanceof List<?> list) { try { return find((List<Map<String, Object>>) (List<?>) list, "objectValue", expected, "id"); } catch (IllegalStateException ignored) { return find((List<Map<String, Object>>) (List<?>) list, "value", expected, "id"); } } } throw new IllegalStateException("Missing " + expected); }
    @SuppressWarnings("unchecked") private Map<String, Object> findObject(Object root, String key, Object expected) { if (root instanceof Map<?, ?> map) { if (String.valueOf(expected).equalsIgnoreCase(String.valueOf(map.get(key)))) return (Map<String, Object>) map; for (Object value : map.values()) { Map<String, Object> found = findObject(value, key, expected); if (found != null) return found; } } else if (root instanceof List<?> list) for (Object value : list) { Map<String, Object> found = findObject(value, key, expected); if (found != null) return found; } return null; }
    private void pause(int seconds) { try { Thread.sleep(seconds * 1000L); } catch (InterruptedException exception) { Thread.currentThread().interrupt(); throw new IllegalStateException("Setup wait interrupted", exception); } }
}
