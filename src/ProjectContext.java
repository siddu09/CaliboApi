package services;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Stores runtime Project data shared across test execution.
 *
 * This class acts as an in-memory context for Project-related
 * information created or retrieved during the test suite.
 */
public final class ProjectContext {

    private ProjectContext() {
        throw new IllegalStateException("Utility class");
    }

    /*=========================================================
     * Project Details
     *=========================================================*/

    public static String projectId;

    public static String projectTitle;

    public static String projectDescription;

    public static String projectStatus;

    /*=========================================================
     * Portfolio
     *=========================================================*/

    public static String portfolioId;

    public static String portfolioTitle;

    /*=========================================================
     * Business Information
     *=========================================================*/

    public static String businessGroupId;

    public static String businessGroupName;

    public static String customerSegmentId;

    public static String customerSegmentName;

    /*=========================================================
     * Creator
     *=========================================================*/

    public static String creatorId;

    public static String creatorRoleId;

    public static String projectOwnerRoleId;

    /*=========================================================
     * Members
     *=========================================================*/

    public static JSONArray memberList = new JSONArray();

    /*=========================================================
     * Complete Objects
     *=========================================================*/

    public static JSONObject projectRequest;

    public static JSONObject projectResponse;

    public static JSONObject defaultProductLine;

    /*=========================================================
     * Utility
     *=========================================================*/

    /**
     * Clears all runtime Project data.
     */
    public static void clear() {

        projectId = null;
        projectTitle = null;
        projectDescription = null;
        projectStatus = null;

        portfolioId = null;
        portfolioTitle = null;

        businessGroupId = null;
        businessGroupName = null;

        customerSegmentId = null;
        customerSegmentName = null;

        creatorId = null;
        creatorRoleId = null;
        projectOwnerRoleId = null;

        memberList.clear();

        projectRequest = null;
        projectResponse = null;
        defaultProductLine = null;
    }

}