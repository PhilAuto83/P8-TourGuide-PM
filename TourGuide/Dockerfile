# dockerfile to build tourguide project PM image
FROM eclipse-temurin:21
MAINTAINER Philip Mercan
COPY /target/tourguide-1.1.0.jar tourguide-1.1.0.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "/tourguide-1.1.0.jar"]