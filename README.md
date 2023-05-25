
## What is Valtimo?
Welcome at the Valtimo repository. Valtimo is the less-code platform for Business Process Automation. Built on top of Camunda. Our goal is to make implementing business process automation and case management easy.

## What are the 'Valtimo backend libraries'?
The backend, being the Java / Kotlin application. Install the Valtimo frontend as well if you need the UI.

## When to use it?
When you work with Cases. Typical use cases are in handling requests from citizens (governmental use) or customers (commercial). But also for internal processes like employee onboarding or production processes. There is a special Edition for Dutch governmental use, 'GZAC'. 

Key ingredients: internal and external users involved, need for monitor progress, need to audit for compliance reasons, demand for further automation: the process is too much to handle properly with Excel / mail.

## When not to use it?
When you want to do pure service orchestration. Camunda and Zeebe are your better options.

## Low code = no code?
No - it's with code. Not too much though - you don't have to have 20 years of Java development experience to get it running. There is functionality for administrators to build and maintain processes via the UI.

## I'm missing functionality like transactional mail or document generation
We're not planning to build the next all-in-one monolith. The idea is to connect to best-of-breed microservices and task applications. So connect with your favourite transactional email service - inhouse or SaaS - whatever works bests for you.

## GZAC edition: how does this fit the 5-layers model from the Dutch Common Ground initiative?
This is typically layer-4 - so process and business logic. In itself the application is a layered model as well (it has a database and a UI), as any other application. Also relevant to know is that Valtimo does not cover Formflow at the moment. We are working on a standard available integration with Open Zaak initiative (layer 1/2). For the future we are also thinking to add more default integrations like IRMA.

## Contributing
Contributions are welcome! To get you in the right direction consult the [Valtimo documentation](https://docs.valtimo.nl/readme/contributing) for guidelines on how to contribute.

## License
The source files in this repo are licensed to you under the EUPL 1.2. You can download the license in 23 languages: https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12. If you have any questions about the use of this codebase in a larger work: just ask us.

## More information about Valtimo
For more information check the following links.
- Website: https://www.valtimo.nl
- Documentation: https://docs.valtimo.nl

## Getting started
This is a step by step guide to setup a local Valtimo backend environment with **Spring Boot** and **IntelliJ**.

* ⚠️ This repository is meant for development/contribution to the Valtimo platform. If you are looking for how to run a Valtimo instance locally, please consult the [public guide](https://docs.valtimo.nl/getting-started/first-dive/creating-your-own-valtimo-implementation).
 
* ⚠️ There are two editions of Valtimo: ‘Valtimo-core’ and ‘GZAC’. The ‘GZAC’ edition is a special Valtimo implementation meant for government organizations. At the moment of publication, this guide only works for the ‘GZAC’ Valtimo development environment. This due to a problem with the docker containers for the ‘Valtimo’ edition. For this reason, this guide is ‘GZAC’ focused.

### Prerequisites:

- [Git](https://git-scm.com/downloads)
- [IntelliJ](https://www.jetbrains.com/idea/download/)
- [Docker (Desktop)](https://www.docker.com/products/docker-desktop/)
- [NPM Package manager / NodeJS (version 16 or higher)](https://nodejs.org/en/download/)
 
### Setup the Valtimo environment:
The environment consists of 3 components:
* Docker containers for supporting services
* Valtimo backend
* Valtimo frontend

#### - Docker:
1. Make sure docker is running.
2. Open IntelliJ and create a new project from Version Control. Use the code URL from the **valtimo-docker-profiles** repository: [Valtimo docker github page](https://github.com/valtimo-platform/valtimo-docker-profiles).
3. Open the terminal, go to the `gzac-platform` folder and run command 
    ```
    docker compose up
    ``` 
    ⚠️ Note that the first time it takes quite a while to pull the required images. Some containers start up and end after a short while, this is on purpose.

#### - Valtimo Backend:
1. In IntelliJ create a new project from Version Control. Use the code url from the **Valtimo-backend-libraries** repository [Valtimo backend github page](https://github.com/valtimo-platform/valtimo-backend-libraries).
2. Open the `Gradle` view. Click the `Refresh` and the `hammer` icon to build the project
3. Then run the `Gradle` bootRun for your desired edition (for the gzac edition this is app/gzac/application/bootRun)\
⚠️ Note: use the same one as in the docker profiles repo:
    * If you use `gzac` start `gzac` bootrun 
    * If you used `valtimo-core` start `valtimo-core` bootrun

#### - Valtimo Frontend:
1. In IntelliJ create a new project from Version Control. Use the code url from the **Valtimo-frontend-libraries** repository [Valtimo frontend github page](https://github.com/valtimo-platform/valtimo-frontend-libraries).
2. Open the terminal and run the commands:
    ```
    nvm use 16
    npm install 
    ```
3. Now build all the libraries with:
    ```
    npm run libs-build-all
    ``` 
5. Start the frontend with 
    ```
    npm start
    ``` 

The application runs on [localhost:4200](http://localhost:4200), the credentials (login/password) are **admin** for both fields.