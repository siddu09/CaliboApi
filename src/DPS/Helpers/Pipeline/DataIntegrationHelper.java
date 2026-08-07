package DPS.Helpers.Pipeline;

import DPS.BuildingBlocks.CrawlersCatalogs.Crawler;

public final class DataIntegrationHelper {
    private final PipelineBuilder pipeline;

    public DataIntegrationHelper(Crawler crawler) { pipeline = new PipelineBuilder(crawler); }
    public void addDataIntegrationStage() { pipeline.addStage(); }
    public void addDatabricksNode() { pipeline.addNode(); }
    public void configureDatabricksNodeWithJob() { pipeline.createDatabricksJobName(); }
}
