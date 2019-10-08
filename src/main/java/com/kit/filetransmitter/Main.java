package com.kit.filetransmitter;

import com.kit.filetransmitter.view.ChatView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        new ChatView(primaryStage);
    }
}
