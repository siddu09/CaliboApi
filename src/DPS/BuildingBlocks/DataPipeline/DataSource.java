package DPS.BuildingBlocks.DataPipeline;

import DPS.Helpers.Pipeline.DataSourceHelper;

public final class DataSource {
    private final DataSourceHelper helper;

    public DataSource(DataSourceHelper helper) { this.helper = helper; }
    public void addDataSourceStageWithMsSqlNode() {
        helper.addDataSourceStage();
        helper.addMsSqlNode();
        helper.configureMsSqlNodeWithDataCatalog();
    }
}
