package com.ooplab.candycrush.ui;

import javafx.fxml.FXML;

public final class HomeController extends BaseController {

    @FXML
    private void handlePlay() {
        router().showLevelSelect();
    }

    @FXML
    private void handleLeaderboard() {
        router().showLeaderboard();
    }

    @FXML
    private void handleHelp() {
        router().showHelp();
    }

    @FXML
    private void handleExit() {
        System.exit(0);
    }
}

