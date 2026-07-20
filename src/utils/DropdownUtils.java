package utils;

import constants.CommonDataConstants;
import io.restassured.path.json.JsonPath;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.PrepareSampleJsonDataUtility;

/**
 * Utility for accessing dropdown values.
 *
 * Loads dropdown values only once and serves
 * Business Group, Customer, Roles, Skills etc.
 */
public final class DropdownUtils {

    private static JsonPath dropdownJson;

    private DropdownUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Loads dropdown values once.
     * Call this during @BeforeSuite.
     */
    public static void initialize() {

        if (dropdownJson != null) {
            return;
        }

        PrepareSampleJsonDataUtility.getAllDropdownFieldValues();

        JSONObject response =
                PrepareSampleJsonDataUtility.getJsonFromCommonJsonDataMap(
                        CommonDataConstants.DROPDOWN_FIELD_VALUES_ALL_JSON);

        dropdownJson = new JsonPath(response.toJSONString());

        dropdownJson.setRootPath("allDropdownFieldsValues");
    }

    /**
     * Returns first dropdown ID.
     */
    public static String firstId(String objectName) {

        JSONObject value = firstValue(objectName);

        return value == null ? null : value.get("id").toString();
    }

    /**
     * Returns first dropdown object value.
     */
    public static String firstObjectValue(String objectName) {

        JSONObject value = firstValue(objectName);

        return value == null ? null : value.get("objectValue").toString();
    }

    /**
     * Returns complete first dropdown object.
     */
    public static JSONObject firstValue(String objectName) {

        initialize();

        int size = dropdownJson.getInt("size()");

        for (int i = 0; i < size; i++) {

            if (objectName.equalsIgnoreCase(
                    dropdownJson.getString("[" + i + "].objectName"))) {

                return dropdownJson.getObject(
                        "[" + i + "].values[0]",
                        JSONObject.class);
            }
        }

        return null;
    }

    /**
     * Returns complete values array.
     */
    public static JSONArray values(String objectName) {

        initialize();

        int size = dropdownJson.getInt("size()");

        for (int i = 0; i < size; i++) {

            if (objectName.equalsIgnoreCase(
                    dropdownJson.getString("[" + i + "].objectName"))) {

                return dropdownJson.getObject(
                        "[" + i + "].values",
                        JSONArray.class);
            }
        }

        return new JSONArray();
    }

    /**
     * Default Roles.
     */
    public static JSONArray defaultRoles() {

        JSONArray roles = new JSONArray();

        JSONObject role = firstValue("role");

        if (role != null) {
            roles.add(role.get("id"));
        }

        return roles;
    }

    /**
     * Default Skills.
     */
    public static JSONArray defaultSkills() {

        JSONArray skills = new JSONArray();

        JSONArray values = values("skill");

        for (Object value : values) {

            JSONObject skill = (JSONObject) value;

            skills.add(skill.get("id"));
        }

        return skills;
    }

    /**
     * Generic dropdown lookup by id.
     */
    public static JSONObject byId(String objectName,
                                  String id) {

        JSONArray values = values(objectName);

        for (Object value : values) {

            JSONObject json = (JSONObject) value;

            if (id.equals(json.get("id").toString())) {
                return json;
            }
        }

        return null;
    }

    /**
     * Clears cached dropdown values.
     */
    public static void clear() {
        dropdownJson = null;
    }

}