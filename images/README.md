# OSS: images
This directory contains a Dockerfile for building an OSS web service docker container, and a ModelRMILauncher docker container. We may use these containers to replace the oss machine and as a basis to replace/upgrade the web-services on ltproxy.

## Deployment

### OSS web service docker

#### Building

To build a OSS web service docker container do the following (where the oss software repository is installed at /home/cjm/eclipse-workspace/oss) :

* **cd /home/cjm/eclipse-workspace/oss/images** (i.e. this directory)
* **./provision_web_service** This copies the web service config file, and war file, into this directory for the docker build. You may want to edit the osswebservice.properties.docker file to specify a different RMI end-point (the machine on which the ModelRMILauncher lives).
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
* Edit the **provision_model_rmi_launcher** script, and the **model_rmi_launcher** docker file, and select the correct mysql-connector (which supplies the com.mysql.jdbc.Driver driver) for the mysql database you are trying to connect to.
* **./provision_model_rmi_launcher** Copy the latest build of the relevant packages into this directory, for the docker build file to pick up.
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
* **docker build -f model_rmi_launcher -t model_rmi_launcher_image .** Build the docker container from the **model_rmi_launcher** file. Note this dockerfile now expects the $RMIREGISTRY_HOST environment variable to be defined to specify the host IP with the rmiregistry server running on it.
* **docker save -o model_rmi_launcher_image.tar model_rmi_launcher_image** Save the constructed docker container into the **model_rmi_launcher_image.tar** tarball.

#### Running

To run the docker image try:
Lots of RMI ports here
* **docker run -itd -p 1100:1100 -p 1101:1101 -p 1102:1102 -p 1103:1103 -p 1104:1104 -p 1105:1105 --name model-rmi-launcher model_rmi_launcher_image**


### Full system deployment

We have not quite worked out how to deploy the ModelRMILauncher, oss webservice, and the rmiregistry (which the ModelRMILauncher exposes it's Phase2 Model to, and the OSS webservice invokes the model by looking up the ModelRMILauncher exposed services).

There is a docker compose file **compose.yaml** that hopefully builds and deploys the 3 services, using the internal docker network to handle the RMI connections (whilst still exporting the webservice tomcat port, and the RMI registry port (for debugging purposes)). To run this, try:

**docker compose up**

Note this won't work for an LT deployment, where the OSS webservice needs to be on a DMZ machine, and the ModelRMILauncher on a TLAN machine.

