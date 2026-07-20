package validators;

import io.restassured.response.Response;
import org.testng.Assert;

/**
 * Validates User API responses.
 *
 * Responsibilities:
 * - Validate HTTP Status Codes
 * - Validate Response Payload
 * - Validate Business Rules
 */
public final class UserValidator {

    /**
     * Validate User Creation
     */
    public void validateUserCreated(Response response) {

        validateStatusCode(response, 201);

        Assert.assertNotNull(
                response.jsonPath().getString("id"),
                "User ID should not be null");

        Assert.assertFalse(
                response.jsonPath().getString("id").isEmpty(),
                "User ID should not be empty");
    }

    /**
     * Validate User Details
     */
    public void validateUserDetails(Response response) {

        validateStatusCode(response, 200);

        Assert.assertNotNull(
                response.jsonPath().getString("id"));

        Assert.assertNotNull(
                response.jsonPath().getString("email"));
    }

    /**
     * Validate User Update
     */
    public void validateUserUpdated(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "User update failed");
    }

    /**
     * Validate User Delete
     */
    public void validateUserDeleted(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "User deletion failed");
    }

    /**
     * Validate Search Response
     */
    public void validateSearchResponse(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getList("$").size() > 0,
                "No users found");
    }

    /**
     * Validate Role Assignment
     */
    public void validateRoleAssigned(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "Role assignment failed");
    }

    /**
     * Validate Role Removal
     */
    public void validateRoleRemoved(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "Role removal failed");
    }

    /**
     * Validate Enable User
     */
    public void validateUserEnabled(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "User enable failed");
    }

    /**
     * Validate Disable User
     */
    public void validateUserDisabled(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "User disable failed");
    }

    /**
     * Generic Status Code Validation
     */
    public void validateStatusCode(Response response,
                                   int expectedStatusCode) {

        Assert.assertEquals(
                response.getStatusCode(),
                expectedStatusCode,
                "Unexpected HTTP Status Code");
    }

    /**
     * Validate Response Contains Field
     */
    public void validateFieldExists(Response response,
                                    String jsonPath) {

        Assert.assertNotNull(
                response.jsonPath().get(jsonPath),
                "Missing field : " + jsonPath);
    }

    /**
     * Validate Response Message
     */
    public void validateMessage(Response response,
                                String expectedMessage) {

        Assert.assertEquals(
                response.jsonPath().getString("message"),
                expectedMessage,
                "Unexpected response message");
    }

}