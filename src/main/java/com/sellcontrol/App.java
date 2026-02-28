package com.sellcontrol;

import com.sellcontrol.db.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación POS SellControl.
 * Inicializa la base de datos y muestra la pantalla de Login.
 */
public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;

        // Inicializar base de datos (crear tablas + seed)
        DatabaseManager.getInstance().initialize();

        // Cargar pantalla de Login
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/sellcontrol/fxml/login.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 400, 350);
        scene.getStylesheets().add(getClass().getResource("/com/sellcontrol/css/styles.css").toExternalForm());

        stage.setTitle("POS SellControl - Inicio de Sesión");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    /**
     * Cambia la vista principal reemplazando el root de la escena actual.
     * No crea una nueva Scene para mantener el estado maximizado.
     */
    public static void changeScene(String fxmlFile, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/sellcontrol/fxml/" + fxmlFile));
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
            primaryStage.setTitle("POS SellControl - " + title);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Regresa a la pantalla de login.
     */
    public static void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/sellcontrol/fxml/login.fxml"));
            Parent root = loader.load();
            primaryStage.getScene().setRoot(root);
            primaryStage.setTitle("POS SellControl - Inicio de Sesión");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
