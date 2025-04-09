# OSS: images
This directory contains a Dockerfile for building an OSS web service docker container, and a ModelRMILauncher docker container. We may use these containers to replace the oss machine and as a basis to replace/upgrade the web-services on ltproxy.

## Deployment

### OSS web service docker

#### Building

To build a OSS web service docker container do the following (where the oss software repository is installed at /home/cjm/eclipse-workspace/oss) :

* **cd /home/cjm/eclipse-workspace/oss/images** (i.e. this directory)
* **cp /home/cjm/eclipse-workspace/oss/resources/config/osswebservice.properties.docker .** Copy the docker version of the osswebservice.properties file to this directory. You may want to edit the file to specify a different RMI end-point (the machine on which the ModelRMILauncher lives).
* **cp /home/dev/bin/javalib/ngat_new_oss.war .** Copy the latest build of the oss web-service war into this directory.
* **docker build -f oss_web_service -t oss_web_service_image .** Build the docker container from the **oss_web_service** file.
* **docker save -o oss_web_service_image.tar oss_web_service_image** Save the constructed docker container into the **oss_web_service_image.tar** tarball.

Note the **oss_web_service** dockerfile currently defaults to using tomcat:9.0, there was apparently a API package name change for tomcat 10 (javax.servlet to jakarta.servlet) so using tomcat 10 or above should give the error **java.lang.NoClassDefFoundError: javax/servlet/ServletContextListener** with the current OSS codebase.

#### Loading / Installing

The docker can then be installed / loaded into the target system as follows:
* **docker load -i oss_web_service_image.tar**

#### Running

To run the docker image try:
* **docker run -itd -p 8080:8080 --name oss-web-service oss_web_service_image**

You can test the tomcat webapp is running, and can talk to the database, by trying the following test code URL in your browser:

* **http://&lt;host&gt;:8080/ngat_new_oss/ListAllGroups.jsp**

Not quite sure how much this loads the webapp though - use with care!

#### Reading the tomcat logs

* **docker ps**

Find the **oss-web-service** container id and then do the following:

* **docker logs &lt;container id&gt;**

With the tomcat container, **catalina.out** is dumped to stdout (and therefore docker logs).

Alternatively, get a shell login into the container:

* **docker exec -it &lt;container id&gt; /bin/bash**
* **cd /usr/local/tomcat/logs/**

To see the localhost and localhost_access_log s.

### ModelRMILauncher docker

#### Building


* **cd /home/cjm/eclipse-workspace/oss/images** (i.e. this directory)
* **docker build -f model_rmi_launcher -t model_rmi_launcher_image .** Build the docker container from the **model_rmi_launcher** file.
* **docker save -o model_rmi_launcher_image.tar model_rmi_launcher_image** Save the constructed docker container into the **model_rmi_launcher_image.tar** tarball.

#### Running

To run the docker image try:
Lots of RMI ports here
* **docker run -itd -p 1100:1100 -p 1101:1101 -p 1102:1102 -p 1103:1103 -p 1104:1104 -p 1105:1105 --name model-rmi-launcher model_rmi_launcher_image**
