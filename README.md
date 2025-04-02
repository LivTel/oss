# OSS

Source code for the Observer Support System (OSS). This contains source code for:
* The OSS web-service, that exposes a JibX interface for invoking the PhaseII database models. This sits on the Liverpool Telescope proxy machine.
* The ModelRMILauncher (ngat.oss.impl.mysql.ModelRMILauncher), that creates a series of Models that access the PhaseII database, and exposes their methods by registering them as RMI (Remote Method Invocation)  objects in the rmi registry. This process sits on the oss machine (where the underlying PhaseII mysql database runs).

The OSS web-service picks up the RMI objects by querying the rmiresistry, and then invokes methods within them. The underlying implementation of the models then create SQL calls to communicate with the underlying database.

 
## ModelRMILauncher

The ModelRMILauncher (ngat.oss.impl.mysql.ModelRMILauncher), that creates a series of Models that access the PhaseII database, and exposes their methods by registering them as RMI (Remote Method Invocation) objects in the rmi registry. This process sits on the oss machine (where the underlying PhaseII mysql database runs).

The access models are defined in the [model interface](WEB-INF/src/ngat/oss/model/) directory. The following access model interfaces exist:

* IAccessModel  (IAccessModel.java)
* IAccountModel (IAccountModel.java)
* IHistoryModel (IHistoryModel.java)
* ILockingModel (ILockingModel.java)
* IPhase2Model  (IPhase2Model.java)
* IStatusModel  (IStatusModel.java)

The model's are implmented in the [model implementation](WEB-INF/src/ngat/oss/impl/mysql/model) directory.

The implementation of those models makes calls to various accessors defined [here](WEB-INF/src/ngat/oss/impl/mysql/accessors)

and those accessors make the actual SQL calls into the database.
