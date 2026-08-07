package DevSecops.building_blocks;

import DevSecops.helper.DevStageHelper;
import org.testng.Assert;

public final class DevStageBuildingBlock {
    private final DevStageHelper helper;

    public DevStageBuildingBlock(DevStageHelper helper) { this.helper = helper; }

    public void createOrUpdateStage() { helper.addorUpdateStage(); }
    public void verifyCreatedStage() { helper.waitForDeploymentSuccess(); }

    public void addNewTechnology() {
        Assert.assertTrue(helper.kubernetesConfiguration(), "Kubernetes configuration is missing");
        Assert.assertTrue(helper.DevStageJenkinsConfigration(), "Jenkins configuration is missing");
    }

    public void configureStageTechnologies() {
        helper.runCiPipelines();
        helper.waitForCiSuccess();
    }

    public void promote() {
        helper.deployPipelines();
        helper.waitForDeploymentSuccess();
        helper.verifyCICDlogs();
        helper.validateLiveURL();
    }
}
