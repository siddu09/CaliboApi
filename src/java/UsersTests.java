package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import services.UserService;

/**
 * User Management API Tests
 * 
 * Complete end-to-end test flow:
 * 1. Get all existing users
 * 2. Create new user with random email
 * 3. Search users list
 * 4. Assign role to created user
 * 5. Delete the created user
 */
public class UsersTests extends BaseTest {

    private final UserService userService = new UserService();

    @Test(priority = 1, description = "Get all existing users")
    public void getAllUsers() {
        userService.getAllUsers();
    }

    @Test(priority = 2, description = "Create new user with random email", dependsOnMethods = "getAllUsers")
    public void createUser() {
        userService.createUser();
    }


    @Test(priority = 3, description = "Search all users", dependsOnMethods = "createUser")
    public void searchUsers() {
        userService.searchUsers();
    }

    @Test(priority = 4, description = "Update user details", dependsOnMethods = "createUser")
    public void updateUser() {
        userService.updateUser();
    }

    @Test(priority = 5, description = "Assign role to user", dependsOnMethods = "createUser")
    public void assignRole() {
        userService.assignRole();
    }

    @Test(priority = 6, description = "Delete user", dependsOnMethods = {"updateUser", "assignRole"}, alwaysRun = true)
    public void deleteUser() {
        userService.deleteUser();
    }


}
