# Project 8: TourGuide by Philip Mercan

## Technologies

> Java 21  
> Spring Boot 3.X  
> JUnit 5  

## How to have gpsUtil, rewardCentral and tripPricer dependencies available ?

> Run : 
- mvn install:install-file -Dfile=/libs/gpsUtil.jar -DgroupId=gpsUtil -DartifactId=gpsUtil -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/RewardCentral.jar -DgroupId=rewardCentral -DartifactId=rewardCentral -Dversion=1.0.0 -Dpackaging=jar  
- mvn install:install-file -Dfile=/libs/TripPricer.jar -DgroupId=tripPricer -DartifactId=tripPricer -Dversion=1.0.0 -Dpackaging=jar

You can now add the dependencies in your pom.xml as the external apps are now downloaded locally in your .m2 repository and added to your external libraries in your project

## Build the image from docker file
_**Prerequisites : you should have docker desktop installed**_

Go to the root directory where the Dockerfile is located and run the following command :
`docker build -t pm/tourguide:1.1.0 .`

## Run the image in a docker container
To reach the container where tomcat is running on port 8080, you need to specify the mapping between docker container and tomcat running in it
- run the following command to start container with the image built previously : `docker run -p 9000:8080 pm/tourguide:1.1.0`
- on your local machine, open a browser and type http://localhost:9000

