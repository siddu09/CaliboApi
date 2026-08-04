package utils;

import common.RequestSpecProvider;
import config.Constants;
import endpoints.ApiEndpoints;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Utility for accessing dropdown / lookup values.
 */
public final class DropdownUtils {

    private static volatile JsonPath dropdownJson;

    private DropdownUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static synchronized void initialize() {

        if (dropdownJson != null) {
            return;
        }

        Response response = given()
                .spec(RequestSpecProvider.get())
                .contentType(Constants.APPLICATION_JSON)
                .accept(Constants.APPLICATION_JSON)
                .when()
                .get(ApiEndpoints.DROPDOWN_VALUES)
                .then()
                .extract()
                .response();

        String body = response.getBody().asString();
        if (response.statusCode() != 200) {
            throw new IllegalStateException(
                    "Failed to load dropdown values. Status: "
                            + response.statusCode());
        }

        dropdownJson = createJsonPath(body);
        System.out.println("Dropdown values loaded.");
    }

    private static JsonPath createJsonPath(String body) {
        String trimmedBody = body == null ? "" : body.trim();
        if (!trimmedBody.startsWith("{") && !trimmedBody.startsWith("[")) {
            throw new IllegalStateException("Dropdown API returned non-JSON response");
        }

        JsonPath rootPath = new JsonPath(trimmedBody);
        if (trimmedBody.startsWith("[")) return rootPath;
        try {
            JsonPath nestedPath = new JsonPath(trimmedBody);
            nestedPath.setRootPath("allDropdownFieldsValues");
            nestedPath.getInt("size()");
            return nestedPath;
        } catch (Exception ignored) {
            return rootPath;
        }
    }

    public static String firstId(String objectName) {
        JSONObject value = firstValue(objectName);
        return value == null ? null : value.get("id").toString();
    }

    public static String firstObjectValue(String objectName) {
        JSONObject value = firstValue(objectName);
        return value == null ? null : value.get("objectValue").toString();
    }

    public static JSONObject firstValue(String objectName) {

        initialize();

        for (int i = 0; i < dropdownJson.getInt("size()"); i++) {
            if (objectName.equalsIgnoreCase(dropdownJson.getString("[" + i + "].objectName"))) {
                Map<String, Object> value = dropdownJson.getMap("[" + i + "].values[0]");
                if (value == null) return null;
                JSONObject result = new JSONObject();
                result.putAll(value);
                return result;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public static JSONArray values(String objectName) {

        initialize();

        for (int i = 0; i < dropdownJson.getInt("size()"); i++) {
            if (objectName.equalsIgnoreCase(dropdownJson.getString("[" + i + "].objectName"))) {
                JSONArray result = new JSONArray();
                List<Map<String, Object>> values = dropdownJson.getList("[" + i + "].values");
                if (values != null) values.forEach(value -> {
                    JSONObject item = new JSONObject();
                    item.putAll(value);
                    result.add(item);
                });
                return result;
            }
        }

        return new JSONArray();
    }

    @SuppressWarnings("unchecked")
    public static JSONArray defaultRoles() {

        JSONArray roles = new JSONArray();
        JSONObject role = firstValue("role");
        if (role != null) {
            roles.add(role.get("id"));
        }
        return roles;
    }

    @SuppressWarnings("unchecked")
    public static JSONArray defaultSkills() {

        JSONArray skills = new JSONArray();
        for (Object value : values("skill")) {
            JSONObject skill = (JSONObject) value;
            skills.add(skill.get("id"));
        }
        return skills;
    }

    public static JSONObject byId(String objectName, String id) {

        for (Object value : values(objectName)) {
            JSONObject json = (JSONObject) value;
            if (id.equals(json.get("id").toString())) {
                return json;
            }
        }

        return null;
    }

    public static synchronized void clear() {
        dropdownJson = null;
    }
}
