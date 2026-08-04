package api;

import common.RequestSpecProvider;
import endpoints.ApiEndpoints;
import io.restassured.response.Response;
import org.json.simple.JSONObject;

import static io.restassured.RestAssured.given;

/**
 * Users REST Client.
 *
 * Responsibility:
 * - Only execute User APIs with real Calibo endpoints.
 * - No validations.
 * - No business logic.
 * - No test data preparation.
 */
public final class UserApiClient {

    /**
     * Create User
     * POST /keycloakadapter/users/addUser
     */
    public Response createUser(JSONObject request) {
        return post(ApiEndpoints.USERS_CREATE, request);
    }

    /**
     * Search/Retrieve Users View List
     * POST /keycloakadapter/users/retrieveUsersViewList
     */
    public Response searchUsers(JSONObject request) {
        return post(ApiEndpoints.USERS_SEARCH, request);
    }

    /**
     * Retrieve All Users With Teams
     * POST /keycloakadapter/users/retrieveAllUsersWithTeams
     */
    public Response getAllUsersWithTeams(JSONObject request) {
        return post(ApiEndpoints.USERS_GET_ALL, request);
    }

    /**
     * Update User
     * PATCH /keycloakadapter/users/updateUserDetails
     */
    public Response updateUser(JSONObject request) {
        return patch(ApiEndpoints.USERS_UPDATE, request);
    }

    /**
     * Delete User
     * DELETE /keycloakadapter/users/removeUser/{userId}
     * User mentioned delete needs body parameter
     */
    public Response deleteUser(String userId, JSONObject inputJson) {

        return given()
                .spec(RequestSpecProvider.get())
                .pathParam("userId", userId)
                .body(inputJson.toJSONString())
                .when()
                .delete(ApiEndpoints.USERS_DELETE)
                .then()
                .extract()
                .response();
    }

    /**
     * Assign Roles to User
     * POST /rbac/userrole/assignselectedroles
     */
    public Response assignRoles(JSONObject request) {
        return post(ApiEndpoints.USERS_ASSIGN_ROLES, request);
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

    private Response patch(String endpoint, JSONObject request) {

        return given()
                .spec(RequestSpecProvider.get())
                .body(request.toJSONString())
                .when()
                .patch(endpoint)
                .then()
                .extract()
                .response();
    }

}
