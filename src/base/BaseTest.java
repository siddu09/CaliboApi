package base;

import common.AuthCode;
import common.RequestSpecProvider;
import config.Config;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * BaseTest - Test Suite Initialization
 * 
 * This class handles the complete test suite setup and teardown:
 * - Loads configuration from config.properties
 * - Performs Playwright login to capture the Bearer token from network traffic
 * - Initializes RestAssured request specifications
 * - Handles cleanup after all tests complete
 * 
 * All test classes should extend this base class to inherit
 * the setup and teardown behavior.
 */
public class BaseTest {

    /**
     * Suite Setup - Runs once before all tests
     * 
     * Execution flow:
     * 1. Load config.properties
     * 2. Launch browser and login via AuthCode
     * 3. Capture Bearer token from network traffic
     * 4. Initialize RestAssured with token
     */
    @BeforeSuite
    public void setupSuite() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🚀 INITIALIZING CALIBO API TEST SUITE");
        System.out.println("=".repeat(60) + "\n");

        // Load configuration
        System.out.println("📋 Step 1: Loading configuration...");
        Config.load();
        System.out.println("✅ Configuration loaded successfully\n");

        // Capture authentication token via browser
        System.out.println("🔐 Step 2: Capturing authentication token...");
        AuthCode.login();
        System.out.println("✅ Token captured successfully\n");

        // Initialize API client
        System.out.println("🌐 Step 3: Initializing API client...");
        RequestSpecProvider.initialize();
        System.out.println("✅ API client initialized\n");

        System.out.println("=".repeat(60));
        System.out.println("✅ TEST SUITE READY - Starting tests...");
        System.out.println("=".repeat(60) + "\n");
    }

    /**
     * Suite Cleanup - Runs once after all tests
     */
    @AfterSuite
    public void tearDownSuite() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("🧹 CLEANING UP TEST SUITE");
        System.out.println("=".repeat(60));

        System.out.println("✅ Test suite completed successfully");
        System.out.println("=".repeat(60) + "\n");
    }
}
