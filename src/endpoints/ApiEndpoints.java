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
}
