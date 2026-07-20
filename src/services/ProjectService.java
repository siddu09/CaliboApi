package services;

import helpers.ProjectsHelper;
import io.restassured.response.Response;
import org.json.simple.JSONObject;
import validators.ProjectValidator;

/**
 * Project Business Service.
 *
 * Responsibilities:
 * - Prepare request payload
 * - Execute Project APIs
 * - Validate responses
 * - Store runtime data
 */
public class ProjectService {

    private final ProjectsHelper projectsHelper;
    private final ProjectValidator validator;

    public ProjectService() {
        this.projectsHelper = new ProjectsHelper();
        this.validator = new ProjectValidator();
    }

    /**
     * Create Project
     */
    public void createProject() {

        JSONObject request =
                ProjectDataFactory.createProjectRequest();

        Response response =
                projectsHelper.createProject(request);

        validator.validateProjectCreated(response);

        ProjectContext.projectId =
                response.jsonPath().getString("id");

        ProjectContext.projectTitle =
                response.jsonPath().getString("title");
    }

    /**
     * Search Project
     */
    public void searchProject() {

        JSONObject request =
                ProjectDataFactory.searchProjectRequest();

        Response response =
                projectsHelper.searchProjects(request);

        validator.validateProjectSearch(response);
    }

    /**
     * Get Project
     */
    public void getProject() {

        Response response =
                projectsHelper.getProject(ProjectContext.projectId);

        validator.validateProjectDetails(response);
    }

    /**
     * Update Project
     */
    public void updateProject() {

        JSONObject request =
                ProjectDataFactory.updateProjectRequest();

        Response response =
                projectsHelper.updateProject(
                        ProjectContext.projectId,
                        request);

        validator.validateProjectUpdated(response);
    }

    /**
     * Delete Project
     */
    public void deleteProject() {

        Response response =
                projectsHelper.deleteProject(
                        ProjectContext.projectId);

        validator.validateProjectDeleted(response);
    }

    /**
     * Get Project Settings
     */
    public void getProjectSettings() {

        Response response =
                projectsHelper.getProjectSettings();

        validator.validateProjectSettings(response);
    }

    /**
     * Create Default Product Line
     */
    public void createDefaultProductLine() {

        JSONObject request =
                ProjectDataFactory.defaultProductLineRequest();

        Response response =
                projectsHelper.createDefaultProductLine(request);

        validator.validateDefaultProductLine(response);

        ProjectContext.portfolioId =
                response.jsonPath().getString("id");

        ProjectContext.portfolioTitle =
                response.jsonPath().getString("title");
    }

    /**
     * Get Product Lines
     */
    public void getProductLines() {

        Response response =
                projectsHelper.getProductLines();

        validator.validateProductLines(response);
    }

}