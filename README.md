[![Build status](https://github.com/navikt/syfoservice-data-syfosmregister/workflows/Deploy%20to%20dev%20and%20prod/badge.svg)](https://github.com/navikt/syfoservice-data-syfosmregister/workflows/Deploy%20to%20dev%20and%20prod/badge.svg)
# syfoservice-data-syfosmregister
Application for move over data from syfoservice to syfosmregister

## Technologies used
* Kotlin
* Ktor
* Gradle
* Spek
* Vault
* Oracle DB

#### Requirements

* JDK 11

## Getting started
#### Running locally
`./gradlew run`

#### Build and run tests
To build locally and run the integration tests you can simply run `./gradlew shadowJar` or on windows 
`gradlew.bat shadowJar`

#### Creating a docker image
Creating a docker image should be as simple as `docker build -t syfoservice-data-syfosmregister .`

#### Running a docker image
`docker run --rm -it -p 8080:8080 syfoservice-data-syfosmregister`

## Contact us
### Code/project related questions can be sent to
* Joakim Kartveit, `joakim.kartveit@nav.no`
* Andreas Nilsen, `andreas.nilsen@nav.no`
* Sebastian Knudsen, `sebastian.knudsen@nav.no`
* Tia Firing, `tia.firing@nav.no`

### For NAV employees
We are available at the Slack channel #team-sykmelding