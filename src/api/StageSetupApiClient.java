package api;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

/** API calls used to create the project/workstream context required by a Dev stage. */
public final class StageSetupApiClient {

    public Response getConfiguration(String configCode) {
        return given().spec(RequestSpecProvider.get()).queryParam("configCode", configCode)
                .get(ApiEndpoints.SETTINGS_BY_CONFIG_CODE);
    }

    public Response getCurrentUser() {
        return given().spec(RequestSpecProvider.get()).get(ApiEndpoints.CURRENT_USER_INFO);
    }

    public Response getProjectRoles() {
        return given().spec(RequestSpecProvider.get()).get(ApiEndpoints.PROJECT_ROLES);
    }

    public Response getPortfolio(String name) {
        return given().spec(RequestSpecProvider.get())
                .queryParam("limit", 10).queryParam("offset", 0)
                .queryParam("sort", "asc").queryParam("search", name)
                .get(ApiEndpoints.PORTFOLIOS);
    }

    public Response getFieldValues() {
        return given().spec(RequestSpecProvider.get())
                .queryParam("isUsageRequired", false).get(ApiEndpoints.DROPDOWN_VALUES);
    }

    public Response createProject(JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString())
                .post(ApiEndpoints.PROJECTS_V2);
    }

    public Response updateProject(String projectId, JSONObject request) {
        return given().spec(RequestSpecProvider.get()).pathParam("projectId", projectId)
                .body(request.toJSONString()).patch(ApiEndpoints.PROJECT_BY_ID_V2);
    }

    public Response createWorkstream(JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString())
                .post(ApiEndpoints.WORKSTREAMS_V2);
    }

    public Response getWorkstreams(String projectId) {
        return given().spec(RequestSpecProvider.get()).pathParam("projectId", projectId)
                .get(ApiEndpoints.PROJECT_WORKSTREAMS);
    }

    public Response getReleases(String projectId) {
        return given().spec(RequestSpecProvider.get()).queryParam("projectId", projectId)
                .get(ApiEndpoints.PROJECT_RELEASES);
    }

    public Response getTechStacks(String projectId) {
        return given().spec(RequestSpecProvider.get()).queryParam("projectId", projectId)
                .queryParam("isSelected", true).get(ApiEndpoints.PROJECT_TECH_STACKS);
    }

    public Response getRepositoryGroups(String projectId) {
        return given().spec(RequestSpecProvider.get()).queryParam("projectId", projectId)
                .get(ApiEndpoints.PROJECT_REPOSITORY_GROUPS);
    }

    public Response createRepository(JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString())
                .post(ApiEndpoints.PROJECT_REPOSITORIES);
    }

    public Response getRepositoryCreationStatus(String projectId, String workstreamId) {
        return given().spec(RequestSpecProvider.get())
                .pathParam("projectId", projectId).pathParam("workstreamId", workstreamId)
                .get(ApiEndpoints.REPOSITORY_CREATION_STATUS);
    }
}
