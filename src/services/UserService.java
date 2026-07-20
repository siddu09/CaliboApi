package services;

import helpers.UsersHelper;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import validators.UserValidator;

/**
 * User business workflow.
 *
 * Responsibilities:
 * - Prepare request
 * - Execute API
 * - Validate response
 * - Store runtime data
 */
public class UserService {

    private final UsersHelper usersHelper;
    private final UserValidator validator;

    public UserService() {
        this.usersHelper = new UsersHelper();
        this.validator = new UserValidator();
    }

    /**
     * Create User
     */
    public void createUser() {

        JSONObject request = UserDataFactory.createUserRequest();

        Response response = usersHelper.createUser(request);

        validator.validateUserCreated(response);

        UserContext.userId =
                response.jsonPath().getString("id");

        UserContext.email =
                request.get("email").toString();
    }

    /**
     * Search User
     */
    public void searchUser() {

        JSONObject request =
                UserDataFactory.searchUserRequest();

        Response response =
                usersHelper.searchUsers(request);

        validator.validateSearchResponse(response);
    }

    /**
     * Get User
     */
    public void getUser() {

        Response response =
                usersHelper.getUser(UserContext.userId);

        validator.validateUserDetails(response);
    }

    /**
     * Update User
     */
    public void updateUser() {

        JSONObject request =
                UserDataFactory.updateUserRequest();

        Response response =
                usersHelper.updateUser(
                        UserContext.userId,
                        request);

        validator.validateUserUpdated(response);
    }

    /**
     * Assign Role
     */
    public void assignRole() {

        JSONObject request =
                UserDataFactory.assignRoleRequest();

        Response response =
                usersHelper.assignRole(request);

        validator.validateRoleAssigned(response);
    }

    /**
     * Remove Role
     */
    public void removeRole() {

        JSONObject request =
                UserDataFactory.removeRoleRequest();

        Response response =
                usersHelper.removeRole(request);

        validator.validateRoleRemoved(response);
    }

    /**
     * Enable User
     */
    public void enableUser() {

        JSONObject request =
                UserDataFactory.enableUserRequest();

        Response response =
                usersHelper.enableUser(request);

        validator.validateUserEnabled(response);
    }

    /**
     * Disable User
     */
    public void disableUser() {

        JSONObject request =
                UserDataFactory.disableUserRequest();

        Response response =
                usersHelper.disableUser(request);

        validator.validateUserDisabled(response);
    }

    /**
     * Delete User
     */
    public void deleteUser() {

        Response response =
                usersHelper.deleteUser(
                        UserContext.userId);

        validator.validateUserDeleted(response);
    }

}