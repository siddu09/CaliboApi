package helpers;

import common.RequestSpecProvider;
import constants.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

/**
 * Users REST Client.
 *
 * Responsibility:
 * - Only execute User APIs.
 * - No validations.
 * - No business logic.
 * - No test data preparation.
 */
public class UsersHelper {

    /**
     * Create User
     */
    public Response createUser(JSONObject request) {
        return post(ApiEndpoints.USERS, request);
    }

    /**
     * Search Users
     */
    public Response searchUsers(JSONObject request) {
        return post(ApiEndpoints.USER_SEARCH, request);
    }

    /**
     * Get User by ID
     */
    public Response getUser(String userId) {

        return given()
                .spec(RequestSpecProvider.get())
                .pathParam("userId", userId)
                .when()
                .get(ApiEndpoints.USER_BY_ID)
                .then()
                .extract()
                .response();
    }

    /**
     * Update User
     */
    public Response updateUser(String userId, JSONObject request) {

        return given()
                .spec(RequestSpecProvider.get())
                .pathParam("userId", userId)
                .body(request.toJSONString())
                .when()
                .put(ApiEndpoints.USER_BY_ID)
                .then()
                .extract()
                .response();
    }

    /**
     * Delete User
     */
    public Response deleteUser(String userId) {

        return given()
                .spec(RequestSpecProvider.get())
                .pathParam("userId", userId)
                .when()
                .delete(ApiEndpoints.USER_BY_ID)
                .then()
                .extract()
                .response();
    }

    /**
     * Assign Role
     */
    public Response assignRole(JSONObject request) {
        return post(ApiEndpoints.USER_ASSIGN_ROLE, request);
    }

    /**
     * Remove Role
     */
    public Response removeRole(JSONObject request) {
        return post(ApiEndpoints.USER_REMOVE_ROLE, request);
    }

    /**
     * Enable User
     */
    public Response enableUser(JSONObject request) {
        return post(ApiEndpoints.USER_ENABLE, request);
    }

    /**
     * Disable User
     */
    public Response disableUser(JSONObject request) {
        return post(ApiEndpoints.USER_DISABLE, request);
    }

    /*=========================================================
     * Generic REST Methods
     *=========================================================*/

    private Response post(String endpoint, JSONObject request) {

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
    private Response put(String endpoint, JSONObject request) {

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