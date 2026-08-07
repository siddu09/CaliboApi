package DPS.Helpers.Pipeline;

import DPS.BuildingBlocks.CrawlersCatalogs.Crawler;

public final class DataSourceHelper {
    private final PipelineBuilder pipeline;

    public DataSourceHelper(Crawler crawler) { pipeline = new PipelineBuilder(crawler); }
    public void addDataSourceStage() { pipeline.addStage(); }
    public void addMsSqlNode() { pipeline.addNode(); }
    public void configureMsSqlNodeWithDataCatalog() { pipeline.addNode(); }
}
