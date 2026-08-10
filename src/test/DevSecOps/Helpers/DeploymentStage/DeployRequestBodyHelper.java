package DevSecOps.Helpers.DeploymentStage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public final class DeployRequestBodyHelper {
    private DeployRequestBodyHelper() { }

    @SuppressWarnings("unchecked")
    public static JSONObject project(JSONObject setup, Map<String, Object> user, String roleId,
                                     String portfolioId, String businessGroupId,
                                     String customerSegmentId, String title) {
        long start = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        long end = Instant.now().plus(3650, ChronoUnit.DAYS).toEpochMilli();
        JSONObject body = new JSONObject();
        body.put("teamName", setup.get("projectTeamName")); body.put("creatorRoleId", roleId);
        body.put("creatorId", user.get("databaseId")); body.put("description", setup.get("projectDescription"));
        body.put("customerSegments", array(customerSegmentId)); body.put("createConfluenceSpace", false);
        body.put("title", title); body.put("workstreamEnabled", true); body.put("moveExistingPages", false);
        body.put("memberList", array(projectMember(user, roleId, start, end)));
        body.put("ahaWorkspaceUpdateRequested", true); body.put("projectOwnerRoleId", roleId);
        body.put("portfolioTitle", setup.get("portfolioName")); body.put("endsOn", end);
        body.put("visibility", true); body.put("stageType", "PRODUCT"); body.put("releaseEnabled", false);
        body.put("startsOn", start); body.put("tags", new JSONArray()); body.put("ahaFeatureImport", false);
        body.put("portfolioId", portfolioId); body.put("sectionIds", array(1L, 3L, 4L, 5L));
        body.put("createAgileProject", false); body.put("newSpaceName", null);
        body.put("businessGroupId", businessGroupId);
        return body;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject workstream(JSONObject setup, Map<String, Object> user, String roleId,
                                        String projectId, String title) {
        JSONObject value = new JSONObject(); value.put("teamName", setup.get("workstreamTeamName"));
        value.put("endsOn", Instant.now().plus(3650, ChronoUnit.DAYS).toEpochMilli());
        value.put("description", setup.get("workstreamDescription")); value.put("teammembers", new JSONObject());
        value.put("title", title); value.put("priority", "MEDIUM");
        value.put("startsOn", Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        value.put("tags", new JSONArray()); value.put("sectionIds", array(1L, 3L, 4L, 5L));
        value.put("memberList", array(workstreamMember(user, roleId))); value.put("releaseId", null);
        value.put("workstreamStage", "NOT_STARTED"); value.put("colorCode", "#5d6631"); value.put("projectId", projectId);
        JSONObject body = new JSONObject(); body.put("workstreamsRequest", array(value));
        return body;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject repository(JSONObject setup, String projectId, String projectName,
                                        String workstreamId, String workstreamName, String releaseId,
                                        String portfolioId, String techStackId, Number groupId,
                                        String repositoryName, String repositoryUid) {
        JSONObject repo = new JSONObject(); repo.put("repoUrl", ""); repo.put("skipRepoCreation", false);
        repo.put("orgName", setup.get("gitlabGroupName")); repo.put("visibility", "private");
        repo.put("repoName", ""); repo.put("selectedRepo", new JSONObject()); repo.put("groupId", groupId);
        repo.put("repoCode", ""); repo.put("sourceCodeRepoTitle", repositoryName); repo.put("uid", repositoryUid);
        repo.put("projectKey", ""); repo.put("isMultiRepoSupported", false); repo.put("techstackId", techStackId);
        JSONObject body = new JSONObject(); body.put("portfolioId", portfolioId); body.put("releaseId", releaseId);
        body.put("releaseName", setup.get("releaseName")); body.put("workstreamId", workstreamId);
        body.put("techStackIds", array(techStackId)); body.put("createRepositories", "pending");
        body.put("title", projectName); body.put("projectName", projectName); body.put("projectId", projectId);
        body.put("portfolioName", setup.get("portfolioName")); body.put("workstreamName", workstreamName);
        body.put("techstackRepos", array(repo));
        return body;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject projectMember(Map<String, Object> user, String roleId, long start, long end) {
        JSONObject allocation = new JSONObject(); allocation.put("allocation", 0L); allocation.put("roleId", roleId);
        allocation.put("allocationFrom", start); allocation.put("allocationTo", end);
        JSONObject member = workstreamMember(user, roleId); member.put("id", user.get("id"));
        member.put("rolesAllocation", array(allocation)); member.put("totalAllocation", 0L);
        member.put("allocationFrom", start); member.put("allocationTo", end);
        return member;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject workstreamMember(Map<String, Object> user, String roleId) {
        JSONObject member = new JSONObject(); member.put("firstName", user.get("firstName"));
        member.put("lastName", user.get("lastName")); member.put("fullName", user.get("firstName") + " " + user.get("lastName"));
        member.put("userId", user.get("databaseId")); member.put("email", user.get("email")); member.put("roles", array(roleId));
        return member;
    }

    @SuppressWarnings("unchecked")
    private static JSONArray array(Object... values) {
        JSONArray array = new JSONArray();
        for (Object value : values) array.add(value);
        return array;
    }
}
