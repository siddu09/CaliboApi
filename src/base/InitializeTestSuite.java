package base;

import common.AuthCode;
import common.RequestSpecProvider;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Initializes the framework before the test suite execution.
 *
 * Responsibilities:
 * 1. Load framework configuration.
 * 2. Perform one-time UI login.
 * 3. Capture authentication cookies/tokens.
 * 4. Initialize Rest Assured RequestSpecification.
 * 5. Logout after suite execution.
 */
public class InitializeTestSuite {

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {

        System.out.println("========================================");
        System.out.println("Initializing Calibo API Automation...");
        System.out.println("========================================");

        // Load configuration properties
        ConfigurationLoader.load();

        // Perform one-time UI login and capture tokens
        AuthCode.login();

        // Initialize Rest Assured Request Specification
        RequestSpecProvider.initialize();

        System.out.println("Framework initialization completed.");
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {

        System.out.println("========================================");
        System.out.println("Closing Calibo Session...");
        System.out.println("========================================");

        AuthCode.logout();

        System.out.println("Framework execution completed.");
    }
}