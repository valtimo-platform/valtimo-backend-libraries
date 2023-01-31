### GZAC

This is a GZAC - Valtimo edition for local development.
Includes an array of additional services for OpenZaak/OpenFormulieren

Clone external git repo https://github.com/valtimo-platform/valtimo-docker-profiles

#### 1 Docker-compose up

Run in terminal:
<code>valtimo-docker-profiles/gzac-platform% docker-compose up</code>

#### 2 Run Spring-boot-application

Run in IntellJ Gradle: <code>app -> gzac -> application -> bootRun</code>

Or

Run in terminal: <code>app/gzac% ../../gradlew bootRun</code>

---

## Keycloak - Test users

Keycloak management can be accessed on http://localhost:8082 with the default credentials of username <ins>admin</ins> and password <ins>admin</ins>.

Keycloak comes preconfigured with the following users. 

| Name | Role | Username | Password |
|---|---|---|---|
| James Vance | ROLE_USER | user | user |
| Asha Miller | ROLE_ADMIN | admin | admin |
| Morgan Finch | ROLE_DEVELOPER | developer | developer |

### Objects API 

Admin can be accessed on http://localhost:8000.

First docker-compose up or just bootRun the app

Terminal:
- <code>docker-compose exec objects-api src/manage.py createsuperuser</code>  

To add demo data:

Terminal:
- <code>docker-compose exec objects-api src/manage.py loaddata demodata</code>

Create a token for the access under Home › API authorizations > Token authorizations and use this in the connector config


### ObjectTypes API

Admin can be accessed on http://localhost:8001.

First docker-compose up or just bootRun the app

Terminal:
- <code>docker-compose exec objecttypes-api src/manage.py createsuperuser</code>

To add demo data:

Terminal:
- <code>docker-compose exec objecttypes-api src/manage.py loaddata demodata</code>

Create a token for the access under Home › API authorizations > Token authorizations and use this in the connector config


### Open Notificaties

Admin can be accessed on http://localhost:8002.

By default an admin user is created with the following credentials

>Username: admin  
>Password: admin
