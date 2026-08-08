package DPS.BuildingBlocks.DataPipeline;

import DPS.Helpers.DataSourceHelper.DataSourceHelper;

public final class DataSource {
    private final DataSourceHelper helper;

    public DataSource(DataSourceHelper helper) { this.helper = helper; }
    public void addDataSourceStageWithMsSqlNode() {
        helper.addDataSourceStageWithMsSqlNode();
    }
}
