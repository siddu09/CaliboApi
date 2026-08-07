package services;

import api.StageApiClient;
import api.StageSetupApiClient;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import validators.StageValidator;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.LinkedHashMap;

/** Complete business activity for creating and verifying a deployment stage. */
public final class StageService {

    private final StageApiClient apiClient = new StageApiClient();
    private final StageSetupApiClient setupApiClient = new StageSetupApiClient();
    private final StageValidator validator = new StageValidator();

    public String createAndVerifyDevStage() {
        JSONObject setup = StageRequestBuilder.loadDevStageTestData();
        Map<String, String> configurationIds = validateRequiredConfigurations(setup);

        Map<String, Object> user = successful(setupApiClient.getCurrentUser(), 200, "current user")
                .jsonPath().getMap("");
        String ownerRoleId = findValue(successful(setupApiClient.getProjectRoles(), 200, "project roles")
                .jsonPath().getList(""), "key", setup.get("ownerRoleKey").toString(), "id", "owner role");

        Response portfolioResponse = successful(
                setupApiClient.getPortfolio(setup.get("portfolioName").toString()), 200, "portfolio");
        String portfolioId = findValue(portfolioResponse.jsonPath().getList("portfolios"),
                "title", setup.get("portfolioName").toString(), "id", "portfolio");

        List<Map<String, Object>> fieldValues = successful(
                setupApiClient.getFieldValues(), 200, "field values").jsonPath().getList("");
        String businessGroupId = findNestedFieldValue(fieldValues, "businessGroup",
                setup.get("businessGroupName").toString());
        String customerSegmentId = findNestedFieldValue(fieldValues, "customer",
                setup.get("customerSegmentName").toString());

        String suffix = Long.toString(ThreadLocalRandom.current().nextLong(36L * 36L * 36L * 36L), 36);
        String projectName = setup.get("projectTitlePrefix") + suffix;
        String workstreamName = setup.get("workstreamTitlePrefix") + suffix;

        JSONObject projectRequest = StageSetupRequestBuilder.projectRequest(setup, user,
                ownerRoleId, portfolioId, businessGroupId, customerSegmentId, projectName);
        Response projectResponse = successful(setupApiClient.createProject(projectRequest), 201, "create project");
        String projectId = requiredPath(projectResponse, "id", "created project ID");
        successful(setupApiClient.updateProject(projectId, projectRequest), 200, "update project");

        JSONObject workstreamRequest = StageSetupRequestBuilder.workstreamRequest(
                setup, user, ownerRoleId, projectId, workstreamName);
        Response workstreamResponse = successful(
                setupApiClient.createWorkstream(workstreamRequest), 201, "create workstream");
        String workstreamId = requiredPath(workstreamResponse, "id", "created workstream ID");
        String releaseId = requiredPath(workstreamResponse, "releaseId", "default release ID");

        verifyWorkstreamAndRelease(projectId, workstreamId, releaseId);

        Map<String, String> techStackIds = createRepositories(setup, projectId, projectName,
                workstreamId, workstreamName, releaseId, portfolioId, suffix);

        JSONObject testData = StageRequestBuilder.createRuntimeStageData(setup, projectId,
                projectName, workstreamId, workstreamName, releaseId, portfolioId);

        String stageName = testData.get("stageName").toString();

        Response templateResponse = apiClient.getStages(projectId, workstreamId, releaseId);
        validator.validateStagesApi(templateResponse);

        List<Map<String, Object>> stages = templateResponse.jsonPath().getList("stages");
        JSONObject request;
        if (stages.isEmpty()) {
            JSONObject configurationNames = (JSONObject) setup.get("configurationNames");
            request = StageRequestBuilder.createInitialKubernetesStageRequest(testData,
                    configurationIds, configurationNames.get("KUBERNETES").toString());
        } else {
            JSONObject existingStage = findStage(templateResponse, stageName);
            request = StageRequestBuilder.createKubernetesStageRequest(existingStage, testData);
        }

        Response createResponse = apiClient.createStage(request);
        validator.validateStageCreated(createResponse);

        Response getResponse = apiClient.getStages(projectId, workstreamId, releaseId);
        validator.validateStageExists(getResponse, stageName, "KUBERNETES");

        return runDeploymentPipeline(setup, testData, getResponse, techStackIds,
                configurationIds.get("KUBERNETES"), suffix);
    }

