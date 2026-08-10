package DevSecOps.Helpers.DeploymentStage;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public final class DeployStageHelper {
    private final Map<String, Object> state;

    public DeployStageHelper(Map<String, Object> state) { this.state = state; }

    @SuppressWarnings("unchecked")
    public void createOrUpdateStage() {
        JSONObject data = requiredJson("testData"), setup = requiredJson("setup");
        Map<String, String> ids = (Map<String, String>) required("configurationIds");
        String projectId = data.get("projectId").toString(), workstreamId = data.get("workstreamId").toString();
        String releaseId = data.get("releaseId").toString(), stageName = data.get("stageName").toString();
        Response response = stages(projectId, workstreamId, releaseId);
        List<Map<String, Object>> current = ok(response, 200).jsonPath().getList("stages");
        JSONObject existing = findStageOrNull(current, stageName);
        String clusterName = ((JSONObject) setup.get("configurationNames")).get("KUBERNETES").toString();
        JSONObject body;
        if (existing == null) {
            body = DeployStageRequestHelper.initialKubernetesStage(data, ids, clusterName);
        } else {
            if (!(existing.get("kubernetesClusters") instanceof org.json.simple.JSONArray clusters)
                    || clusters.isEmpty())
                existing.putAll(DeployStageRequestHelper.initialKubernetesStage(data, ids, clusterName));
            body = DeployStageRequestHelper.kubernetesStage(existing, data);
        }
        Response created = given().spec(RequestSpecProvider.get()).body(body.toJSONString()).post(ApiEndpoints.DEVOPS_STAGE);
        if (created.statusCode() != 200 && created.statusCode() != 201)
            throw new IllegalStateException("Stage creation failed: " + created.asString());
        JSONObject stage = findStage(ok(stages(projectId, workstreamId, releaseId), 200), stageName);
        state.put("stage", stage); state.put("stageDetailsId", stage.get("stageDetailsId"));
    }

    private Response stages(String projectId, String workstreamId, String releaseId) {
        return given().spec(RequestSpecProvider.get()).pathParam("projectId", projectId)
                .queryParam("workstreamId", workstreamId).queryParam("releaseId", releaseId)
                .get(ApiEndpoints.PROJECT_STAGES);
    }

    private JSONObject findStage(Response response, String name) {
        JSONObject stage = findStageOrNull(response.jsonPath().getList("stages"), name);
        if (stage != null) return stage;
        throw new IllegalStateException("Stage not found: " + name);
    }

    private JSONObject findStageOrNull(List<Map<String, Object>> stages, String name) {
        for (Map<String, Object> stage : stages)
            if (name.equalsIgnoreCase(String.valueOf(stage.get("stageName")))) return new JSONObject(stage);
        return null;
    }

    private Response ok(Response response, int status) { if (response.statusCode() != status) throw new IllegalStateException("Expected HTTP " + status + ": " + response.asString()); return response; }
    private JSONObject requiredJson(String key) { return (JSONObject) required(key); }
    private Object required(String key) { Object value = state.get(key); if (value == null) throw new IllegalStateException("Missing " + key); return value; }
}


