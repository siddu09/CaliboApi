package services;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

/** Builds project and workstream bodies matching the supplied successful execution log. */
public final class StageSetupRequestBuilder {

    private StageSetupRequestBuilder() {
    }

    @SuppressWarnings("unchecked")
    public static JSONObject projectRequest(JSONObject setup, Map<String, Object> user,
                                            String ownerRoleId, String portfolioId,
                                            String businessGroupId, String customerSegmentId,
                                            String projectTitle) {
        long startsOn = Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli();
        long endsOn = Instant.now().plus(3650, ChronoUnit.DAYS).toEpochMilli();

        JSONObject request = new JSONObject();
        request.put("teamName", setup.get("projectTeamName"));
        request.put("creatorRoleId", ownerRoleId);
        request.put("creatorId", user.get("databaseId"));
        request.put("description", setup.get("projectDescription"));
        request.put("customerSegments", array(customerSegmentId));
        request.put("createConfluenceSpace", false);
        request.put("title", projectTitle);
        request.put("workstreamEnabled", true);
        request.put("moveExistingPages", false);
        request.put("memberList", array(projectMember(user, ownerRoleId, startsOn, endsOn)));
        request.put("ahaWorkspaceUpdateRequested", true);
        request.put("projectOwnerRoleId", ownerRoleId);
        request.put("portfolioTitle", setup.get("portfolioName"));
        request.put("endsOn", endsOn);
        request.put("visibility", true);
        request.put("stageType", "PRODUCT");
        request.put("releaseEnabled", false);
        request.put("startsOn", startsOn);
        request.put("tags", new JSONArray());
        request.put("ahaFeatureImport", false);
        request.put("portfolioId", portfolioId);
        request.put("sectionIds", array(1L, 3L, 4L, 5L));
        request.put("createAgileProject", false);
        request.put("newSpaceName", null);
        request.put("businessGroupId", businessGroupId);
        return request;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject workstreamRequest(JSONObject setup, Map<String, Object> user,
                                               String ownerRoleId, String projectId,
                                               String workstreamTitle) {
        JSONObject workstream = new JSONObject();
        workstream.put("teamName", setup.get("workstreamTeamName"));
        workstream.put("endsOn", Instant.now().plus(3650, ChronoUnit.DAYS).toEpochMilli());
        workstream.put("description", setup.get("workstreamDescription"));
        workstream.put("teammembers", new JSONObject());
        workstream.put("title", workstreamTitle);
        workstream.put("priority", "MEDIUM");
        workstream.put("startsOn", Instant.now().minus(1, ChronoUnit.DAYS).toEpochMilli());
        workstream.put("tags", new JSONArray());
        workstream.put("sectionIds", array(1L, 3L, 4L, 5L));
        workstream.put("memberList", array(workstreamMember(user, ownerRoleId)));
        workstream.put("releaseId", null);
        workstream.put("workstreamStage", "NOT_STARTED");
        workstream.put("colorCode", "#5d6631");
        workstream.put("projectId", projectId);

        JSONObject request = new JSONObject();
        request.put("workstreamsRequest", array(workstream));
        return request;
    }

    @SuppressWarnings("unchecked")
    public static JSONObject repositoryRequest(JSONObject setup, String projectId,
                                               String projectName, String workstreamId,
                                               String workstreamName, String releaseId,
                                               String portfolioId, String techStackId,
                                               Number groupId, String repositoryName) {
        JSONObject repo = new JSONObject();
        repo.put("repoUrl", "");
        repo.put("skipRepoCreation", false);
        repo.put("orgName", setup.get("gitlabGroupName"));
        repo.put("visibility", "private");
        repo.put("repoName", "");
        repo.put("selectedRepo", new JSONObject());
        repo.put("groupId", groupId);
        repo.put("repoCode", "");
        repo.put("sourceCodeRepoTitle", repositoryName);
        repo.put("uid", repositoryName);
        repo.put("projectKey", "");
        repo.put("isMultiRepoSupported", false);
        repo.put("techstackId", techStackId);

        JSONObject request = new JSONObject();
        request.put("portfolioId", portfolioId);
        request.put("releaseId", releaseId);
        request.put("releaseName", setup.get("releaseName"));
        request.put("workstreamId", workstreamId);
        request.put("techStackIds", array(techStackId));
        request.put("createRepositories", "pending");
        request.put("title", projectName);
        request.put("projectName", projectName);
        request.put("projectId", projectId);
        request.put("portfolioName", setup.get("portfolioName"));
        request.put("workstreamName", workstreamName);
        request.put("techstackRepos", array(repo));
        return request;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject projectMember(Map<String, Object> user, String roleId,
                                            long startsOn, long endsOn) {
        JSONObject allocation = new JSONObject();
        allocation.put("allocation", 0L);
        allocation.put("roleId", roleId);
        allocation.put("allocationFrom", startsOn);
        allocation.put("allocationTo", endsOn);

        JSONObject member = workstreamMember(user, roleId);
        member.put("id", user.get("id"));
        member.put("rolesAllocation", array(allocation));
        member.put("totalAllocation", 0L);
        member.put("allocationFrom", startsOn);
        member.put("allocationTo", endsOn);
        return member;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject workstreamMember(Map<String, Object> user, String roleId) {
        JSONObject member = new JSONObject();
        member.put("firstName", user.get("firstName"));
        member.put("lastName", user.get("lastName"));
        member.put("fullName", user.get("firstName") + " " + user.get("lastName"));
        member.put("userId", user.get("databaseId"));
        member.put("email", user.get("email"));
        member.put("roles", array(roleId));
        return member;
    }

    @SuppressWarnings("unchecked")
    private static JSONArray array(Object... values) {
        JSONArray array = new JSONArray();
        for (Object value : values) array.add(value);
        return array;
    }
}
