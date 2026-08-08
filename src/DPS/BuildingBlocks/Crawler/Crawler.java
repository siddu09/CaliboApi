package DPS.BuildingBlocks.Crawler;

import DPS.Helpers.Crawlers.CrawlerHelper;
import DPS.Helpers.DpsContext;

public final class Crawler {
    private final DpsContext context;
    private final CrawlerHelper helper;
    private boolean completed;

    public Crawler(DpsContext context) {
        this.context = context;
        helper = new CrawlerHelper(context);
    }

    public void createDatabaseCrawler() { helper.create(); }
    public void waitForCrawlerRunToComplete() { helper.waitUntilComplete(); completed = true; }
    public void verifyCrawlerResult() {
        if (!completed) throw new IllegalStateException("Crawler has not completed");
        helper.details();
    }
    public DpsContext context() { return context; }
    public CrawlerHelper helper() { return helper; }
}
