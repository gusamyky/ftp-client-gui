package ftp.gusamyky.client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model pliku zdalnego do wiązań w JavaFX.
 */
public class RemoteFile {
    protected final StringProperty size = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();

    public RemoteFile(String name, String size) {
        this.name.set(name);
        this.size.set(size);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }


}