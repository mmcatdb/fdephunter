# FDepHunter Backend

## Configuration

- Copy the configuration file (adjust the variables if necessary):
```bash
cp server/src/main/resources/default.properties server/src/main/resources/application.properties
```

## Installation

- Create MongoDB container (don't forget to edit the variables if you changed them in the previous step):
```bash
docker run -d -e MONGO_INITDB_ROOT_USERNAME=user -e MONGO_INITDB_ROOT_PASSWORD=password -p 5602:27017 --name=fdephunter-mongodb mongo
```
- Compile the project and start development server (with hot reload):
```bash
./compile.sh
cd server
mvn spring-boot:run
```
