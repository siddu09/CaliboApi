package services;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.DropdownUtils;
import utils.JsonUtils;
import config.Config;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Creates user request payloads for one test flow.
 *
 * Responsibilities:
 * - Read JSON templates
 * - Populate dynamic values from dropdown API and user.json
 * - Return ready-to-use request objects
 * - Hold runtime context produced by one test and consumed by dependent tests
 */
public final class UserRequestBuilder {

    private static final String USERS_JSON = "users.json";

    private final UserContext context;

    UserRequestBuilder(UserContext context) {
        this.context = context;
    }

    /**
     * Generates random email with pattern: p7q8rs@calibo.com
     * @return Random email address with 6 alphanumeric characters
     */
    private static String randomEmail() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString() + "@calibo.com";
    }

    /**
     * Create User Request
     * 1. Load dropdown field values from API
     * 2. Read user.json template (has "users" array)
     * 3. Generate random email only
     * 4. Return complete request ready for API call
     */
    public JSONObject createUserRequest() {

        // FIRST: Ensure dropdown values are loaded from API
        DropdownUtils.initialize();

        // Read user.json template (has {"users": [{...}]} structure)
        JSONObject request = JsonUtils.readJson(
                Config.testDataPath + USERS_JSON);

        // Get users[0]
        JSONArray users = (JSONArray) request.get("users");
        JSONObject user = (JSONObject) users.get(0);

        // Only randomize email - all other data comes from users.json
        user.put("email", randomEmail());

        // Store email in context for later use (search/update/delete)
        context.email(user.get("email").toString());

        return request;  // Return {"users": [...]}
    }

    /**
     * Update User Request
     * Uses userEdit data from users.json with current userId and email
     */
    public JSONObject updateUserRequest() {
        if (context.details() == null) {
            throw new IllegalStateException("Created user details are unavailable for update");
        }
        JSONObject request = new JSONObject();
        request.putAll(context.details());
        if (request.get("organizationName") == null) {
            request.put("organizationName", DropdownUtils.firstValue("company"));
        }
        if (request.get("costArea") == null) request.put("costArea", new JSONObject());
        request.put("lastName", "User Updated");
        return request;
    }

    /**
     * Search User Request
     * Uses searchCriteria from users.json
     */
    public JSONObject searchUserRequest() {

        // Read user.json template
        JSONObject data = JsonUtils.readJson(
                Config.testDataPath + USERS_JSON);

        // Extract searchCriteria section
        JSONObject searchCriteria = (JSONObject) data.get("searchCriteria");

        return searchCriteria;
    }

    /**
     * Search by Email Request
     * Search for specific user by email
     */
    public JSONObject searchByEmailRequest(String email) {

        JSONObject searchRequest = new JSONObject();
        searchRequest.put("search", email);
        searchRequest.put("limit", 10);
        searchRequest.put("offset", 0);
        searchRequest.put("orderBy", "firstName");
        searchRequest.put("sort", "asc");
        
        JSONArray statuses = new JSONArray();
        statuses.add("DEACTIVE");
        statuses.add("ACTIVE");
        statuses.add("PENDING");
        statuses.add("EMAILVERIFIED");
        statuses.add("ADDED");
        searchRequest.put("status", statuses);

        return searchRequest;
    }

    /**
     * Assign Role Request
     * Uses assignSelectedRoleInput from users.json
     */
    public JSONObject assignRoleRequest() {

        // Read user.json template
        JSONObject data = JsonUtils.readJson(
                Config.testDataPath + USERS_JSON);

        // Extract assignSelectedRoleInput section
        JSONObject assignRoleInput = (JSONObject) data.get("assignSelectedRoleInput");
        
        // Update with created user's email if available
        if (context.email() != null && !context.email().isBlank()) {
            assignRoleInput.put("userEmail", context.email());
        }

        return assignRoleInput;
    }

    /**
     * Get All Users Request
     */
    public JSONObject getAllUsersRequest() {

        JSONObject request = new JSONObject();

        // Empty request body for retrieveAllUsersWithTeams endpoint
        // Add filters if needed in future

        return request;
    }

}
