### Build Stage ###
FROM maven:3-openjdk-17-slim AS build
WORKDIR /opt/clipcollector/
ADD . .
RUN cp -f src/main/resources/application.properties.sample src/main/resources/application.properties
RUN mvn package -s settings.xml

### Run Stage ###
FROM openjdk:17-slim
WORKDIR /opt/clipcollector/
COPY --from=build /opt/clipcollector/target/ClipCollector.jar .

EXPOSE 8080
ENTRYPOINT ["java","-jar","ClipCollector.jar"]
