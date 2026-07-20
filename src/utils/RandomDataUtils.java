package utils;

import com.github.javafaker.Faker;

import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Utility class for generating random test data.
 */
public final class RandomDataUtils {

    private static final Faker FAKER = new Faker(Locale.ENGLISH);

    private RandomDataUtils() {
        throw new IllegalStateException("Utility class");
    }

    /*=====================================================
     * User
     *=====================================================*/

    public static String firstName() {
        return FAKER.name().firstName();
    }

    public static String lastName() {
        return FAKER.name().lastName();
    }

    public static String fullName() {
        return firstName() + " " + lastName();
    }

    public static String email() {
        return "automation_"
                + UUID.randomUUID().toString().substring(0, 8)
                + "@calibo.com";
    }

    public static String mobileNumber() {
        return "9" + randomNumeric(9);
    }

    public static String employeeId() {
        return "EMP-" + randomNumeric(6);
    }

    /*=====================================================
     * Project
     *=====================================================*/

    public static String projectName() {
        return "Project-" + randomAlphaNumeric(8);
    }

    public static String portfolioName() {
        return "Portfolio-" + randomAlphaNumeric(6);
    }

    /*=====================================================
     * Generic
     *=====================================================*/

    public static String company() {
        return FAKER.company().name();
    }

    public static String city() {
        return FAKER.address().city();
    }

    public static String country() {
        return FAKER.address().country();
    }

    public static String sentence() {
        return FAKER.lorem().sentence();
    }

    public static String paragraph() {
        return FAKER.lorem().paragraph();
    }

    /*=====================================================
     * Random Strings
     *=====================================================*/

    public static String randomAlphabetic(int length) {

        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {

            builder.append(
                    alphabet.charAt(
                            ThreadLocalRandom.current()
                                    .nextInt(alphabet.length())));
        }

        return builder.toString();
    }

    public static String randomNumeric(int length) {

        String numbers = "0123456789";

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {

            builder.append(
                    numbers.charAt(
                            ThreadLocalRandom.current()
                                    .nextInt(numbers.length())));
        }

        return builder.toString();
    }

    public static String randomAlphaNumeric(int length) {

        String characters =
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < length; i++) {

            builder.append(
                    characters.charAt(
                            ThreadLocalRandom.current()
                                    .nextInt(characters.length())));
        }

        return builder.toString();
    }

    /*=====================================================
     * UUID
     *=====================================================*/

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

}