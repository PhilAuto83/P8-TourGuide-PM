# dockerfile to build tourguide project PM image
FROM eclipse-temurin:21
MAINTAINER Philip Mercan
COPY /target/*.jar tourguide.jar
ENTRYPOINT ["java", "-jar", "/tourguide.jar"]