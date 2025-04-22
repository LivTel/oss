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
* **cp /home/dev/bin/javalib/ngat_astrometry.jar .** Copy the latest build of the ngat astrometry jar (used by ngat.phase2) into this directory.
* **cp /home/dev/bin/javalib/ngat_new_oss.jar .** Copy the latest build of the oss jar into this directory.
* **cp /home/dev/bin/javalib/ngat_new_icm.jar .** Copy the latest build of the icm (instrument capabilities and monitoring) jar into this directory.
* **cp /home/dev/bin/javalib/ngat_new_phase2.jar .** Copy the latest build of the oss jar into this directory.
* **cp /home/dev/bin/javalib/ngat_new_tcm.jar .** Copy the latest build of the tcm (telescope capabilities and monitoring) jar into this directory.
* **cp /home/dev/bin/javalib/ngat_util.jar .** Copy the latest build of the ngat util jar into this directory.
* **cp /home/dev/bin/javalib_third_party/log4j-1.2.13.jar .** Copy the latest build of the oss jar into this directory.
* **cp /home/dev/bin/javalib_third_party/mysql-connector-java-3.1.12-bin.jar .** Copy the mysql connector (suppl;ies the com.mysql.jdbc.Driver driver) into this directory.
* Create a config file **oss.properties.docker** in the **/home/cjm/eclipse-workspace/oss/images** directory containing the following:
```
# This file exists as /oss/oss/config/oss.properties
# on the machine running the oss rmi objects and database connections

accessmodel.rmi.objectname=AccessModel
proposalaccountmodel.rmi.objectname=ProposalAccountModel
useraccountmodel.rmi.objectname=UserAccountModel
tagaccountmodel.rmi.objectname=TagAccountModel
historymodel.rmi.objectname=HistoryModel
lockingmodel.rmi.objectname=LockingModel
phase2model.rmi.objectname=Phase2Model

accessmodel.rmi.port=1100
proposalaccountmodel.rmi.port=1101
useraccountmodel.rmi.port=1102
tagaccountmodel.rmi.port=1103
historymodel.rmi.port=1104
lockingmodel.rmi.port=1105
phase2model.rmi.port=1106

database.host=<hostname/ip address>
database.db=phase2odb
database.user=<username>
database.password=<password>
```
* **cp /home/cjm/eclipse-workspace/oss/resources/security/policy.dat .** Copy the security policy config file into this directory.
* **docker build -f model_rmi_launcher -t model_rmi_launcher_image .** Build the docker container from the **model_rmi_launcher** file.
* **docker save -o model_rmi_launcher_image.tar model_rmi_launcher_image** Save the constructed docker container into the **model_rmi_launcher_image.tar** tarball.

#### Running

To run the docker image try:
Lots of RMI ports here
* **docker run -itd -p 1100:1100 -p 1101:1101 -p 1102:1102 -p 1103:1103 -p 1104:1104 -p 1105:1105 --name model-rmi-launcher model_rmi_launcher_image**
