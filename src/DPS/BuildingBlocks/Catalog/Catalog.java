package DPS.BuildingBlocks.Catalog;

import DPS.BuildingBlocks.Crawler.Crawler;
import DPS.Helpers.Catalogs.CatalogHelper;

public final class Catalog {
    private final CatalogHelper helper;
    private boolean created;

    public Catalog(Crawler crawler) {
        helper = new CatalogHelper(crawler.context(), crawler.helper());
    }

    public void createCatalog() { helper.create(); created = true; }
    public void verifyCatalogDetails() {
        if (!created) throw new IllegalStateException("Catalog has not been created");
        helper.verify();
    }
}
