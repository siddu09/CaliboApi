package services;

import common.Configuration;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utils.DropdownUtils;
import utils.JsonUtils;
import utils.RandomDataUtils;

/**
 * Factory class responsible for preparing Project request payloads.
 *
 * Responsibilities:
 * - Read project.json template
 * - Populate dynamic values
 * - Populate runtime values
 * - Return ready-to-use request JSON
 */
public final class ProjectDataFactory {

    private ProjectDataFactory() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Create Project Request
     */
    public static JSONObject createProjectRequest() {

        JSONObject request = JsonUtils.readJson(
                Configuration.testDataPath + Configuration.projectJson);

        // Project Information
        request.put("title", RandomDataUtils.projectName());
        request.put("description", "Automation Project");

        // Business Group
        request.put("businessGroupId",
                DropdownUtils.firstId("businessGroup"));

        // Customer Segment
        JSONArray customerSegments = new JSONArray();
        customerSegments.add(
                DropdownUtils.firstId("customer"));

        request.put("customerSegments", customerSegments);

        // Creator Information
        request.put("creatorId",
                UserContext.userId);

        request.put("creatorRoleId",
                UserContext.roleId);

        request.put("projectOwnerRoleId",
                UserContext.roleId);

        // Portfolio Information
        request.put("portfolioId",
                ProjectContext.portfolioId);

        request.put("portfolioTitle",
                ProjectContext.portfolioTitle);

        // Member List
        JSONArray memberList = new JSONArray();

        JSONObject member = new JSONObject();

        member.put("userId",
                UserContext.userId);

        member.put("email",
                UserContext.email);

        member.put("roles",
                UserContext.roles);

        memberList.add(member);

        request.put("memberList", memberList);

        return request;
    }

    /**
     * Update Project Request
     */
    public static JSONObject updateProjectRequest() {

        JSONObject request = createProjectRequest();

        request.put("title",
                RandomDataUtils.projectName());

        request.put("description",
                "Updated Automation Project");

        return request;
    }

    /**
     * Search Project Request
     */
    public static JSONObject searchProjectRequest() {

        JSONObject request = new JSONObject();

        request.put("title",
                ProjectContext.projectTitle);

        return request;
    }

    /**
     * Default Product Line Request
     */
    public static JSONObject defaultProductLineRequest() {

        JSONObject request = new JSONObject();

        request.put("title",
                "Automation Product Line");

        request.put("description",
                "Created by Automation Framework");

        request.put("businessGroupId",
                DropdownUtils.firstId("businessGroup"));

        return request;
    }

}