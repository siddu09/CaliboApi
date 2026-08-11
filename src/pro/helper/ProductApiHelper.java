package pro.helper;

import config.Config;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.JsonUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class ProductApiHelper {
    private static final String E2E_JSON = Config.testDataPath + "E2E.json";

    public JSONObject loadProductTestData() {
        return loadProductTestData("product1");
    }

    public JSONObject loadProductTestData(String productKey) {
        String path = "products." + productKey;
        JSONObject product = JsonUtils.readSection(E2E_JSON, path);
        JSONObject configuration = JsonUtils.readSection(E2E_JSON, "proConfiguration");
        configuration.put("productDescription", product.get("description"));
        Map<String, Object> user = user();
        String ownerRoleId = configuration.get("ownerRoleId").toString();
        long startsOn = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        long endsOn = Instant.now().plus(30, ChronoUnit.DAYS).toEpochMilli();

        JSONObject allocation = new JSONObject();
        allocation.put("allocation", 0);
        allocation.put("roleId", ownerRoleId);
        allocation.put("allocationFrom", startsOn);
        allocation.put("allocationTo", endsOn);

        JSONObject member = member(user, ownerRoleId);
        member.put("id", user.get("id"));
        member.put("rolesAllocation", array(allocation));
        member.put("totalAllocation", 0);
        member.put("allocationFrom", startsOn);
        member.put("allocationTo", endsOn);

        JSONObject request = new JSONObject();
        request.put("title", product.get("titlePrefix"));
        request.put("description", configuration.get("productDescription"));
        request.put("teamName", "Auto_CreateProjectV2");
        request.put("creatorRoleId", ownerRoleId);
        request.put("creatorId", user.get("databaseId"));
        request.put("projectOwnerRoleId", ownerRoleId);
        request.put("portfolioId", JsonUtils.readString(E2E_JSON, "portfolio.portfolioId"));
        request.put("portfolioTitle", JsonUtils.readString(E2E_JSON, "portfolio.title"));
        request.put("businessGroupId", configuration.get("businessGroupId"));
        request.put("customerSegments", array(configuration.get("customerSegmentId")));
        request.put("memberList", array(member));
        JSONArray sectionIds = new JSONArray();
        sectionIds.addAll((java.util.List<Object>) configuration.get("sectionIds"));
        request.put("sectionIds", sectionIds);
        request.put("stageType", "PRODUCT");
        request.put("workstreamEnabled", true);
        request.put("visibility", true);
        request.put("startsOn", startsOn);
        request.put("endsOn", endsOn);
        return request;
    }

    public String getUniqueProductName(JSONObject data) {
        return getUniqueProductName(data.get("title").toString());
    }

    public String getUniqueProductName(String titlePrefix) {
        return titlePrefix + String.valueOf(System.currentTimeMillis()).substring(7);
    }

    public void updateRuntimeData(String productKey, String title, String productId) {
        JsonUtils.update(E2E_JSON, "products." + productKey + ".title", title);
        JsonUtils.update(E2E_JSON, "products." + productKey + ".productId", productId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> user() {
        return JsonUtils.readSection(E2E_JSON, "proConfiguration.user");
    }

    @SuppressWarnings("unchecked")
    private JSONObject member(Map<String, Object> user, String ownerRoleId) {
        JSONObject member = new JSONObject();
        member.put("firstName", user.get("firstName"));
        member.put("lastName", user.get("lastName"));
        member.put("fullName", user.get("firstName") + " " + user.get("lastName"));
        member.put("userId", user.get("databaseId"));
        member.put("email", user.get("email"));
        member.put("roles", array(ownerRoleId));
        return member;
    }

    @SuppressWarnings("unchecked")
    private JSONArray array(Object value) {
        JSONArray array = new JSONArray();
        array.add(value);
        return array;
    }

}
