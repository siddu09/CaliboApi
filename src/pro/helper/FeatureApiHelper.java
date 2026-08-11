package pro.helper;

import config.Config;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.JsonUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class FeatureApiHelper {
    private static final String E2E_JSON = Config.testDataPath + "E2E.json";

    public JSONObject loadFeatureTestData() {
        return loadFeatureTestData("product1", "feature1");
    }

    public JSONObject loadFeatureTestData(String productKey, String featureKey) {
        String path = "products." + productKey;
        JSONObject feature = JsonUtils.readSection(E2E_JSON, path + ".features." + featureKey);
        JSONObject configuration = JsonUtils.readSection(E2E_JSON, "proConfiguration");
        configuration.put("featureDescription", feature.get("description"));
        Map<String, Object> user = user();
        String ownerRoleId = configuration.get("ownerRoleId").toString();

        JSONObject member = new JSONObject();
        member.put("firstName", user.get("firstName"));
        member.put("lastName", user.get("lastName"));
        member.put("fullName", user.get("firstName") + " " + user.get("lastName"));
        member.put("userId", user.get("databaseId"));
        member.put("email", user.get("email"));
        member.put("roles", array(ownerRoleId));

        JSONArray sectionIds = new JSONArray();
        sectionIds.addAll((java.util.List<Object>) configuration.get("sectionIds"));

        JSONObject featureRequest = new JSONObject();
        featureRequest.put("title", feature.get("titlePrefix"));
        featureRequest.put("description", configuration.get("featureDescription"));
        featureRequest.put("teamName", "AutomationWorkStreamTeam");
        featureRequest.put("priority", "MEDIUM");
        featureRequest.put("projectId", JsonUtils.readString(E2E_JSON, path + ".productId"));
        featureRequest.put("sectionIds", sectionIds);
        featureRequest.put("memberList", array(member));
        featureRequest.put("workstreamStage", "NOT_STARTED");
        featureRequest.put("colorCode", "#5d6631");
        featureRequest.put("startsOn", Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        featureRequest.put("endsOn", Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli());

        JSONObject request = new JSONObject();
        request.put("workstreamsRequest", array(featureRequest));
        return request;
    }

    public String getUniqueFeatureName(JSONObject data) {
        return getUniqueFeatureName(data.get("title").toString());
    }

    public String getUniqueFeatureName(String titlePrefix) {
        return titlePrefix + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    public JSONObject feature(JSONObject request) {
        return (JSONObject) ((JSONArray) request.get("workstreamsRequest")).get(0);
    }

    public void updateRuntimeData(String productKey, String featureKey, String title,
                                  String featureId, String releaseId) {
        String path = "products." + productKey + ".features." + featureKey;
        JsonUtils.update(E2E_JSON, path + ".title", title);
        JsonUtils.update(E2E_JSON, path + ".featureId", featureId);
        JsonUtils.update(E2E_JSON, "release.releaseId", releaseId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> user() {
        return JsonUtils.readSection(E2E_JSON, "proConfiguration.user");
    }

    @SuppressWarnings("unchecked")
    private JSONArray array(Object value) {
        JSONArray array = new JSONArray();
        array.add(value);
        return array;
    }
}
