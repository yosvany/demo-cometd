package com.demo.cometd;

import com.demo.cometd.service.ChatService;
import org.cometd.annotation.server.AnnotationCometDServlet;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.server.BayeuxServerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

@SpringBootApplication
public class CometdApplication implements ServletContextInitializer {

	public static void main(String[] args) {
			SpringApplication.run(CometdApplication.class, args);
	}

	@Autowired
	BayeuxServerImpl bayeux;

	@Override
	public void onStartup(ServletContext servletContext) {

		servletContext.setAttribute(BayeuxServer.ATTRIBUTE, this.bayeux);
		this.bayeux.setOption(ServletContext.class.getName(), servletContext);

	}
}
