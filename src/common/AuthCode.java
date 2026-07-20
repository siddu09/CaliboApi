package common;

public final class AuthCode {

    private static WebDriver driver;

    private static WebDriverWait wait;

    private AuthCode() {}

    public static void login() {

        driver = createDriver();

        wait = new WebDriverWait(driver, Duration.ofSeconds(60));

        try {

            openLoginPage();

            enterEmail();

            selectTenant();

            enterPassword();

            clickStaySignedIn();

            captureTokens();

        }
        finally {

            driver.quit();

        }

    }

    public static void logout(){

        new LoginHelper().userLogout(
                Configuration.tenantId,
                Configuration.accessToken,
                Configuration.refreshToken);

    }

}