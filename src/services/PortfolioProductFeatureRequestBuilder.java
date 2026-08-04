package services;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

final class PortfolioProductFeatureRequestBuilder {

    private PortfolioProductFeatureRequestBuilder() {}

    @SuppressWarnings("unchecked")
    static JSONObject portfolio(JSONObject data, Map<String, Object> user, String title) {
        JSONObject stakeholder = new JSONObject();
        stakeholder.put("name", fullName(user));
        stakeholder.put("roleName", "OWNER");
        stakeholder.put("email", user.get("email"));
        stakeholder.put("username", user.get("username"));

        JSONObject request = new JSONObject();
        request.put("title", title);
        request.put("description", data.get("portfolioDescription"));
        request.put("strategy", data.get("portfolioStrategy"));
        request.put("priority", "LOW");
        request.put("currency", "USD");
        request.put("value", 100);
        request.put("stakeholders", array(stakeholder));
        return request;
    }

    @SuppressWarnings("unchecked")
    static JSONObject product(JSONObject data, Map<String, Object> user, String ownerRoleId,
                              String portfolioId, String portfolioTitle, String title,
                              long startsOn, long endsOn) {
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
        request.put("title", title);
        request.put("description", data.get("productDescription"));
        request.put("teamName", "Auto_CreateProjectV2");
        request.put("creatorRoleId", ownerRoleId);
        request.put("creatorId", user.get("databaseId"));
        request.put("projectOwnerRoleId", ownerRoleId);
        request.put("portfolioId", portfolioId);
        request.put("portfolioTitle", portfolioTitle);
        request.put("businessGroupId", data.get("businessGroupId"));
        request.put("customerSegments", array(data.get("customerSegmentId")));
        request.put("memberList", array(member));
        request.put("sectionIds", copyArray(data.get("sectionIds")));
        request.put("stageType", "PRODUCT");
        request.put("workstreamEnabled", true);
        request.put("visibility", true);
        request.put("startsOn", startsOn);
        request.put("endsOn", endsOn);
        return request;
    }

    @SuppressWarnings("unchecked")
    static JSONObject feature(JSONObject data, Map<String, Object> user, String ownerRoleId,
                              String projectId, String title, long startsOn, long endsOn) {
        JSONObject feature = new JSONObject();
        feature.put("title", title);
        feature.put("description", data.get("featureDescription"));
        feature.put("teamName", "AutomationWorkStreamTeam");
        feature.put("priority", "MEDIUM");
        feature.put("projectId", projectId);
        feature.put("sectionIds", copyArray(data.get("sectionIds")));
        feature.put("memberList", array(member(user, ownerRoleId)));
        feature.put("workstreamStage", "NOT_STARTED");
        feature.put("colorCode", "#5d6631");
        feature.put("startsOn", startsOn);
        feature.put("endsOn", endsOn);

        JSONObject request = new JSONObject();
        request.put("workstreamsRequest", array(feature));
        return request;
    }

    @SuppressWarnings("unchecked")
    static JSONObject cleanup() {
        JSONObject request = new JSONObject();
        request.put("comment", "Automation Cleanup");
        return request;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject member(Map<String, Object> user, String ownerRoleId) {
        JSONObject member = new JSONObject();
        member.put("firstName", user.get("firstName"));
        member.put("lastName", user.get("lastName"));
        member.put("fullName", fullName(user));
        member.put("userId", user.get("databaseId"));
        member.put("email", user.get("email"));
        member.put("roles", array(ownerRoleId));
        return member;
    }

    private static String fullName(Map<String, Object> user) {
        return user.get("firstName") + " " + user.get("lastName");
    }

    @SuppressWarnings("unchecked")
    private static JSONArray array(Object value) {
        JSONArray array = new JSONArray();
        array.add(value);
        return array;
    }

    @SuppressWarnings("unchecked")
    private static JSONArray copyArray(Object value) {
        JSONArray array = new JSONArray();
        array.addAll((List<Object>) value);
        return array;
    }
}
