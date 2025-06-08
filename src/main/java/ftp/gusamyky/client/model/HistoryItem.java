package ftp.gusamyky.client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serial;

/**
 * Model pojedynczego wpisu historii operacji.
 */
public class HistoryItem implements java.io.Serializable {
    private final StringProperty operation = new SimpleStringProperty();
    private final StringProperty timestamp = new SimpleStringProperty();

    public HistoryItem(String operation, String timestamp) {
        this.operation.set(operation);
        this.timestamp.set(timestamp);
    }

    public String getOperation() {
        return operation.get();
    }

    public void setOperation(String operation) {
        this.operation.set(operation);
    }

    public String getTimestamp() {
        return timestamp.get();
    }

    public void setTimestamp(String timestamp) {
        this.timestamp.set(timestamp);
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
        out.defaultWriteObject();
        out.writeUTF(getOperation());
        out.writeUTF(getTimestamp());
    }

    @Serial
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        setOperation(in.readUTF());
        setTimestamp(in.readUTF());
    }
}