package services;

import api.UserApiClient;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import validators.UserValidator;

import java.util.List;
import java.util.Map;

/** Executes the user-management test flow with instance-scoped runtime state. */
public final class UserService {

    private final UserApiClient api = new UserApiClient();
    private final UserValidator validator = new UserValidator();
    private final UserContext context = new UserContext();
    private final UserRequestBuilder requests = new UserRequestBuilder(context);

    public void createUser() {
        JSONObject request = requests.createUserRequest();
        Response response = api.createUser(request);
        validator.userCreated(response);

        JSONObject user = (JSONObject) ((JSONArray) request.get("users")).getFirst();
        context.email(user.get("email").toString());
        Response search = api.searchUsers(requests.searchByEmailRequest(context.email()));
        validator.successful(search, 200, "find created user");
        Map<String, Object> createdUser = findUser(search.jsonPath().getList("users"), context.email());
        context.details(createdUser);
        context.id(createdUser.get("id").toString());
        System.out.printf("[Users] Created %s (%s)%n", context.email(), context.id());
    }

    public void searchUsers() {
        validator.usersReturned(api.searchUsers(requests.searchUserRequest()), "search users");
    }

    public void getAllUsers() {
        validator.usersReturned(api.getAllUsersWithTeams(requests.getAllUsersRequest()), "get all users");
    }

    public void updateUser() {
        validator.userUpdated(api.updateUser(requests.updateUserRequest()));
    }

    public void assignRole() {
        validator.successful(api.assignRoles(requests.assignRoleRequest()), 200, "assign role");
    }

    public void deleteUser() {
        validator.successful(api.deleteUser(context.id(), new JSONObject()), 200, "delete user");
        System.out.printf("[Users] Deleted %s (%s)%n", context.email(), context.id());
    }

    private Map<String, Object> findUser(List<Map<String, Object>> users, String email) {
        if (users != null) {
            for (Map<String, Object> user : users) {
                if (email.equals(user.get("email")) && user.get("id") != null) {
                    return user;
                }
            }
        }
        throw new IllegalStateException("Created user was not returned by search: " + email);
    }
}
