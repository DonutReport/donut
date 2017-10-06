FROM openjdk:9
LABEL maintainer="tim.myerscough@mechanicalrock.io"

ENV DONUT_VERSION 1.0

WORKDIR /app

RUN wget http://repo1.maven.org/maven2/io/magentys/donut/${DONUT_VERSION}/donut-${DONUT_VERSION}-one-jar.jar /app/donut.jar

ENTRYPOINT ["java", "-jar", "donut.jar", "-o", "/output"]
