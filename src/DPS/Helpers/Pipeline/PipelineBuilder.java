package DPS.Helpers.Pipeline;

import DPS.BuildingBlocks.CrawlersCatalogs.Crawler;

public final class PipelineBuilder {
    private final Crawler crawler;

    public PipelineBuilder(Crawler crawler) { this.crawler = crawler; }
    public void addStage() { crawler.verifyCrawlerResult(); }
    public void addNode() { crawler.verifyCrawlerResult(); }
    public String createDatabricksJobName() { return "Databricks_MSSQL_Snowflake_" + System.currentTimeMillis(); }
}
