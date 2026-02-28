module com.sellcontrol {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;

    opens com.sellcontrol to javafx.fxml;
    opens com.sellcontrol.controller to javafx.fxml;
    opens com.sellcontrol.model to javafx.base;

    exports com.sellcontrol;
    exports com.sellcontrol.controller;
    exports com.sellcontrol.model;
    exports com.sellcontrol.service;
    exports com.sellcontrol.dao;
    exports com.sellcontrol.db;
}
