# Deeds digital certificate generation

This project's purpose is to generate a digital version of the deeds certificates.

### Technologies used

The document generation is achieved using **iTextPdf** library.

### Running the project

The project is implemented using SpringBoot with maven as the build mechanism.
To build and run it, the following commands can be executed in its root directory:

* Building: `mvn clean install`
* Running: `mvn spring-boot:run`

The project is configured to run on port `9500` which can be changed
in the `application.properties` file.

--------

The project can also run using docker, here are the steps:

* Building: `mvn clean install`
* Building docker image: `docker build -t springio/digital-deeds-certificate .`
* Running the docker image: `docker run -d --name digital-deeds-certificate -p 9500:9500 springio/digital-deeds-certificate`

Then the certificate can be accessed through this API:

`
curl -X GET {{hostname}}:9500/generate-digital-deed
`
