package DPS.BuildingBlocks.DataPipeline;

import DPS.Helpers.DataLakeHelper.DataLakeHelper;

public final class DataLake {
    private final DataLakeHelper helper;

    public DataLake(DataLakeHelper helper) { this.helper = helper; }

    public void addDataLakeStageWithSnowflakeNode() {
        helper.addDataLakeStageWithSnowflakeNode();
    }
}
