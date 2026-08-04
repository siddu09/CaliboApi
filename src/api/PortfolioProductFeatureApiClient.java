package api;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

public final class PortfolioProductFeatureApiClient {

    public Response getCurrentUser() {
        return given().spec(RequestSpecProvider.get()).get(ApiEndpoints.CURRENT_USER_INFO);
    }

    public Response getProjectRoles() {
        return given().spec(RequestSpecProvider.get()).get(ApiEndpoints.PROJECT_ROLES);
    }

    public Response createPortfolio(JSONObject request) {
        return post(ApiEndpoints.PORTFOLIOS, request);
    }

    public Response createProduct(JSONObject request) {
        return post(ApiEndpoints.PROJECTS_V2, request);
    }

    public Response createFeature(JSONObject request) {
        return post(ApiEndpoints.WORKSTREAMS_V2, request);
    }

    public Response deleteProduct(String productId, JSONObject request) {
        return given().spec(RequestSpecProvider.get())
                .pathParam("projectId", productId)
                .body(request.toJSONString())
                .delete(ApiEndpoints.PROJECT_BY_ID_V2);
    }

    private Response post(String endpoint, JSONObject request) {
        return given().spec(RequestSpecProvider.get()).body(request.toJSONString()).post(endpoint);
    }
}
