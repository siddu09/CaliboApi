package services;

import java.util.Map;

/** Mutable runtime values scoped to one user test flow. */
final class UserContext {
    private String id;
    private String email;
    private Map<String, Object> details;

    String id() {
        return id;
    }

    void id(String id) {
        this.id = id;
    }

    String email() {
        return email;
    }

    void email(String email) {
        this.email = email;
    }

    Map<String, Object> details() {
        return details;
    }

    void details(Map<String, Object> details) {
        this.details = details;
    }
}
