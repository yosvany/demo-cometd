# CometD + spring boot + JPA

This is a very simple demo of cometD integration with spring boot.

clone the repo and run 
 ```shell script
 mvn clean package && java -jar target/cometd-example-1.0.0.jar
```

Or 

 ```shell script
 mvn spring-boot:run
```
 
If you prefer Docker:

````shell script
mvn clean package && docker build --rm -t demo-cometd . && docker run -it -p8080:8080 demo-cometd
````  
Once the server start, you can open http://localhost:8080 and join the chat with one user, then repeat in another tab with a different user

Every message will be saved to the in memory H2 database and the user will be notified with the ID.

Use case very simple and not functional, but the idea was to inject an spring annotated JPA repository within the cometD annotated @service 

Hope it help.

## Versions

- Java: 1.8
- Spring boot: 2.3.2.RELEASE
- Cometd: 5.0.1
- jetty: 9.4.30.v20200611

All the credit goes to CometD team and Glenn Thompson
