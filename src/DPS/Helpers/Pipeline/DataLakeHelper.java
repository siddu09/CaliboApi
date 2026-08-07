package DPS.Helpers.Pipeline;

import DPS.BuildingBlocks.CrawlersCatalogs.Crawler;

public final class DataLakeHelper {
    private final PipelineBuilder pipeline;

    public DataLakeHelper(Crawler crawler) { pipeline = new PipelineBuilder(crawler); }
    public void addDataLakeStage() { pipeline.addStage(); }
    public void addSnowflakeNode() { pipeline.addNode(); }
    public void configureSnowflakeNode() { pipeline.addNode(); }
    public void browseSnowflakeTargetTable() { pipeline.addNode(); }
}
