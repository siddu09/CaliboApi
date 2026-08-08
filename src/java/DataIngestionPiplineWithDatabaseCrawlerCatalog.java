package tests;

import DPS.BuildingBlocks.Catalog.Catalog;
import DPS.BuildingBlocks.Crawler.Crawler;
import DPS.BuildingBlocks.DataPipeline.DataIntegration;
import DPS.BuildingBlocks.DataPipeline.DataLake;
import DPS.BuildingBlocks.DataPipeline.DataSource;
import DPS.BuildingBlocks.DataPipeline.Workflow;
import DPS.Helpers.DataIntegrationHelper.DataIntegrationHelper;
import DPS.Helpers.DataLakeHelper.DataLakeHelper;
import DPS.Helpers.DataSourceHelper.DataSourceHelper;
import DPS.Helpers.DpsContext;
import base.BaseTest;
import org.testng.annotations.Test;

public class DataIngestionPiplineWithDatabaseCrawlerCatalog extends BaseTest {
    @Test(groups = "data-ingestion")
    public void mssqlCatalogDatabricksSnowflakePipeline() {
        Workflow workflow = new Workflow();
        DpsContext context = workflow.setup();
        Crawler crawler = new Crawler(context);
        crawler.createDatabaseCrawler();
        crawler.waitForCrawlerRunToComplete();
        crawler.verifyCrawlerResult();
        Catalog catalog = new Catalog(crawler);
        catalog.createCatalog();
        catalog.verifyCatalogDetails();
        new DataSource(new DataSourceHelper(crawler)).addDataSourceStageWithMsSqlNode();
        DataIntegration integration = new DataIntegration(new DataIntegrationHelper(crawler));
        integration.addDataIntegrationStageWithDatabricksNode();
        new DataLake(new DataLakeHelper(crawler)).addDataLakeStageWithSnowflakeNode();
        integration.configureDatabricksJob();
        workflow.runAndVerify();
    }
}
