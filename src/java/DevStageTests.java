package tests;

import DevSecops.building_blocks.DevStageBuildingBlock;
import DevSecops.helper.DevStageHelper;
import base.BaseTest;
import org.testng.annotations.Test;

public class DevStageTests extends BaseTest {
    @Test(groups = "deploy-stage")
    public void createDevStage() {
        DevStageHelper helper = new DevStageHelper();
        DevStageBuildingBlock stage = new DevStageBuildingBlock(helper);
        helper.loadTestData();
        stage.createOrUpdateStage();
        stage.verifyCreatedStage();
        stage.addNewTechnology();
        stage.configureStageTechnologies();
        stage.promote();
    }
}
