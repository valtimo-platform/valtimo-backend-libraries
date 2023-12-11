
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

The environment consists of 3 components. The links below contain instructions on how to run each component:

* [Docker containers for supporting services](app/gzac/README.md#start-docker-containers-for-supporting-services)
* [Valtimo backend](app/gzac/README.md#run-spring-boot-application)
* [Valtimo frontend](https://github.com/valtimo-platform/valtimo-frontend-template/blob/main/README.md)
