# OSS: images
This directory contains a Dockerfile for building an OSS web service docker container, and a ModelRMILauncher docker container. We may use these containers to replace the oss machine and as a basis to replace/upgrade the web-services on ltproxy.

## Deployment

### OSS web service docker

To build a OSS web service docker container do the following (where the oss software repository is installed at /home/cjm/eclipse-workspace/oss) :

* **cd /home/cjm/eclipse-workspace/oss/images** (i.e. this directory)
* **cp /home/cjm/eclipse-workspace/oss/resources/config/osswebservice.properties.docker .** Copy the docker version of the osswebservice.properties file to this directory. You may want to edit the file to specify a different RMI end-point (the machine on which the ModelRMILauncher lives).
* **cp /home/dev/bin/javalib/ngat_new_oss.war .** Copy the latest build of the oss web-service war into this directory.
* **sudo docker build -f oss_web_service -t oss_web_service_image .** Build the docker container from the **oss_web_service** file.
* **docker save -o oss_web_service_image.tar oss_web_service_image** Save the constructed docker container into the **oss_web_service_image.tar** tarball.

The docker can then be installed / loaded into the target system as follows:
* **sudo docker load -i oss_web_service_image.tar**

To run the docker image try:
* **docker run -itd -p 8080:8080 --name oss-web-service oss_web_service_image**

