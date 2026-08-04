package api;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;

import static io.restassured.RestAssured.given;

/** Executes deployment-stage API calls without business logic or assertions. */
public final class StageApiClient {

    public Response createStage(JSONObject request) {
        return given()
                .spec(RequestSpecProvider.get())
                .body(request.toJSONString())
                .when()
                .post(ApiEndpoints.DEVOPS_STAGE)
                .then()
                .extract()
                .response();
    }

    public Response getStages(String projectId, String workstreamId, String releaseId) {
        return given()
                .spec(RequestSpecProvider.get())
                .pathParam("projectId", projectId)
                .queryParam("workstreamId", workstreamId)
                .queryParam("releaseId", releaseId)
                .when()
                .get(ApiEndpoints.PROJECT_STAGES)
                .then()
                .extract()
                .response();
    }

    public Response configureTechnologies(JSONArray request, String workstreamId, String releaseId) {
        return given().spec(RequestSpecProvider.get())
                .queryParam("workstreamId", workstreamId).queryParam("releaseId", releaseId)
                .body(request.toJSONString()).post(ApiEndpoints.STAGE_TECH_STACK_PIPELINE);
    }

    public Response runCi(String pipelineDetailsId) {
        return given().spec(RequestSpecProvider.get()).pathParam("pipelineDetailsId", pipelineDetailsId)
                .get(ApiEndpoints.PIPELINE_CI_RUN);
    }

    public Response getBuildStatus(JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString())
                .post(ApiEndpoints.PIPELINE_BUILD_STATUS);
    }

    public Response deploy(JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString())
                .post(ApiEndpoints.PIPELINE_DEPLOY);
    }

    public Response getPipelineStages(JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString())
                .post(ApiEndpoints.PIPELINE_STAGE_LOGS);
    }
}
