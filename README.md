
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

## Running the Valtimo libraries from source

⚠️ The following instructions are for running Valtimo from source code. Usually with the intention of development/contribution to the Valtimo platform. If you only want to run a Valtimo implementation, please consult the [getting started guide](https://docs.valtimo.nl/getting-started/first-dive/creating-your-own-valtimo-implementation).

### Prerequisites:

- Java 17
- [Git](https://git-scm.com/downloads)
- An IDE like [IntelliJ](https://www.jetbrains.com/idea/download/) or [Eclipse](https://www.eclipse.org/downloads/)
- [Docker (Desktop)](https://www.docker.com/products/docker-desktop/)
 
### Setup the Valtimo environment:
The environment consists of 3 components:
* [Docker containers for supporting services](https://github.com/valtimo-platform/valtimo-docker-profiles)
* Valtimo backend (this repository)
* [Valtimo frontend](https://github.com/valtimo-platform/valtimo-frontend-libraries/)

#### - Docker containers for supporting services:
1. Make sure docker is running.
2. Clone the Git repository **valtimo-docker-profiles** repository: [Valtimo docker git repository](https://github.com/valtimo-platform/valtimo-docker-profiles).
3. Open a terminal, go to the `valtimo-platform` folder and run command
    ```
    docker compose up -d
    ``` 

#### - Valtimo Backend:
1. Make sure this repository (the one where this README is a part of) is cloned to your workstation  
2. The application can be started with the gradle task ```:app:valtimo-core:bootRun```. 

The Valtimo backend api is now available at http://localhost:8080 . In order to work with it, you will need a running frontend implementation. See the next step.

#### - Valtimo Frontend:

The Git repository for running a frontend implementation can be found here: [Valtimo frontend template](https://github.com/valtimo-platform/valtimo-frontend-template). The repository includes the instructions for running the frontend. 


