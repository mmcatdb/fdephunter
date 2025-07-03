# FDepHunter Backend

## Installation

- Copy the `server/src/main/resources/default.properties` file to `server/src/main/resources/application.properties` file and adjust the settings.
- Create MongoDB container:
```bash
docker run -d -e MONGO_INITDB_ROOT_USERNAME=user -e MONGO_INITDB_ROOT_PASSWORD=password -p 5602:27017 --name=mongodb mongo
```

- Start development server (with hot reload):
```bash
./dev.sh
```