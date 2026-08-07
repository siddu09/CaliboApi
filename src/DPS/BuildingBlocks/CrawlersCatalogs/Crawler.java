package DPS.BuildingBlocks.CrawlersCatalogs;

import services.DataIngestionService;

public final class Crawler {
    private boolean completed;

    public void createDatabaseCrawler() {
        new DataIngestionService().runEndToEndIngestion();
        completed = true;
    }
    public void waitForCrawlerRunToComplete() { verifyCrawlerResult(); }
    public void verifyCrawlerResult() {
        if (!completed) throw new IllegalStateException("Database crawler flow has not completed");
    }
}
