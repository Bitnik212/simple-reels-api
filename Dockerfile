FROM alpine:latest AS runner

WORKDIR /app

RUN apk add openjdk17-jre

COPY ./build/libs/*-all.jar /app/ktor-docker.jar

EXPOSE 8080/tcp

CMD ["java","-jar","/app/ktor-docker.jar"]
