module com.chat_fx_program {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.chat_Server to javafx.fxml;
    exports com.chat_Server;
}
