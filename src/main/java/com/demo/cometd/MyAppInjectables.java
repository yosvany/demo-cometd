package com.demo.cometd;

public class MyAppInjectables {

    private Object[] injectables;

    public MyAppInjectables(Object... injectables) {
        this.injectables = injectables;
    }


    public Object[] getInjectables() {
        return this.injectables;
    }

}
