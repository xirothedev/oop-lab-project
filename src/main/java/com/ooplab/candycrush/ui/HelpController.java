package com.ooplab.candycrush.ui;

import javafx.fxml.FXML;

public final class HelpController extends BaseController {

    @FXML
    private void handleBack() {
        router().showHome();
    }
}

