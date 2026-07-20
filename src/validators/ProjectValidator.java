package validators;

import io.restassured.response.Response;
import org.testng.Assert;

/**
 * Validates Project API responses.
 *
 * Responsibilities:
 * - Validate HTTP Status Codes
 * - Validate Response Payload
 * - Validate Business Rules
 */
public final class ProjectValidator {

    /**
     * Validate Project Creation
     */
    public void validateProjectCreated(Response response) {

        validateStatusCode(response, 201);

        Assert.assertNotNull(
                response.jsonPath().getString("id"),
                "Project ID should not be null");

        Assert.assertFalse(
                response.jsonPath().getString("id").isEmpty(),
                "Project ID should not be empty");
    }

    /**
     * Validate Project Details
     */
    public void validateProjectDetails(Response response) {

        validateStatusCode(response, 200);

        Assert.assertNotNull(
                response.jsonPath().getString("id"));

        Assert.assertNotNull(
                response.jsonPath().getString("title"));
    }

    /**
     * Validate Project Update
     */
    public void validateProjectUpdated(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "Project update failed");
    }

    /**
     * Validate Project Delete
     */
    public void validateProjectDeleted(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getBoolean("success"),
                "Project deletion failed");
    }

    /**
     * Validate Project Search
     */
    public void validateProjectSearch(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getList("$").size() > 0,
                "No projects found");
    }

    /**
     * Validate Project Settings
     */
    public void validateProjectSettings(Response response) {

        validateStatusCode(response, 200);

        Assert.assertNotNull(
                response.jsonPath().get("settings"),
                "Project settings are missing");
    }

    /**
     * Validate Default Product Line
     */
    public void validateDefaultProductLine(Response response) {

        validateStatusCode(response, 201);

        Assert.assertNotNull(
                response.jsonPath().getString("id"),
                "Portfolio ID should not be null");

        Assert.assertNotNull(
                response.jsonPath().getString("title"),
                "Portfolio title should not be null");
    }

    /**
     * Validate Product Lines
     */
    public void validateProductLines(Response response) {

        validateStatusCode(response, 200);

        Assert.assertTrue(
                response.jsonPath().getList("$").size() > 0,
                "No Product Lines found");
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
     * Validate Field Exists
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