package common;

import config.Config;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.ServerSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

/**
 * Handles login and token capture using Selenium CDP.
 * Simplified version - removed unnecessary code.
 */
public final class AuthCode {

    private AuthCode() {}

    /**
     * Performs login and captures Bearer token from network traffic.
     */
    public static void login() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        if (Config.headless) opts.addArguments("--headless=new");
        opts.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
                "--window-size=1440,900", "--remote-allow-origins=*");

        // Enable performance logging for network capture
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        opts.setCapability("goog:loggingPrefs", logPrefs);
        
        Map<String, Object> perfLogPrefs = new HashMap<>();
        perfLogPrefs.put("enableNetwork", true);
        perfLogPrefs.put("enablePage", false);
        opts.setExperimentalOption("perfLoggingPrefs", perfLogPrefs);

        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingPort(findAvailablePort())
                .build();
        ChromeDriver driver = new ChromeDriver(service, opts);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Config.explicitWait));
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(15));

        try {
            // 1. Open login page
            driver.get(Config.loginUrl);
            Thread.sleep(2000);

            // 2. Enter email + click Proceed
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("input[name='email'], input[type='email']")))
                    .sendKeys(Config.username);
            shortWait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.login-btn, button[type='submit']"))).click();
            Thread.sleep(1500);

            // 3. Select tenant from dropdown (React-Select needs JS executor)
            WebElement tenantInput = wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector("input[id^='react-select']")));
            // Use JavaScript to click and focus (React-Select requirement)
            ((org.openqa.selenium.JavascriptExecutor) driver)
                    .executeScript("arguments[0].click(); arguments[0].focus();", tenantInput);
            Thread.sleep(400);
            tenantInput.sendKeys(Config.tenantName);
            Thread.sleep(1000);
            
            try {
                shortWait.until(ExpectedConditions.elementToBeClickable(
                        By.xpath("//*[contains(@id,'react-select') and contains(@id,'option')]"
                                + "[contains(normalize-space(.),'" + Config.tenantName + "')]"
                                + " | //*[contains(@class,'option')]"
                                + "[contains(normalize-space(.),'" + Config.tenantName + "')]"))).click();
            } catch (TimeoutException e) {
                tenantInput.sendKeys(Keys.ARROW_DOWN, Keys.ENTER);
            }

            // 4. Click Proceed after tenant
            shortWait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("button.login-btn, button[type='submit']"))).click();
            Thread.sleep(2000);

            // 5. Microsoft login - enter email if shown
            try {
                WebElement msEmail = shortWait.until(ExpectedConditions.visibilityOfElementLocated(
                        By.cssSelector("input[name='loginfmt'], input#i0116")));
                msEmail.clear();
                msEmail.sendKeys(Config.username);
                Thread.sleep(400);
                shortWait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("input#idSIButton9, button#idSIButton9"))).click();
                Thread.sleep(2000);
            } catch (TimeoutException ignored) {}

            // 6. Enter password + Sign In
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector("input[type='password']:not(.moveOffScreen)")))
                    .sendKeys(Config.password);
            shortWait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("input#idSIButton9, button#idSIButton9, button[type='submit']")))
                    .click();
            Thread.sleep(2000);

            // 7. Stay signed in (if prompted)
            try {
                shortWait.until(ExpectedConditions.elementToBeClickable(
                        By.cssSelector("input[value='Yes'], button[data-testid='kmsiYes']"))).click();
                Thread.sleep(2000);
            } catch (TimeoutException ignored) {}

            // 8. Wait for app to load
            wait.until(d -> {
                String url = d.getCurrentUrl();
                return url.startsWith(Config.baseUrl)
                        && !url.contains("/login")
                        && !url.contains("login.microsoftonline");
            });
            Thread.sleep(3000);
            
            System.out.println("[AuthCode] App loaded, clicking Configuration tab...");

            clickConfigurationTab(shortWait);
            Thread.sleep(2000);
            
            System.out.println("[AuthCode] Configuration tab clicked, capturing token...");
            

            String token = captureTokenFromLogs(driver);
            
            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Failed to capture Bearer token from network");
            }
            
            Config.accessToken = token;
            System.out.println("[AuthCode] Token captured successfully");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Login interrupted", e);
        } finally {
            driver.quit();
        }
    }

    private static void clickConfigurationTab(WebDriverWait wait) {
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                try {
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.cssSelector("a[href='/tenants/settings/eng-lab-home']"))).click();
                } catch (TimeoutException exception) {
                    wait.until(ExpectedConditions.elementToBeClickable(
                            By.xpath("//span[contains(@class,'icon-configurations_icon')]/.."))).click();
                }
                return;
            } catch (StaleElementReferenceException exception) {
                if (attempt == 3) throw exception;
            }
        }
    }

    /** Lets the operating system choose an available ephemeral port. */
    private static int findAvailablePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to allocate a ChromeDriver port", exception);
        }
    }

    /**
     * Captures Bearer token from performance logs.
     * Looks for platformSetupConfigs network request.
     */
    private static String captureTokenFromLogs(ChromeDriver driver) {
        for (int attempt = 1; attempt <= 10; attempt++) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            for (LogEntry entry : driver.manage().logs().get(LogType.PERFORMANCE)) {
                try {
                    JsonObject log = JsonParser.parseString(entry.getMessage()).getAsJsonObject();
                    JsonObject message = log.getAsJsonObject("message");
                    String method = message.get("method").getAsString();
                    
                    if ("Network.requestWillBeSent".equals(method)) {
                        JsonObject params = message.getAsJsonObject("params");
                        JsonObject request = params.getAsJsonObject("request");
                        String url = request.get("url").getAsString();
                        
                        if (url.contains("/configuration/settings/platformSetupConfigs")) {
                            JsonObject headers = request.getAsJsonObject("headers");
                            
                            // Try both lowercase and uppercase Authorization header
                            String authHeader = headers.has("Authorization") 
                                    ? headers.get("Authorization").getAsString()
                                    : headers.has("authorization") 
                                    ? headers.get("authorization").getAsString()
                                    : null;
                            
                            if (authHeader != null) {
                                System.out.println("[AuthCode] Found platformSetupConfigs request");
                                return authHeader.startsWith("Bearer ") 
                                        ? authHeader.substring(7) 
                                        : authHeader;
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // Skip malformed log entries
                }
            }
        }
        
        System.out.println("[AuthCode] WARNING: Token not found after 10 attempts");
        return null;
    }
}
