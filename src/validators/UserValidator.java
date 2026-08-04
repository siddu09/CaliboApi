package validators;

import io.restassured.response.Response;
import org.testng.Assert;

import java.util.List;

/** Assertions for user-management responses. */
public final class UserValidator {

    public void successful(Response response, int expectedStatus, String operation) {
        Assert.assertEquals(response.statusCode(), expectedStatus,
                operation + " failed: " + response.asString());
    }

    public void userCreated(Response response) {
        successful(response, 201, "create user");
        String body = response.asString();
        Assert.assertTrue(body.contains("Success") || body.contains("created successfully"),
                "Create-user response has no success message: " + body);
    }

    public void userUpdated(Response response) {
        successful(response, 201, "update user");
        Assert.assertTrue(response.asString().contains("updated successfully"),
                "Update-user response has no success message: " + response.asString());
    }

    public void usersReturned(Response response, String operation) {
        successful(response, 200, operation);
        List<?> users = response.jsonPath().getList("users");
        Assert.assertNotNull(users, operation + " response has no users array");
    }
}
