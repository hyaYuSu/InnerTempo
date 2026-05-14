package main;

import manager.ScreenManager;

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        ScreenManager screenManager = new ScreenManager(stage);

        stage.setTitle("Inner Tempo");
        stage.setResizable(false);
        stage.setOpacity(1);
        screenManager.showTitle();
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
