package com.demo.cometd;

import org.cometd.annotation.server.AnnotationCometDServlet;
import org.cometd.annotation.server.ServerAnnotationProcessor;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * MyApp wrapper for AnnotationCometDServlet that adds injectables from the Spring
 * bean factory.
 *  Note that this extends CometD class
 */
public class MyAppAnnotationCometDServlet extends AnnotationCometDServlet {
    private static final Logger log = LoggerFactory.getLogger(MyAppAnnotationCometDServlet.class);
    private static final long serialVersionUID = 1L;

    // Spring fills these in for us.

    @Autowired
    BayeuxServerImpl bayeux;

    @Autowired
    private MyAppInjectables injectables;

    private ServletContext servletContext;


    /*
     *  !!!!!!! important
     * This is the last hook in the servlet api before the servlet container
     * starts the servlet. This is the only place during startup that we can get
     * at the CometD Services instantiated by the BayeuxServerImpl. the
     * super.init() call insures it. Previously I had this lazy detection in the
     * MyAppMsgReqRespRoutingMsgListener which had to do a lazy check on every
     * onMessage call. Now anything with a reference to the BayeuxServerImpl can
     * get the myAppReqRespServices set once the servlet is up and running.
     *
     * (non-Javadoc)
     *
     * @see org.cometd.annotation.AnnotationCometDServlet#init()
     */
    @Override
    public void init() throws ServletException {
        super.init();
        if (log.isDebugEnabled()) {
            log.debug("super init has completed");
        }
        this.servletContext = getServletContext();


    }


    /*
     * This method is key for allowing stuff to be injected from Spring Beans
     * factory using the Cometd injection annotations. A little bit of a hack but it
     * provides the bridge between the 2 injection mechanisms. Important so you
     * can run in spring boot.
     *
     * (non-Javadoc)
     *
     * @see org.cometd.annotation.AnnotationCometDServlet#
     * newServerAnnotationProcessor(org.cometd.bayeux.server.BayeuxServer)
     */
    @Override
    protected ServerAnnotationProcessor newServerAnnotationProcessor(BayeuxServer bayeuxServer) {
        return new ServerAnnotationProcessor(bayeuxServer, this.injectables.getInjectables());
    }





}