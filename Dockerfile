FROM openjdk:9
LABEL maintainer="tim.myerscough@mechanicalrock.io"


WORKDIR /app

COPY target/donut*.jar /app/donut.jar

ENTRYPOINT ["java", "-jar", "donut.jar", "-s", "cucumber:/source", "-o", "/output"]
