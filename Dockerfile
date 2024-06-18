FROM maven:3-eclipse-temurin-17 as build
RUN mkdir /usr/src/project
COPY . /usr/src/project
WORKDIR /usr/src/project
RUN mvn package -DskipTests
RUN jar xf target/my-activities-*.jar
RUN jdeps --ignore-missing-deps -q  \
    --recursive  \
    --multi-release 17  \
    --print-module-deps  \
    --class-path 'BOOT-INF/lib/*'  \
    target/my-activities-*.jar > deps.info
RUN jlink \
    --add-modules $(cat deps.info) \
    --strip-debug \
    --compress 2 \
    --no-header-files \
    --no-man-pages \
    --output /myjre
FROM debian:bookworm-slim
ENV JAVA_HOME /user/java/jdk17
ENV PATH $JAVA_HOME/bin:$PATH
COPY --from=build /myjre $JAVA_HOME
RUN mkdir /project
COPY --from=build /usr/src/project/target/my-activities-*.jar /project/
WORKDIR /project
EXPOSE 8080
ENTRYPOINT java -jar my-activities-*.jar