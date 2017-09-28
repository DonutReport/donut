FROM openjdk:9
LABEL maintainer="tim.myerscough@mechanicalrock.io"


WORKDIR /app

RUN wget http://repo1.maven.org/maven2/io/magentys/donut/1.0/donut-1.0-one-jar.jar /app/donut.jar

ENTRYPOINT ["java", "-jar", "donut.jar", "-s", "cucumber:/source", "-o", "/output"]
