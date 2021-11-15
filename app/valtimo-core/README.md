###Valtimo-

This is a Valtimo for local development.

CLone external git repo https://github.com/valtimo-platform/valtimo-docker-profiles

####1 Docker-compose up

Run in terminal:
<code>valtimo-docker-profiles/valtimo-platform% docker-compose up</code>

####2 Run Spring-boot-application

Run in IntellJ Gradle: <code>app -> valtimo -> application -> bootRun</code>

Or

Run in terminal: <code>app/valtimo% ../../gradlew bootRun</code>

---

## Keycloak - Test users

Keycloak management can be accessed on http://localhost:8082 with the default credentials of username <ins>admin</ins> and password <ins>admin</ins>.

Keycloak comes preconfigured with the following users. 

| Name | Role | Username | Password |
|---|---|---|---|
| James Vance | ROLE_USER | user | user |
| Asha Miller | ROLE_ADMIN | admin | admin |
| Morgan Finch | ROLE_DEVELOPER | developer | developer |