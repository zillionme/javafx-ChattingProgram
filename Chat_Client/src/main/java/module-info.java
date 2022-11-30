module com.chat_client {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.chat_client to javafx.fxml;
    exports com.chat_client;
}
