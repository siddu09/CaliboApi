package DPS.BuildingBlocks.Pipeline;

import DPS.Helpers.Pipeline.DataLakeHelper;

public final class DataLake {
    private final DataLakeHelper helper;

    public DataLake(DataLakeHelper helper) { this.helper = helper; }
    public void addDataLakeStageWithSnowflakeNode() {
        helper.addDataLakeStage();
        helper.addSnowflakeNode();
        helper.configureSnowflakeNode();
        helper.browseSnowflakeTargetTable();
    }
}
