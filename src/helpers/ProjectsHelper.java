package helpers;

import common.RequestSpecProvider;
import constants.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

/**
 * REST client for Project APIs.
 *
 * Responsibilities:
 * - Execute Project REST APIs
 * - Return Response
 * - No business logic
 * - No validations
 */
public class ProjectsHelper {

    /**
     * Create Project
     */
    public Response createProject(JSONObject request) {
        return post(ApiEndpoints.PROJECTS, request);
    }

    /**
     * Search Projects
     */
    public Response searchProjects(JSONObject request) {
        return post(ApiEndpoints.PROJECT_SEARCH, request);
    }

    /**
     * Get Project By Id
     */
    public Response getProject(String projectId) {

        return given()
                .spec(RequestSpecProvider.get())
                .pathParam("projectId", projectId)
                .when()
                .get(ApiEndpoints.PROJECT_BY_ID)
                .then()
                .extract()
                .response();
    }

    /**
     * Update Project
     */
    public Response updateProject(String projectId,
                                  JSONObject request) {

        return given()
                .spec(RequestSpecProvider.get())
                .pathParam("projectId", projectId)
                .body(request.toJSONString())
                .when()
                .put(ApiEndpoints.PROJECT_BY_ID)
                .then()
                .extract()
                .response();
    }

    /**
     * Delete Project
     */
    public Response deleteProject(String projectId) {

        return given()
                .spec(RequestSpecProvider.get())
                .pathParam("projectId", projectId)
                .when()
                .delete(ApiEndpoints.PROJECT_BY_ID)
                .then()
                .extract()
                .response();
    }

    /**
     * Get Project Settings
     */
    public Response getProjectSettings() {

        return given()
                .spec(RequestSpecProvider.get())
                .when()
                .get(ApiEndpoints.PROJECT_SETTINGS)
                .then()
                .extract()
                .response();
    }

    /**
     * Create Default Product Line
     */
    public Response createDefaultProductLine(JSONObject request) {
        return post(ApiEndpoints.DEFAULT_PRODUCT_LINE, request);
    }

    /**
     * Get Product Lines
     */
    public Response getProductLines() {

        return given()
                .spec(RequestSpecProvider.get())
                .when()
                .get(ApiEndpoints.PRODUCT_LINES)
                .then()
                .extract()
                .response();
    }

    /*=====================================================
     * Generic REST Methods
     *=====================================================*/

    private Response post(String endpoint,
                          JSONObject request) {

        return given()
                .spec(RequestSpecProvider.get())
                .body(request.toJSONString())
                .when()
                .post(endpoint)
                .then()
                .extract()
                .response();
    }

    @SuppressWarnings("unused")
    private Response put(String endpoint,
                         JSONObject request) {

        return given()
                .spec(RequestSpecProvider.get())
                .body(request.toJSONString())
                .when()
                .put(endpoint)
                .then()
                .extract()
                .response();
    }

    @SuppressWarnings("unused")
    private Response get(String endpoint) {

        return given()
                .spec(RequestSpecProvider.get())
                .when()
                .get(endpoint)
                .then()
                .extract()
                .response();
    }

    @SuppressWarnings("unused")
    private Response delete(String endpoint) {

        return given()
                .spec(RequestSpecProvider.get())
                .when()
                .delete(endpoint)
                .then()
                .extract()
                .response();
    }

}