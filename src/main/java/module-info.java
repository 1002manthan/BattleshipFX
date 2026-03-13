module com.example.battleshipfx {
    requires javafx.controls;
    requires javafx.graphics;
    opens com.example.battleshipfx to javafx.graphics, javafx.fxml;
}