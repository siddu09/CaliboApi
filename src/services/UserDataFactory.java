package services;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.DropdownUtils;
import utils.JsonUtils;
import utils.RandomDataUtils;
import common.Configuration;
import constants.FrameworkConstants;

/**
 * Creates User request payloads.
 *
 * Responsibilities:
 * - Read JSON templates
 * - Populate dynamic values
 * - Return ready-to-use request objects
 */
public final class UserDataFactory {

    private UserDataFactory() {
    }

    /**
     * Create User Request
     */
    public static JSONObject createUserRequest() {

        JSONObject request = JsonUtils.readJson(
                Configuration.testDataPath
                        + Configuration.usersJson);

        request.put("firstName", RandomDataUtils.firstName());

        request.put("lastName", RandomDataUtils.lastName());

        request.put("email", RandomDataUtils.email());

        request.put("phoneNumber", RandomDataUtils.mobileNumber());

        request.put("employeeId", RandomDataUtils.employeeId());

        request.put("organizationName",
                DropdownUtils.firstObjectValue("organization"));

        request.put("businessGroupId",
                DropdownUtils.firstId("businessGroup"));

        request.put("customerSegmentId",
                DropdownUtils.firstId("customer"));

        request.put("designation",
                DropdownUtils.firstObjectValue("designation"));

        request.put("department",
                DropdownUtils.firstObjectValue("department"));

        request.put("location",
                DropdownUtils.firstObjectValue("location"));

        request.put("roles",
                DropdownUtils.defaultRoles());

        request.put("skills",
                DropdownUtils.defaultSkills());

        UserContext.email =
                request.get("email").toString();

        return request;
    }

    /**
     * Update User Request
     */
    public static JSONObject updateUserRequest() {

        JSONObject request = createUserRequest();

        request.put("firstName",
                RandomDataUtils.firstName());

        request.put("lastName",
                RandomDataUtils.lastName());

        return request;
    }

    /**
     * Search User Request
     */
    public static JSONObject searchUserRequest() {

        JSONObject request = new JSONObject();

        request.put("email", UserContext.email);

        return request;
    }

    /**
     * Assign Role Request
     */
    public static JSONObject assignRoleRequest() {

        JSONObject request = new JSONObject();

        request.put("userId", UserContext.userId);

        request.put("roles",
                DropdownUtils.defaultRoles());

        return request;
    }

    /**
     * Remove Role Request
     */
    public static JSONObject removeRoleRequest() {

        JSONObject request = new JSONObject();

        request.put("userId", UserContext.userId);

        request.put("roles",
                DropdownUtils.defaultRoles());

        return request;
    }

    /**
     * Enable User Request
     */
    public static JSONObject enableUserRequest() {

        JSONObject request = new JSONObject();

        request.put("userId", UserContext.userId);

        request.put("enabled", true);

        return request;
    }

    /**
     * Disable User Request
     */
    public static JSONObject disableUserRequest() {

        JSONObject request = new JSONObject();

        request.put("userId", UserContext.userId);

        request.put("enabled", false);

        return request;
    }

}