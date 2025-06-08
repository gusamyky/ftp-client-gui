package ftp.gusamyky.client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model użytkownika do wiązań w JavaFX.
 */
public class User {
    private final StringProperty username = new SimpleStringProperty();

    public User(String username) {
        this.username.set(username);
    }

    public String getUsername() {
        return username.get();
    }
}