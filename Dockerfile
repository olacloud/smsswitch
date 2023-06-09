FROM openjdk:17
ADD target/smsswitch-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar
EXPOSE 2775
ENTRYPOINT ["java","-jar","app.jar"]
