package tests;

import common.AuthCode;
import config.Config;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginTest {

    @Test
    public void validateLoginAndToken() {
        Config.load();
        AuthCode.login();

        Assert.assertNotNull(Config.accessToken, "Token was not captured");
        Assert.assertFalse(Config.accessToken.isBlank(), "Captured token is empty");
        System.out.println("Login successful and token captured");
    }
}
