package com.demo.cometd;

import com.demo.cometd.reposiroty.DBRepository;
import com.demo.cometd.service.ChatService;
import org.cometd.annotation.server.AnnotationCometDServlet;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.server.BayeuxServerImpl;
import org.cometd.server.authorizer.GrantAuthorizer;
import org.cometd.server.ext.AcknowledgedMessagesExtension;
import org.cometd.server.ext.TimestampExtension;
import org.cometd.server.ext.TimesyncExtension;
import org.cometd.server.http.AsyncJSONTransport;
import org.cometd.server.http.JSONPTransport;
import org.cometd.server.websocket.javax.WebSocketTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServlet;


@Configuration
public class AppConfig {


    private static Logger log = LoggerFactory.getLogger(AppConfig.class);

    @Value("${ws.cometdURLMapping:/cometd/*}")
    private String bayeuxMapping;

    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        JettyServletWebServerFactory servletFactory = new JettyServletWebServerFactory();
        log.info("Returning JettyServletWebServerFactory");
        return servletFactory;
    }

    @Bean(name = "cometd")
    public ServletRegistrationBean<HttpServlet> cometdServletReg() {
        ServletRegistrationBean<HttpServlet> regBean = new ServletRegistrationBean<>(annotationCometDServlet(),
                this.bayeuxMapping);

        /*
         * This is somehow defaulted but I felt it best to be explicit. As we
         * use it to locate the MyApp cometd servlet.
         */
        regBean.setName("MyAppAnnotationCometDServlet");

        /*
         * TODO *Still* bothered by the fact I have to list Services here. This
         * technique is used in all the examples I saw. So MyApp
         * routing capability now counts on it as well. so be aware of that
         * should you find a better way.
         *
         * Hope this doesn confuse but I left this here to help understand why I was doing things the way I did.
         *
         * To use the MyApp framework in a more general way, the
         * "services" (essentially specifically channel configuration )
         * need to be separate from the hub. Yet they are dependent on the hub
         * or at least the special "routing and auth" behaviors implemented
         * there. Even if there is another artifact between them. So by
         * definition we can't reference the services classes directly in
         * the lib module. So we make the hub a straight jar and have a boot app
         * with the services such that all the references can be resolved later.
         *
         * I haven't included real stuff her but I hope the names help you understand.
         * This init parameter is used in MyAppAnnotationCometDServlet below that extends CometD's annotation servlet.
         */
        regBean.addInitParameter("services",
                 ChatService.class.getName() );
        regBean.addInitParameter("ws.cometdURLMapping", this.bayeuxMapping);
        regBean.setAsyncSupported(true);
        regBean.setLoadOnStartup(1);
        return regBean;
    }



    /**
     *
     * MyAppAnnotationCometDServlet construction.
     *
     * In order for proper "Spring Framework" wiring to occur, servlets must be
     * constructed within the context of the bean factory. This means through
     * scanning or Bean annotation. To programmatically configure(vs web.xml)
     * the servlet itself you have to use a ServletRegistrationBean. We felt
     * this was the most straight forward way to do it in the context of a potentially
     * mult-servlet web application.
     *
     * For services labeled with CometD's Service annotation the
     * MyAppAnnotationCometDServlet uses MyAppInjectables created below to inject Beans into
     * those services via the standard Inject annotation used by CometD @Service annotated services.
     *
     * @return
     */
    @Bean()
    public AnnotationCometDServlet annotationCometDServlet() {
        return new MyAppAnnotationCometDServlet();
    }

    /**
     * Born in the Bayuex!
     **/
    @Bean
    public BayeuxServerImpl bayeuxServerImpl() {
        log.info("Creating BayeuxServerImpl");
        BayeuxServerImpl bayeux = new BayeuxServerImpl();
        bayeux.setTransports(new WebSocketTransport(bayeux), new AsyncJSONTransport(bayeux),
                new JSONPTransport(bayeux));
        bayeux.setAllowedTransports("websocket", "long-polling", "callback-polling");
        bayeux.setOption("ws.cometdURLMapping", this.bayeuxMapping);

        /*
         * Add extensions
         */
        bayeux.addExtension(new TimesyncExtension());
        bayeux.addExtension(new AcknowledgedMessagesExtension());
        bayeux.addExtension(new TimestampExtension("HH:mm:ss.SSS"));


                //This is more local customization stuff that I left in place because should you need it, the email referenced in the ticket may help.
                /*
                 * Note: Per Simone Bordet Extensions can't support this properly at the moment. He
                 * asked me to file a bug which is #878
                 *
                 * He gave me a work around for now. See
                 * FilteringBroadcastToPublisherMessageListener
                 */





       
        bayeux.createChannelIfAbsent("/**",
                (ServerChannel.Initializer) channel -> channel.addAuthorizer(GrantAuthorizer.GRANT_NONE));


        bayeux.createChannelIfAbsent(ServerChannel.META_HANDSHAKE,
                (ServerChannel.Initializer) channel -> channel.addAuthorizer(GrantAuthorizer.GRANT_PUBLISH));

        return bayeux;
    }

    @Bean
    public MyDBService myDBService() {
        return new MyDBService();
    }

    @Bean
    public MyAppInjectables myAppInjectables() {
        MyAppInjectables myAppInjectables = new MyAppInjectables(myDBService());
        return myAppInjectables;
    }



}
