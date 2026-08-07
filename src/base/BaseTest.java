package base;

import common.AuthCode;
import common.RequestSpecProvider;
import config.Config;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

public class BaseTest {

    @BeforeSuite
    public void setupSuite() {
        System.out.println("INITIALIZING CALIBO API TEST SUITE");
        Config.load();
        AuthCode.login();
        RequestSpecProvider.initialize();
        System.out.println("✅ Token captured successfully\n");
    }

    @AfterSuite
    public void tearDownSuite() {
    }
}
