package DPS.BuildingBlocks.DataPipeline;

import DPS.Helpers.DataIntegrationHelper.DataIntegrationHelper;

public final class DataIntegration {
    private final DataIntegrationHelper helper;

    public DataIntegration(DataIntegrationHelper helper) { this.helper = helper; }

    public void addDataIntegrationStageWithDatabricksNode() {
        helper.addDataIntegrationStageWithDatabricksNode();
    }

    public void configureDatabricksJob() { helper.configureDatabricksJob(); }
}
