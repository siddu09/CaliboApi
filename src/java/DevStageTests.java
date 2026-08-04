package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import services.StageService;

/**
 * Configures the existing Dev stage for Kubernetes and verifies the saved stage.
 *
 * Required test data is read from resources/DevStageSetup.json. The test first
 * retrieves the server-generated stage template so environment configuration IDs
 * and Kubernetes cluster details are never hardcoded in the Java test.
 */
public class DevStageTests extends BaseTest {

    private final StageService stageService = new StageService();

    @Test(groups = "deploy-stage",
            description = "Configure and verify the Dev stage for Kubernetes deployment")
    public void createDevStage() {
        stageService.createAndVerifyDevStage();
    }
}
