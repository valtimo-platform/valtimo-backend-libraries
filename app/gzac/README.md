# GZAC

This is a GZAC - Valtimo edition for local development.

## Running the Valtimo libraries from source

The following instructions are for running Valtimo from source code. Usually with the intention of improving the Valtimo
libraries. If you want to get started with Valtimo for you own application, please consult
the [getting started guide](https://docs.valtimo.nl/getting-started/first-dive/creating-your-own-valtimo-implementation).

### Prerequisites:

- Java 17
- [Git](https://git-scm.com/downloads)
- An IDE like [IntelliJ](https://www.jetbrains.com/idea/download/) or [Eclipse](https://www.eclipse.org/downloads/)
- [Docker (Desktop)](https://www.docker.com/products/docker-desktop/)

### Start docker containers for supporting services

Make sure docker is running.

Run IntellJ Gradle task <code>app -> gzac -> Tasks -> docker -> composeUpGzac</code>.

Or

Open a terminal and run command
```
./gradlew :app:gzac:composeUpGzac
``` 

### Run Spring-boot-application

Run in IntellJ Gradle task: <code>app -> gzac -> Tasks -> application -> bootRun</code>

Or

Open a terminal and run command
```
./gradlew :app:gzac:bootRun
```

## Keycloak - Test users

Keycloak management can be accessed on http://localhost:8081 with the default credentials of username <ins>admin</ins>
and password <ins>admin</ins>.

Keycloak comes preconfigured with the following users.

| Name         | Role           | Username  | Password  |
|--------------|----------------|-----------|-----------|
| James Vance  | ROLE_USER      | user      | user      |
| Asha Miller  | ROLE_ADMIN     | admin     | admin     |
| Morgan Finch | ROLE_DEVELOPER | developer | developer |
