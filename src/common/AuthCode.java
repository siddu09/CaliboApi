package common;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Request;
import config.Config;

/** Logs in with Playwright and captures the API bearer token. */
public final class AuthCode {

    private AuthCode() {
    }

    public static void login() {
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(Config.headless));
            Page page = browser.newPage(
                    new Browser.NewPageOptions().setViewportSize(1440, 900));
            page.setDefaultTimeout(Config.explicitWait * 1_000.0);

            page.navigate(Config.loginUrl);
            page.locator("input[name='email'], input[type='email']")
                    .fill(Config.username);
            page.locator("button.login-btn, button[type='submit']").click();

            Locator tenant = page.locator("input[id^='react-select']");
            tenant.focus();
            tenant.fill(Config.tenantName);
            page.locator("[id*='react-select'][id*='option'], [class*='option']")
                    .filter(new Locator.FilterOptions().setHasText(Config.tenantName))
                    .first().click();
            page.locator("button.login-btn, button[type='submit']").click();

            page.locator("input[name='loginfmt'], input#i0116").fill(Config.username);
            page.locator("input#idSIButton9, button#idSIButton9").click();
            page.locator("input[type='password']:not(.moveOffScreen)").fill(Config.password);
            page.locator("input#idSIButton9, button#idSIButton9").click();

            Request tokenRequest = page.waitForRequest(
                    request -> request.url().startsWith(Config.baseUrl)
                            && request.headerValue("authorization") != null,
                    () -> page.locator(
                            "input[value='Yes'], button[data-testid='kmsiYes']").click());

            String authorization = tokenRequest.headerValue("authorization");
            Config.accessToken = authorization.replaceFirst("(?i)^Bearer\\s+", "");
            browser.close();
            System.out.println("[AuthCode] Playwright login succeeded; bearer token captured");
        }
    }
}
