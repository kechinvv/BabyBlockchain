FROM gradle:7.6-jdk11-alpine as builder
USER root
WORKDIR /builder
ADD . /builder
RUN ["gradle", "clean","MyFatJar"]

FROM openjdk:11
WORKDIR /bot
COPY --from=builder /builder/build/libs/BabyBlockchain-0.0.1.jar .
ENTRYPOINT ["java", "-jar", "BabyBlockchain-0.0.1.jar"]