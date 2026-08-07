package common;

import com.microsoft.playwright.*;
import config.Config;

/** Logs in with Playwright and captures the API bearer token. */
public final class AuthCode {
    private AuthCode() {}

    public static void login() {
        try (Playwright playwright = Playwright.create()) {
            Page page = playwright.chromium()
                    .launch(new BrowserType.LaunchOptions().setHeadless(Config.headless)).newPage();
            page.setDefaultTimeout(Config.explicitWait * 1_000.0);
            page.navigate(Config.loginUrl);

            page.locator("input[name='email'], input[type='email']").fill(Config.username);
            page.locator("button.login-btn, button[type='submit']").click();
            page.locator("input[id^='react-select']").fill(Config.tenantName);
            page.locator("[id*='react-select'][id*='option'], [class*='option']")
                    .filter(new Locator.FilterOptions().setHasText(Config.tenantName)).first().click();
            page.locator("button.login-btn, button[type='submit']").click();

            page.locator("input[name='loginfmt'], input#i0116").fill(Config.username);
            page.locator("input#idSIButton9, button#idSIButton9").click();
            page.locator("input[type='password']:not(.moveOffScreen)").fill(Config.password);
            page.locator("input#idSIButton9, button#idSIButton9").click();

            Request request = page.waitForRequest(r -> r.url().startsWith(Config.baseUrl)
                            && r.headerValue("authorization") != null,
                    () -> page.locator("input[value='Yes'], button[data-testid='kmsiYes']").click());
            Config.accessToken = request.headerValue("authorization").replaceFirst("(?i)^Bearer\\s+", "");
            System.out.println("[AuthCode] Bearer token captured");
        }
    }
}
