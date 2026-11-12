module uk.olivercrebbin.codenameshost {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires jdk.httpserver;
    requires java.desktop;
    requires io.nayuki.qrcodegen;
    opens uk.olivercrebbin.codenameshost to javafx.fxml;
    requires javafx.swing;
    requires com.google.gson;
    requires java.logging;
    exports uk.olivercrebbin.codenameshost;
    exports uk.olivercrebbin.codenameshost.Game;
    exports uk.olivercrebbin.codenameshost.Server;
    opens uk.olivercrebbin.codenameshost.Server to com.google.gson;
    opens uk.olivercrebbin.codenameshost.Game to javafx.fxml;
}