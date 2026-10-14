module com.example.proyectocrudg3 {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.example.proyectocrudg3.model to javafx.base;
    opens com.example.proyectocrudg3 to javafx.fxml;
    exports com.example.proyectocrudg3;
    exports com.example.proyectocrudg3.controller;
    opens com.example.proyectocrudg3.controller to javafx.fxml;
}