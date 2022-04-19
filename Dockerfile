FROM openjdk:14

RUN mkdir /usr/src/application
RUN mkdir /usr/src/application/calibration

COPY build/libs/ECTSignalSoft.jar /usr/src/application
COPY src/main/resources/calibration  /usr/src/application/calibration

WORKDIR /usr/src/application
ENV DOCKER_ENV  /usr/src/application/calibration

EXPOSE 8080
CMD ["java", "-jar", "ECTSignalSoft.jar"]