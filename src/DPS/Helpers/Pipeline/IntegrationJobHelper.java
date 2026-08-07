package DPS.Helpers.Pipeline;

import DPS.BuildingBlocks.CrawlersCatalogs.Crawler;

public final class IntegrationJobHelper {
    private final PipelineBuilder pipeline;

    public IntegrationJobHelper(Crawler crawler) { pipeline = new PipelineBuilder(crawler); }
    public String addDatabricksJobWithSnowflakeAsDataLake() { return pipeline.createDatabricksJobName(); }
}
