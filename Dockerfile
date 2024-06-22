FROM bellsoft/liberica-runtime-container:jdk-21-slim-musl
COPY target/my-activities-*.jar /opt/app/app.jar
EXPOSE 8080
CMD ["java", "-showversion", "-jar", "/opt/app/app.jar"]