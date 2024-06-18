# My Activities API
The My Activities API is a Spring Boot application designed to provide an interface for managing your upcoming activities.

Used with `https://github.com/JonathanD01/my-activities-frontend`

## Requirements
* JDK 17
* Maven
* Docker (for containerization)

## Getting started

1. Clone the repository
```
git clone https://github.com/JonathanD01/my-activities-api.git
```

2. Build the project
```
cd my-activities-api
mvn clean package
```

3. Run the Application
```
java -jar target/my-activities.jar
```

4. Access the API
   Once the application is running, you can access the API at http://localhost:8080/api/v1/activities.
    Authentication is also required. You can access that API from http://localhost:8080/api/v1/auth.

## Configuration
* **Application configuration**: Configure the application in `application.properties`.

## TODO
- Add comments
- ~~Add tests~~