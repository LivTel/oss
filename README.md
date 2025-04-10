# OSS

Source code for the Observer Support System (OSS). This contains source code for:
* The OSS web-service, that exposes a JibX interface for invoking the PhaseII database models. This sits on the Liverpool Telescope proxy machine.
* The ModelRMILauncher (ngat.oss.impl.mysql.ModelRMILauncher), that creates a series of Models that access the PhaseII database, and exposes their methods by registering them as RMI (Remote Method Invocation)  objects in the rmi registry. This process sits on the oss machine (where the underlying PhaseII mysql database runs).

The OSS web-service picks up the RMI objects by querying the rmiresistry, and then invokes methods within them. The underlying implementation of the models then create SQL calls to communicate with the underlying database.


## OSS webservice

The OSS web-service is a tomcat web-service containder deployed on the proxy machine, that receives web-service calls from the Phase2UI and passses them onto the RMI models exposed by the ModelRMILauncher.

It can be built from within eclipse using the 'war' target. This generates the file '''/home/dev/bin/javalib/ngat_new_oss.war''' , which is copied into tomcat's webapp directory (ltproxy:/usr/local/tomcat/webapps/) to deploy the web-services. The web-services exposed for defined in the [web.xml](WEB-INF/web.xml) file and associated XML files.

An associated configuration file [osswebservice.properties](resources/config/osswebservice.properties.live) should be deployed to tomcats conf directory ltproxy:/usr/local/tomcat/conf/. This configures the RMI objectsname for each service and also what host the invoked RMI services live on. For the live system this is the oss machine in the TLAN, but for testing purposes a modified version of this configuration file can be deployed on another tomcat machine to invoke a test database rather than the real live one.

## ModelRMILauncher

The ModelRMILauncher ([ngat.oss.impl.mysql.ModelRMILauncher](WEB-INF/src/ngat/oss/impl/mysql/ModelRMILauncher.java)), that creates a series of Models that access the PhaseII database, and exposes their methods by registering them as RMI (Remote Method Invocation) objects in the rmi registry. This process sits on the oss machine (where the underlying PhaseII mysql database runs).

The access models are defined in the [model interface](WEB-INF/src/ngat/oss/model/) directory. The following access model interfaces exist:

* IAccessModel  (IAccessModel.java) Handles Users and their access permissions. Also how Users authenticate with the phase2 interface via the authenticate method.
* IAccountModel (IAccountModel.java) Handles the allocation of time to Proposals/Semesters.
* IHistoryModel (IHistoryModel.java) Handles history associated with the database (records of observations taken and observation failures).
* ILockingModel (ILockingModel.java) Enables locking of groups (i.e. we can't modify a group when it is executing).
* IPhase2Model  (IPhase2Model.java) This contains the methods for adding/modifying/deleting the programmes/proposals/groups/targets/instruments/constraints/observing sequences in the PhaseII database.
* IStatusModel  (IStatusModel.java) This provides a status interface - this seems mainly to do with getting rotator offsets for the science instruments mounted on the telescope.

The model's are implmented in the [model implementation](WEB-INF/src/ngat/oss/impl/mysql/model) directory.

The implementation of those models makes calls to various accessors defined [here](WEB-INF/src/ngat/oss/impl/mysql/accessors)

and those accessors make the actual SQL calls into the database. This is done via a Connection retrieved from the [ConnectionPool](WEB-INF/src/ngat/oss/impl/mysql/ConnectionPool.java), the singleton constructor of which initialises the connections using a JDBS connector with the URL  "jdbc:mysql://"+host+"/"+database +"?user="+user+"&password="+password; , where '''host''' , '''database''' , '''user''' and '''password''' are initialised from the OssProperties instance (and therefore the /oss/oss/config/oss.properties config file).

### ModelRMILauncher configuration

The ModelRMILauncher configures both the access model RMI objects, and the phase2 database connection, via a singleton instance of [OssProperties](WEB-INF/src/ngat/oss/configuration/OssProperties.java). This reads the properties file (hardcoded in OssProperties.java to be /oss/oss/config/oss.properties). This has the following format (there is no master copy in this repository as it contains database usernames/passwords):

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

database.host=oss
database.db=phase2odb
database.user=<username>
database.password=<password>
```