    private Map<String, String> validateRequiredConfigurations(JSONObject setup) {
        Map<String, String> configurationIds = new LinkedHashMap<>();
        JSONObject names = (JSONObject) setup.get("configurationNames");
        for (Object entryValue : names.entrySet()) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) entryValue;
            String code = entry.getKey().toString();
            String name = entry.getValue().toString();
            List<Map<String, Object>> configurations = successful(
                    setupApiClient.getConfiguration(code), 200, code + " configurations")
                    .jsonPath().getList("");
            configurationIds.put(code,
                    findValue(configurations, "name", name, "id", code + " configuration"));
        }
        return configurationIds;
    }

    private void verifyWorkstreamAndRelease(String projectId, String workstreamId, String releaseId) {
        List<Map<String, Object>> workstreams = successful(
                setupApiClient.getWorkstreams(projectId), 200, "verify workstream").jsonPath().getList("");
        findValue(workstreams, "id", workstreamId, "id", "created workstream");

        List<Map<String, Object>> releases = successful(
                setupApiClient.getReleases(projectId), 200, "verify release").jsonPath().getList("");
        findValue(releases, "id", releaseId, "id", "default release");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> createRepositories(JSONObject setup, String projectId, String projectName,
                                                   String workstreamId, String workstreamName,
                                                   String releaseId, String portfolioId, String suffix) {
        Object techStackRoot = successful(setupApiClient.getTechStacks(projectId), 200,
                "retrieve tech stacks").jsonPath().get("");
        List<Map<String, Object>> groups = successful(
                setupApiClient.getRepositoryGroups(projectId), 200, "retrieve GitLab groups")
                .jsonPath().getList("");
        String groupIdText = findValue(groups, "name", setup.get("gitlabGroupName").toString(),
                "id", "GitLab group");

        Map<String, String> ids = new LinkedHashMap<>();
        JSONArray techInputs = (JSONArray) setup.get("techStacks");
        int index = 0;
        for (Object inputValue : techInputs) {
            JSONObject input = (JSONObject) inputValue;
            String techName = input.get("name").toString();
            Map<String, Object> techStack = findObjectRecursively(techStackRoot, "name", techName);
            if (techStack == null || techStack.get("id") == null) {
                throw new IllegalStateException("Unable to find tech stack " + techName);
            }
            String techId = techStack.get("id").toString();
            ids.put(techName, techId);
            JSONObject repositoryRequest = StageSetupRequestBuilder.repositoryRequest(setup,
                    projectId, projectName, workstreamId, workstreamName, releaseId, portfolioId,
                    techId, Long.valueOf(groupIdText), "devstage" + suffix + index++);
            successful(setupApiClient.createRepository(repositoryRequest), 201,
                    "create repository for " + techName);
            waitForRepository(projectId, workstreamId);
        }
        return ids;
    }

    private void waitForRepository(String projectId, String workstreamId) {
        for (int attempt = 1; attempt <= 24; attempt++) {
            Response statusResponse = successful(
                    setupApiClient.getRepositoryCreationStatus(projectId, workstreamId),
                    200, "repository creation status");
            String status = statusResponse.asString().replace("\"", "").trim();
            if ("ACTIVE".equalsIgnoreCase(status)) return;
            if (!"IN_PROGRESS".equalsIgnoreCase(status)) {
                throw new IllegalStateException("Repository creation failed with status: " + status);
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for repository creation", exception);
            }
        }
        throw new IllegalStateException("Repository did not become ACTIVE within 48 seconds");
    }

    @SuppressWarnings("unchecked")
    private String runDeploymentPipeline(JSONObject setup, JSONObject testData, Response stageResponse,
                                         Map<String, String> techStackIds,
                                         String kubernetesSettingId, String suffix) {
        JSONObject stage = findStage(stageResponse, testData.get("stageName").toString());
        String stageDetailsId = String.valueOf(stage.get("stageDetailsId"));
        JSONArray pipelineData = (JSONArray) stage.get("pipelineData");
        JSONArray techInputs = (JSONArray) setup.get("techStacks");
        JSONArray technologyRequests = new JSONArray();
        List<String> pipelineIds = new java.util.ArrayList<>();

        for (Object inputValue : techInputs) {
            JSONObject input = (JSONObject) inputValue;
            String techName = input.get("name").toString();
            String techId = techStackIds.get(techName);
            JSONObject pipeline = findPipeline(pipelineData, techId);
            pipelineIds.add(String.valueOf(pipeline.get("pipelineDetailsId")));
            technologyRequests.add(StageRequestBuilder.technologyRequest(pipeline, input, setup,
                    stageDetailsId, testData.get("portfolioId").toString(),
                    testData.get("portfolioName").toString(), kubernetesSettingId,
                    ((JSONObject) setup.get("configurationNames")).get("KUBERNETES").toString(), suffix));
        }

        successful(apiClient.configureTechnologies(technologyRequests,
                testData.get("workstreamId").toString(), testData.get("releaseId").toString()),
                200, "configure deployment technologies");

        for (String pipelineId : pipelineIds) {
            successful(apiClient.runCi(pipelineId), 200, "run CI pipeline " + pipelineId);
        }
        List<String> successfulCi = waitForPipelineStatus(setup, pipelineIds, "ciPipelineStatus",
                List.of("SUCCESS"));

        JSONObject deployRequest = StageRequestBuilder.deployRequest(testData, stageDetailsId,
                successfulCi, setup.get("imageTag").toString());
        successful(apiClient.deploy(deployRequest), 200, "deploy pipelines");
        waitForPipelineStatus(setup, successfulCi, "pipelineStatus", List.of("DEPLOYED", "SUCCESS"));

        JSONObject logsRequest = new JSONObject();
        JSONArray logIds = new JSONArray();
        logIds.addAll(successfulCi);
        logsRequest.put("pipelineIds", logIds);
        Response logsResponse = successful(apiClient.getPipelineStages(logsRequest), 200,
                "retrieve CI/CD pipeline stages");
        for (String pipelineId : successfulCi) {
            if (logsResponse.jsonPath().get(pipelineId) == null) {
                throw new IllegalStateException("Pipeline logs missing for " + pipelineId);
            }
        }
        return waitForLiveUrl(testData);
    }

    private String waitForLiveUrl(JSONObject testData) {
        for (int attempt = 0; attempt < 12; attempt++) {
            Response response = successful(apiClient.getStages(testData.get("projectId").toString(),
                    testData.get("workstreamId").toString(), testData.get("releaseId").toString()),
                    200, "retrieve deployed stage");
            String url = findLiveUrl(findStage(response, testData.get("stageName").toString()));
            if (url != null) return url;
            try { Thread.sleep(10_000); }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for live URL", e);
            }
        }
        throw new IllegalStateException("Deployment completed but no application URL was generated");
    }

    private String findLiveUrl(Object value) {
        if (value instanceof Map<?, ?> map) {
            for (String field : List.of("applicationUrl", "appServerUrl")) {
                Object url = map.get(field);
                if (url != null && url.toString().matches("https?://.+")) return url.toString();
            }
            for (Object nested : map.values()) {
                String url = findLiveUrl(nested);
                if (url != null) return url;
            }
        } else if (value instanceof List<?> list) {
            for (Object nested : list) {
                String url = findLiveUrl(nested);
                if (url != null) return url;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<String> waitForPipelineStatus(JSONObject setup, List<String> pipelineIds,
                                               String statusField, List<String> successStates) {
        List<String> pending = new java.util.ArrayList<>(pipelineIds);
        List<String> completed = new java.util.ArrayList<>();
        int maxAttempts = ((Number) setup.get("pipelineMaxAttempts")).intValue();
        int pollSeconds = ((Number) setup.get("pipelinePollSeconds")).intValue();
        for (int attempt = 1; attempt <= maxAttempts && !pending.isEmpty(); attempt++) {
            JSONObject body = new JSONObject();
            JSONArray ids = new JSONArray();
            ids.addAll(pending);
            body.put("pipelineIds", ids);
            Response response = successful(apiClient.getBuildStatus(body), 200, "pipeline build status");
            JSONArray statuses;
            try {
                statuses = (JSONArray) new JSONParser().parse(response.asString());
            } catch (ParseException exception) {
                throw new IllegalStateException("Invalid build-status response", exception);
            }
            for (Object statusValue : statuses) {
                JSONObject status = (JSONObject) statusValue;
                String id = String.valueOf(status.get("pipelineDetailsId"));
                String value = String.valueOf(status.get(statusField));
                if (successStates.stream().anyMatch(state -> state.equalsIgnoreCase(value))) {
                    pending.remove(id);
                    completed.add(id);
                } else if ("FAILED".equalsIgnoreCase(value) || "CREATION_FAILED".equalsIgnoreCase(value)) {
                    throw new IllegalStateException("Pipeline " + id + " failed with " + statusField + "=" + value);
                }
            }
            if (!pending.isEmpty()) {
                try { Thread.sleep(pollSeconds * 1000L); }
                catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while polling pipelines", exception);
                }
            }
        }
        if (!pending.isEmpty()) throw new IllegalStateException("Pipelines timed out: " + pending);
        return completed;
    }

    private JSONObject findPipeline(JSONArray pipelines, String techStackId) {
        for (Object value : pipelines) {
            JSONObject pipeline = (JSONObject) value;
            if (techStackId.equalsIgnoreCase(String.valueOf(pipeline.get("techStackId")))) return pipeline;
        }
        throw new IllegalStateException("No pipeline generated for tech stack " + techStackId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> findObjectRecursively(Object value, String field, String expected) {
        if (value instanceof Map<?, ?> map) {
            if (expected.equalsIgnoreCase(String.valueOf(map.get(field)))) {
                return (Map<String, Object>) map;
            }
            for (Object nested : map.values()) {
                Map<String, Object> found = findObjectRecursively(nested, field, expected);
                if (found != null) return found;
            }
        } else if (value instanceof List<?> list) {
            for (Object nested : list) {
                Map<String, Object> found = findObjectRecursively(nested, field, expected);
                if (found != null) return found;
            }
        }
        return null;
    }

    private Response successful(Response response, int expectedStatus, String activity) {
        if (response.statusCode() != expectedStatus) {
            throw new IllegalStateException(activity + " failed. Expected HTTP " + expectedStatus
                    + " but received " + response.statusCode() + ": " + response.asString());
        }
        return response;
    }

    private String requiredPath(Response response, String path, String description) {
        String value = response.jsonPath().getString(path);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Response is missing " + description + ": " + response.asString());
        }
        return value;
    }

    private String findValue(List<Map<String, Object>> values, String matchField,
                             String expected, String resultField, String description) {
        if (values != null) {
            for (Map<String, Object> value : values) {
                if (expected.equalsIgnoreCase(String.valueOf(value.get(matchField)))) {
                    Object result = value.get(resultField);
                    if (result != null) return result.toString();
                }
            }
        }
        throw new IllegalStateException("Unable to find " + description + " matching " + expected);
    }

    @SuppressWarnings("unchecked")
    private String findNestedFieldValue(List<Map<String, Object>> fields,
                                        String objectName, String objectValue) {
        if (fields != null) {
            for (Map<String, Object> field : fields) {
                if (objectName.equals(field.get("objectName"))) {
                    return findValue((List<Map<String, Object>>) field.get("values"),
                            "objectValue", objectValue, "id", objectName);
                }
            }
        }
        throw new IllegalStateException("Unable to find field-values group: " + objectName);
    }

    private JSONObject findStage(Response response, String stageName) {
        try {
            JSONObject root = (JSONObject) new JSONParser().parse(response.asString());
            JSONArray stages = (JSONArray) root.get("stages");

            for (Object value : stages) {
                JSONObject stage = (JSONObject) value;
                if (stageName.equalsIgnoreCase(String.valueOf(stage.get("stageName")))) {
                    return stage;
                }
            }

            throw new IllegalArgumentException(
                    "Stage template '" + stageName + "' was not returned by the stages API");
        } catch (ParseException exception) {
            throw new IllegalStateException("Unable to parse stages response as JSON", exception);
        }
    }
}
