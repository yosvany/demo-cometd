package com.demo.cometd;

import com.demo.cometd.service.ChatService;
import org.cometd.annotation.server.AnnotationCometDServlet;
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

	@Override
	public void onStartup(ServletContext servletContext) {

		ServletRegistration.Dynamic cometdServlet = servletContext.addServlet("cometd", AnnotationCometDServlet.class);
		String mapping = "/cometd/*";
		cometdServlet.addMapping(mapping);
		cometdServlet.setAsyncSupported(true);
		cometdServlet.setLoadOnStartup(1);

		cometdServlet.setInitParameter("services",

						ChatService.class.getName()
		);
		cometdServlet.setInitParameter("ws.cometdURLMapping", mapping);


	}
}
