ARG REPO_PORT_DEFAULT=8080

FROM eclipse-temurin:17-alpine AS ro-crate-rest-build
LABEL stage=build

COPY . .
RUN ./gradlew build --stacktrace 

RUN cp build/libs/ro-crate-rest-0.0.1-SNAPSHOT.jar ./ro-crate-rest-0.0.1-SNAPSHOT.jar

FROM eclipse-temurin:17-jdk-alpine as ro-crate-rest
LABEL stage=run

ENV REPO_PORT=${REPO_PORT_DEFAULT}

COPY --from=ro-crate-rest-build ./ro-crate-rest*.jar ./ro-crate-rest.jar

EXPOSE ${REPO_PORT}

CMD java -jar ro-crate-rest.jar