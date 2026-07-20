package java;

import org.testng.annotations.Test;
import services.UserService;

/**
 * User API Test Scenarios.
 *
 * Responsibilities:
 * - Execute business scenarios
 * - No API implementation
 * - No JSON preparation
 * - No validations
 */
public class UsersTests extends InitializeTestSuite {

    private final UserService userService = new UserService();

    @Test(priority = 1)
    public void createUser() {

        userService.createUser();

    }

    @Test(priority = 2, dependsOnMethods = "createUser")
    public void searchUser() {

        userService.searchUser();

    }

    @Test(priority = 3, dependsOnMethods = "createUser")
    public void getUser() {

        userService.getUser();

    }

    @Test(priority = 4, dependsOnMethods = "getUser")
    public void updateUser() {

        userService.updateUser();

    }

    @Test(priority = 5, dependsOnMethods = "updateUser")
    public void assignRole() {

        userService.assignRole();

    }

    @Test(priority = 6, dependsOnMethods = "assignRole")
    public void removeRole() {

        userService.removeRole();

    }

    @Test(priority = 7, dependsOnMethods = "removeRole")
    public void disableUser() {

        userService.disableUser();

    }

    @Test(priority = 8, dependsOnMethods = "disableUser")
    public void enableUser() {

        userService.enableUser();

    }

    @Test(priority = 9, dependsOnMethods = "enableUser")
    public void deleteUser() {

        userService.deleteUser();

    }

}