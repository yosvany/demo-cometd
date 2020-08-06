FROM anapsix/alpine-java:8
EXPOSE 8080
ENV JAVA_OPTS="-server -XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=50.0 -Djava.security.egd=file:/dev/./urandom"
ADD target/big-cometd-1.0.0.jar /app.jar
ENTRYPOINT exec  java $JAVA_OPTS -jar /app.jar
