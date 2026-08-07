package DPS.BuildingBlocks.Pipeline;

import DPS.Helpers.Pipeline.DataIntegrationHelper;

public final class DataIntegration {
    private final DataIntegrationHelper helper;

    public DataIntegration(DataIntegrationHelper helper) { this.helper = helper; }
    public void addDataIntegrationStageWithDatabricksNode() {
        helper.addDataIntegrationStage();
        helper.addDatabricksNode();
        helper.configureDatabricksNodeWithJob();
    }
}
