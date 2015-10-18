FROM java:8-jre

MAINTAINER Reto Hablützel <rethab@rethab.ch>

RUN mkdir /opt/app
ADD target/cb-ctt-1.0-SNAPSHOT-jar-with-dependencies.jar /opt/app/cb-ctt
ADD src/test/resources/comp01.ectt /opt/app/comp01.ectt

WORKDIR /opt/app

CMD ["java", "-jar", "/opt/app/cb-ctt", "/opt/app/comp01.ectt"]
