package DPS.BuildingBlocks.CrawlersCatalogs;

public final class Catalog {
    private final Crawler crawler;

    public Catalog(Crawler crawler) { this.crawler = crawler; }
    public void createCatalog() { crawler.verifyCrawlerResult(); }
    public void verifyCatalogDetails() { crawler.verifyCrawlerResult(); }
}
