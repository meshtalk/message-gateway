FROM openjdk:8-jdk-stretch AS build

RUN DEBIAN_FRONTEND=noninteractive apt-get update && \
    DEBIAN_FRONTEND=noninteractive apt-get -y upgrade && \
    DEBIAN_FRONTEND=noninteractive apt-get -y install maven

COPY . /src/
WORKDIR /src
RUN mvn clean install -DskipTests

FROM openjdk:8-jdk-alpine
WORKDIR /root/
COPY --from=build /src/target/*jar-with-dependencies.jar ./message-gateway.jar
RUN chmod +x message-gateway.jar
CMD ["java", "-jar", "message-gateway.jar"]
