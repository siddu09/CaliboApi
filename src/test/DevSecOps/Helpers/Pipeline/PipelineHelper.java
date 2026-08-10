package DevSecOps.Helpers.Pipeline;

import DevSecOps.Helpers.DeploymentStage.DeployStageRequestHelper;
import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public final class PipelineHelper {
    private final Map<String, Object> state;

    public PipelineHelper(Map<String, Object> state) { this.state = state; }

    public void runCiPipeline() {
        for (String id : ids("pipelineIds")) ok(given().spec(RequestSpecProvider.get())
                .pathParam("pipelineDetailsId", id).get(ApiEndpoints.PIPELINE_CI_RUN), "run CI pipeline " + id);
    }

    public void runDeploymentPipeline() {
        JSONObject setup = json("setup"), data = json("testData");
        JSONObject body = DeployStageRequestHelper.deploy(data, required("stageDetailsId").toString(),
                ids("successfulPipelineIds"), setup.get("imageTag").toString());
        ok(given().spec(RequestSpecProvider.get()).body(body.toJSONString()).post(ApiEndpoints.PIPELINE_DEPLOY),
                "deploy pipelines");
    }

    private Response ok(Response response, String activity) { if (response.statusCode() != 200) throw new IllegalStateException(activity + " failed: HTTP " + response.statusCode() + ": " + response.asString()); return response; }
    @SuppressWarnings("unchecked") private List<String> ids(String key) { return (List<String>) required(key); }
    private JSONObject json(String key) { return (JSONObject) required(key); }
    private Object required(String key) { Object value = state.get(key); if (value == null) throw new IllegalStateException("Missing DevSecOps runtime value: " + key); return value; }
}


