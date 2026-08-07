package tests;

import DPS.BuildingBlocks.CrawlersCatalogs.Catalog;
import DPS.BuildingBlocks.CrawlersCatalogs.Crawler;
import DPS.BuildingBlocks.DataPipeline.DataSource;
import DPS.BuildingBlocks.Pipeline.DataIntegration;
import DPS.BuildingBlocks.Pipeline.DataLake;
import DPS.Helpers.CrawlersCatalogs.CrawlerCatalogUtils;
import DPS.Helpers.CrawlersCatalogs.CrawlerConfugurations;
import DPS.Helpers.Pipeline.*;
import base.BaseTest;
import org.json.simple.JSONObject;
import org.testng.annotations.Test;

public class DataIngestionPiplineWithDatabaseCrawlerCatalog extends BaseTest {
    @Test(groups = "data-ingestion")
    public void mssqlCatalogDatabricksSnowflakePipeline() {
        CrawlerConfugurations configuration = new CrawlerConfugurations();
        CrawlerCatalogUtils catalogUtils = new CrawlerCatalogUtils();
        JSONObject crawlerRequest = configuration.configureMsSqlCrawlerWithExistingDatastore();
        crawlerRequest.put("name", catalogUtils.generateUniqueCrawlerName());

        Crawler crawler = new Crawler();
        crawler.createDatabaseCrawler();
        crawler.waitForCrawlerRunToComplete();
        crawler.verifyCrawlerResult();
        new Catalog(crawler).verifyCatalogDetails();
        new DataSource(new DataSourceHelper(crawler)).addDataSourceStageWithMsSqlNode();
        new DataIntegration(new DataIntegrationHelper(crawler)).addDataIntegrationStageWithDatabricksNode();
        new DataLake(new DataLakeHelper(crawler)).addDataLakeStageWithSnowflakeNode();
        new IntegrationJobHelper(crawler).addDatabricksJobWithSnowflakeAsDataLake();
    }
}
