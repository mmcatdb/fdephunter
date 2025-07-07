FROM maven:3.8.3-openjdk-17

WORKDIR /app

COPY . .

# The configuration file has to be in the directory from which we run the `java` command. 

RUN mvn clean package -DskipTests

CMD [ "java", "-jar", "server/target/server-BETA.jar" ]
