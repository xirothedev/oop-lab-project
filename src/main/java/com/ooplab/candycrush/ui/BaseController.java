package com.ooplab.candycrush.ui;

import com.ooplab.candycrush.app.SceneRouter;

public abstract class BaseController {
    private SceneRouter router;

    public void setRouter(SceneRouter router) {
        this.router = router;
        onRouterReady();
    }

    protected SceneRouter router() {
        return router;
    }

    protected void onRouterReady() {
    }
}

