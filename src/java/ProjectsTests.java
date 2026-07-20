package java;

import org.testng.annotations.Test;
import services.ProjectService;

/**
 * Project API Test Scenarios.
 *
 * Responsibilities:
 * - Execute Project business scenarios
 * - No API implementation
 * - No JSON preparation
 * - No validations
 */
public class ProjectsTests extends InitializeTestSuite {

    private final ProjectService projectService = new ProjectService();

    /**
     * Creates Default Product Line
     * Required before creating a Project.
     */
    @Test(priority = 1)
    public void createDefaultProductLine() {

        projectService.createDefaultProductLine();

    }

    /**
     * Create Project
     */
    @Test(priority = 2,
            dependsOnMethods = "createDefaultProductLine")
    public void createProject() {

        projectService.createProject();

    }

    /**
     * Search Project
     */
    @Test(priority = 3,
            dependsOnMethods = "createProject")
    public void searchProject() {

        projectService.searchProject();

    }

    /**
     * Get Project Details
     */
    @Test(priority = 4,
            dependsOnMethods = "createProject")
    public void getProject() {

        projectService.getProject();

    }

    /**
     * Update Project
     */
    @Test(priority = 5,
            dependsOnMethods = "getProject")
    public void updateProject() {

        projectService.updateProject();

    }

    /**
     * Get Project Settings
     */
    @Test(priority = 6,
            dependsOnMethods = "updateProject")
    public void getProjectSettings() {

        projectService.getProjectSettings();

    }

    /**
     * Get Product Lines
     */
    @Test(priority = 7,
            dependsOnMethods = "createDefaultProductLine")
    public void getProductLines() {

        projectService.getProductLines();

    }

    /**
     * Delete Project
     */
    @Test(priority = 8,
            dependsOnMethods = "updateProject")
    public void deleteProject() {

        projectService.deleteProject();

    }

}