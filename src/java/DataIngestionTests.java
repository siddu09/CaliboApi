package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import services.DataIngestionService;

/** End-to-end MSSQL catalog → Databricks → Snowflake data-ingestion API flow. */
public class DataIngestionTests extends BaseTest {

    private final DataIngestionService service = new DataIngestionService();

    @Test(groups = "data-ingestion",
            description = "Replay and validate the complete MSSQL catalog data-ingestion flow")
    public void dataIngestion() {
        service.runEndToEndIngestion();
    }
}
