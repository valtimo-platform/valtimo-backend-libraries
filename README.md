# Welcome at Valtimo

### What is Valtimo?
Valtimo is the low-code platform for Business Process Automation. Our goal is to make implementing business process automation and case management easy.

### What does the Valtimo platform contain?
- Valtimo consists of two services: 
  - A Spring Boot Java/Kotlin backend
  - An Angular frontend
- Valtimo depends on two services:
  - Keycloak as an identity and access provider
  - A database (default is PostgreSQL)

<img src="images/valtimo-platform.png" width="400" alt="Valtimo platform"/>

### What are the 'Valtimo backend libraries'?
This repository contains:
- A collection of Java/Kotlin libraries that together form the Valtimo backend
- The `app:gzac` module, containing a Spring Boot application, used for library development

### Running the Valtimo libraries from source
Starting up the Valtimo platform required three steps:
1. Starting the supporting Docker containers: Keycloak and PostgreSQL. Instructions for starting the supporting services can be found [here](app/gzac/README.md#start-docker-containers-for-supporting-services).
2. Starting the Valtimo backend. Instructions can be found [here](app/gzac/README.md#run-spring-boot-application).
3. Starting the Valtimo frontend. Instructions can be found in the README of the [frontend libraries repository](https://github.com/valtimo-platform/valtimo-frontend-libraries)

### Contributing
Contributions are welcome! To get you in the right direction, please consult the [Valtimo documentation](https://docs.valtimo.nl/readme/contributing) for guidelines on how to contribute.

### License
The source files in this repo are licensed under the [EUPL 1.2](https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12). 
If you have any questions about the use of this codebase in a larger work: please reach out through the [Valtimo website](https://www.valtimo.nl/contact/).

### More information
- Website: https://www.valtimo.nl
- Documentation: https://docs.valtimo.nl
