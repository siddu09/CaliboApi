package constants;

/**
 * API Endpoints used across the framework.
 *
 * NOTE:
 * Base URL is configured separately in config.properties.
 * Only relative API paths should be defined here.
 */
public final class ApiEndpoints {

    private ApiEndpoints() {
        throw new IllegalStateException("Utility class");
    }

    /*=====================================================
     * Authentication
     *=====================================================*/

    public static final String LOGIN = "/api/login";

    public static final String LOGOUT = "/api/logout";

    /*=====================================================
     * Users
     *=====================================================*/

    public static final String USERS = "/api/users";

    public static final String USER_BY_ID = "/api/users/{userId}";

    public static final String USER_SEARCH = "/api/users/search";

    public static final String USER_ASSIGN_ROLE = "/api/users/assignRole";

    public static final String USER_REMOVE_ROLE = "/api/users/removeRole";

    public static final String USER_ENABLE = "/api/users/enable";

    public static final String USER_DISABLE = "/api/users/disable";

    /*=====================================================
     * Projects
     *=====================================================*/

    public static final String PROJECTS = "/api/projects";

    public static final String PROJECT_BY_ID = "/api/projects/{projectId}";

    public static final String PROJECT_SEARCH = "/api/projects/search";

    public static final String PROJECT_SETTINGS = "/api/projects/settings";

    /*=====================================================
     * Product Line
     *=====================================================*/

    public static final String PRODUCT_LINES = "/api/product-lines";

    public static final String DEFAULT_PRODUCT_LINE =
            "/api/product-lines/default";

    /*=====================================================
     * Dropdowns
     *=====================================================*/

    public static final String DROPDOWN_VALUES =
            "/api/dropdown/all";

    /*=====================================================
     * Business Group
     *=====================================================*/

    public static final String BUSINESS_GROUP =
            "/api/business-group";

    /*=====================================================
     * Customer Segment
     *=====================================================*/

    public static final String CUSTOMER_SEGMENT =
            "/api/customer-segment";

    /*=====================================================
     * Portfolio
     *=====================================================*/

    public static final String PORTFOLIOS =
            "/api/portfolio";

    /*=====================================================
     * Ideas
     *=====================================================*/

    public static final String IDEAS =
            "/api/ideas";

    /*=====================================================
     * Pipelines
     *=====================================================*/

    public static final String PIPELINES =
            "/api/pipelines";

    /*=====================================================
     * Assessments
     *=====================================================*/

    public static final String ASSESSMENTS =
            "/api/assessments";

}