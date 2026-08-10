package endpoints;

/** Central location for API endpoint paths used by the framework. */
public final class ApiEndpoints {

    private ApiEndpoints() {
        throw new IllegalStateException("Utility class");
    }

    public static final String DROPDOWN_VALUES =
            "/configuration/settings/fieldvalues?isUsageRequired=false";
    public static final String SETTINGS_BY_CONFIG_CODE =
            "/configuration/settings/client";

    public static final String USERS_GET_ALL =
            "/keycloakadapter/users/retrieveAllUsersWithTeams";
    public static final String USERS_CREATE =
            "/keycloakadapter/users/addUser";
    public static final String USERS_SEARCH =
            "/keycloakadapter/users/retrieveUsersViewList";
    public static final String USERS_UPDATE =
            "/keycloakadapter/users/updateUserDetails";
    public static final String USERS_DELETE =
            "/keycloakadapter/users/removeUser/{userId}";
    public static final String USERS_ASSIGN_ROLES =
            "/rbac/userrole/assignselectedroles";
    public static final String CURRENT_USER_INFO =
            "/keycloakadapter/users/v2/userInfo";
    public static final String PROJECT_ROLES =
            "/rbac/roles/type/project";

    public static final String PORTFOLIOS = "/elab/portfolios";
    public static final String PROJECTS_V2 = "/elab/v2/projects";
    public static final String PROJECT_BY_ID_V2 = "/elab/v2/projects/{projectId}";
    public static final String PROJECT_DEPENDENCY =
            "/elab/projects/{sourceProjectId}/dependentOn/{targetProjectId}";
    public static final String PROJECT_NOTIFICATIONS =
            "/elab/projects/getNotifications/{projectId}";
    public static final String PROJECT_NOTIFICATION_STATUS =
            "/elab/projects/updateMsgReadStatus/{notificationId}";
    public static final String WORKSTREAMS_V2 = "/elab/v2/projects/workstreams";
    public static final String PROJECT_WORKSTREAMS =
            "/elab/projects/workstreams/getAllWorkstreams/{projectId}";
    public static final String PROJECT_RELEASES = "/elab/projects/releases";
    public static final String PROJECT_TECH_STACKS =
            "/configuration/settings/techStack/ordered";
    public static final String PROJECT_REPOSITORY_GROUPS =
            "/elab/projects/repositories/groups/list";
    public static final String PROJECT_REPOSITORIES = "/elab/projects/repositories";
    public static final String REPOSITORY_CREATION_STATUS =
            "/elab/projects/repositories/createRepoStatus/{projectId}/{workstreamId}";

    public static final String DEVOPS_STAGE = "/devops/stage";
    public static final String PROJECT_STAGES = "/devops/project/{projectId}/stages";
    public static final String STAGE_TECH_STACK_PIPELINE =
            "/devops/stage/v2/techStackPipeline";
    public static final String PIPELINE_CI_RUN =
            "/devops/pipeline/{pipelineDetailsId}/ci/run";
    public static final String PIPELINE_DEPLOY = "/devops/pipeline/deploy";
    public static final String PIPELINE_BUILD_STATUS = "/devops/pipeline/v3/buildStatus";
    public static final String PIPELINE_STAGE_LOGS = "/devops/project/pipelines/v2/stages";

    // ============================== DPS ENDPOINTS ==============================
    public static final String DPS_DATA_INTEGRATION_SETTINGS =
            "/configuration/settings/client?configCode=DATA_INTEGRATION";
    public static final String DPS_SECURITY_ASSESSMENT_SETTINGS =
            "/configuration/settings/client?configCode=SECURITY_ASSESSMENT";
    public static final String DPS_SETTINGS_VERSION = "/configuration/settings/version";
    public static final String DPS_DATA_STORES =
            "/configuration/v2/settings/dataStores/all/permission";

    public static final String DPS_STAGE = "/devops/stage/{stageName}";
    public static final String DPS_PIPELINE_DETAILS =
            "/datapipeline/project/dataflow/pipeline/details";
    public static final String DPS_DRAFT = "/datapipeline/project/dataflow/v2/draft";
    public static final String DPS_DRAFT_STATUS =
            "/datapipeline/project/dataflow/v2/draft/status";
    public static final String DPS_DRAFT_PUBLISH =
            "/datapipeline/project/dataflow/v2/draft/publish";
    public static final String DPS_DRAFT_PUBLISH_V3 =
            "/datapipeline/project/dataflow/v3/draft/publish";
    public static final String DPS_ALL_STAGE_DETAILS =
            "/datapipeline/project/dataflow/allStages/detail";
    public static final String DPS_WORKFLOW_INITIATE = "/datapipeline/workflow/v2/initiate";
    public static final String DPS_WORKFLOW_STATUS =
            "/datapipeline/project/dataflow/v3/status";

    public static final String DPS_CRAWLERS = "/datapipeline/crawlers";
    public static final String DPS_CRAWLER = "/datapipeline/crawlers/{crawlerId}";
    public static final String DPS_CRAWLER_RUN = "/datapipeline/crawlers/run";
    public static final String DPS_CRAWLER_DETAILS =
            "/datapipeline/crawlers/{crawlerId}/details";
    public static final String DPS_CATALOGS = "/datapipeline/catalogs";
    public static final String DPS_CATALOG = "/datapipeline/catalogs/{catalogId}";

    public static final String DPS_DATABRICKS_TEMPLATES = "/databricks/v1/template";
    public static final String DPS_CLIENT_SETTINGS = "/configuration/settings/client";
    public static final String DPS_DATABRICKS_TEMPLATE =
            "/databricks/v2/template/{templateId}";
    public static final String DPS_DATABRICKS_CLUSTER_WHL_MAPPING =
            "/databricks/config/clusterWhlMapping";
    public static final String DPS_DATABRICKS_JOB = "/databricks/v1/jobs/{jobId}";
    public static final String DPS_SNOWFLAKE_TABLE_ROWS = "/snowflake/v2/table/rows";

    public static final String DPS_PROJECT_REPOSITORIES = "/elab/projects/repositories";
    public static final String DPS_PROJECT_REPOSITORY_LIST =
            "/elab/projects/{projectId}/repositories";
    public static final String DPS_DELETE_PROJECT_REPOSITORIES =
            "/elab/projects/repositories/{projectId}/deleteRepos";
    public static final String DPS_DELETE_WORKSTREAM =
            "/elab/projects/workstreams/{workstreamId}/{projectId}";
}
