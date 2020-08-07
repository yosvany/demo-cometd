package com.demo.cometd;

import org.cometd.annotation.server.AnnotationCometDServlet;
import org.cometd.annotation.server.ServerAnnotationProcessor;
import org.cometd.bayeux.server.BayeuxServer;
import org.springframework.beans.factory.annotation.Autowired;

public class MyAppAnnotationCometDServlet extends AnnotationCometDServlet {

    @Autowired
    private MyAppInjectables injectables;

    @Override
    protected ServerAnnotationProcessor newServerAnnotationProcessor(BayeuxServer bayeuxServer) {
        return new ServerAnnotationProcessor(bayeuxServer, this.injectables.getInjectables());
    }





}