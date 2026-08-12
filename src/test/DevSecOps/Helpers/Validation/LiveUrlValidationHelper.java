package DevSecOps.Helpers.Validation;

import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;
import config.Config;
import utils.JsonUtils;

import java.util.Map;

public final class LiveUrlValidationHelper {
    private static final String E2E_JSON = Config.testDataPath + "E2E.json";
    private final Map<String, Object> state;

    public LiveUrlValidationHelper(Map<String, Object> state) { this.state = state; }

    public void validateLiveUrl() {
        String url = String.valueOf(state.get("liveUrl"));
        if (!url.matches("https?://.+")) throw new IllegalStateException("Deployment did not provide a valid live URL: " + url);
        try (Playwright playwright = Playwright.create()) {
            Page page = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true)).newPage();
            com.microsoft.playwright.Response response = open(page, url);
            if (response == null || response.status() >= 400 || !page.locator("body").isVisible())
                throw new IllegalStateException("Live URL validation failed: " + url);
            String validatedUrl = page.url();
            JsonUtils.update(E2E_JSON, "devSecOps.liveUrl", validatedUrl);
            System.out.println("Live URL validated: HTTP " + response.status() + " " + validatedUrl);
        }
    }

    private com.microsoft.playwright.Response open(Page page, String url) {
        for (int attempt = 1; attempt <= 3; attempt++) try {
            return page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(60_000));
        } catch (PlaywrightException exception) {
            if (attempt == 3) throw exception;
            page.waitForTimeout(10_000);
        }
        throw new IllegalStateException("Unable to open live URL: " + url);
    }
}


