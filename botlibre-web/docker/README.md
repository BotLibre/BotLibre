# About

This folder contains the required Dockerfiles for building the docker container (paphussolutions/botlibre).

A docker-compose file is also included for running the containers. If the required containers are all available online in a Docker registry, then only the docker-compose file is necessary for running the containers.

The containers can be run on any Linux machine with Docker and Docker Compose installed. They can also be used on Mac and Windows machines with Docker installed (where the containers will be run on a virtual machine).

The containers required for the application to work are:
* botlibre
* postgres:9.4

# How to Build

## botlibre

Before building the .war file, ensure that the code is correctly configured for deploying as `ROOT.war` (i.e. the URLs in `Site.java` are set to use `localhost` instead of `localhost:8080/botlibre`, and `SDK.APP` in `sdk.js` is set to `""`).

In addition to these changes, the following changes must be made to `Site.java`: the database host needs to be `"app-db"` instead of `"localhost"`, and the Python server URL needs to be `"http://app-py:6777/"` instead of `"http://localhost:6777/"`).

Perform the Ant build, and save the .war file in the `docker/botlibre` directory as `ROOT.war`.

To build the botlibre-enterprise container using the Dockerfile, run the following command from the botlibre-enterprise directory:
\
`$ sudo docker build -it paphussolutions/botlibre .`

# How to Run

Before running, ensure that Docker and Docker Compose are working.

To start the containers using Docker Compose, run the following command from the same directory as the `docker-compose.yml` file:
\
`$ sudo docker-compose up`
