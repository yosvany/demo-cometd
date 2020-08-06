package com.demo.cometd;

/**
 * Simple holder class for multiple classes that can/should be injected into the
 * services. They are passed to ServerAnnotationProcessor by the
 * MyAppAnnotationCometDServlet;
 */
public class MyAppInjectables {

    private Object[] injectables = new Object[0];

    public MyAppInjectables(Object... injectables) {
        this.injectables = injectables;
    }


    public Object[] getInjectables() {
        return this.injectables;
    }

}
