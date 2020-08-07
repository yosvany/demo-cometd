package com.demo.cometd.configuration;

import com.demo.cometd.MyAppAnnotationCometDServlet;
import com.demo.cometd.MyAppInjectables;
import com.demo.cometd.service.ChatService;
import com.demo.cometd.service.MyDBService;
import org.cometd.annotation.server.AnnotationCometDServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServlet;


@Configuration
public class AppConfig {


    private static Logger log = LoggerFactory.getLogger(AppConfig.class);

    private String bayeuxMapping = "/cometd/*";

    @Bean(name = "cometd")
    public ServletRegistrationBean<HttpServlet> cometdServletReg() {
        ServletRegistrationBean<HttpServlet> regBean = new ServletRegistrationBean<>(annotationCometDServlet(),
                this.bayeuxMapping);
        regBean.setName("MyAppAnnotationCometDServlet");

        regBean.addInitParameter("services",
                ChatService.class.getName() );
        regBean.addInitParameter("ws.cometdURLMapping", this.bayeuxMapping);
        regBean.setAsyncSupported(true);
        regBean.setLoadOnStartup(1);
        return regBean;
    }

    @Bean()
    public AnnotationCometDServlet annotationCometDServlet() {
        return new MyAppAnnotationCometDServlet();
    }


    @Bean
    public MyDBService myDBService() {
        return new MyDBService();
    }

    @Bean
    public MyAppInjectables myAppInjectables() {
        return new MyAppInjectables(myDBService());
    }



}
