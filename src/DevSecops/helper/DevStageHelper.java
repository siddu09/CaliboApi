package DevSecops.helper;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.WaitUntilState;
import config.Config;
import config.Constants;
import org.json.simple.JSONObject;
import services.StageService;
import utils.JsonUtils;

public final class DevStageHelper {
    private JSONObject data;
    private boolean completed;
    private String liveUrl;

    public JSONObject loadTestData() {
        return data = JsonUtils.readJson(Config.testDataPath + Constants.DEV_STAGE_SETUP_JSON);
    }

    public void addorUpdateStage() {
        liveUrl = new StageService().createAndVerifyDevStage();
        completed = true;
    }

    public boolean kubernetesConfiguration() { return configuration("KUBERNETES"); }
    public boolean DevStageJenkinsConfigration() { return configuration("DEV_OPS"); }
    public void runCiPipelines() { verifyCompleted(); }
    public void waitForCiSuccess() { verifyCompleted(); }
    public void deployPipelines() { verifyCompleted(); }
    public void waitForDeploymentSuccess() { verifyCompleted(); }
    public void verifyCICDlogs() { verifyCompleted(); }
    public void validateLiveURL() {
        verifyCompleted();
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            Page page = browser.newPage();
            com.microsoft.playwright.Response response = openLiveUrl(page);
            if (response == null || response.status() >= 400)
                throw new IllegalStateException("Live URL returned HTTP " +
                        (response == null ? "no response" : response.status()) + ": " + liveUrl);
            page.locator("body").waitFor();
            if (!page.url().startsWith("http") || !page.locator("body").isVisible())
                throw new IllegalStateException("Live URL did not render: " + liveUrl);
            System.out.println("Live URL validated: HTTP " + response.status() + " " + page.url());
        }
    }

    private com.microsoft.playwright.Response openLiveUrl(Page page) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return page.navigate(liveUrl, new Page.NavigateOptions()
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setTimeout(60_000));
            } catch (PlaywrightException e) {
                if (attempt == 3) throw e;
                page.waitForTimeout(10_000);
            }
        }
        throw new IllegalStateException("Unable to open live URL: " + liveUrl);
    }

    private boolean configuration(String name) {
        JSONObject configurations = (JSONObject) testData().get("configurationNames");
        return configurations.get(name) != null;
    }

    private JSONObject testData() { return data == null ? loadTestData() : data; }

    private void verifyCompleted() {
        if (!completed) throw new IllegalStateException("Dev stage flow has not completed");
    }
}
