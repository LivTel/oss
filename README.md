# OSS

Source code for the Observer Support System (OSS). This contains source code for:
* The OSS web-service, that exposes a JibX interface for invoking the PhaseII database models. This sits on the Liverpool Telescope proxy machine.
* The ModelRMILauncher (ngat.oss.impl.mysql.ModelRMILauncher), that creates a series of Models that access the PhaseII database, and exposes their methods by registering them as RMI (Remote Method Invocation)  objects in the rmi registry. This process sits on the oss machine (where the underlying PhaseII mysql database runs).

The OSS web-service picks up the RMI objects by querying the rmiresistry, and then invokes methods within them. The underlying implementation of the models then create SQL calls to communicate with the underlying database.

 
## ModelRMILauncher

The ModelRMILauncher (ngat.oss.impl.mysql.ModelRMILauncher), that creates a series of Models that access the PhaseII database, and exposes their methods by registering them as RMI (Remote Method Invocation) objects in the rmi registry. This process sits on the oss machine (where the underlying PhaseII mysql database runs).

The access models are defined in the [model interface](WEB-INF/src/ngat/oss/model/) directory. The following access model interfaces exist:

* IAccessModel  (IAccessModel.java) Handles Users and their access permissions. Also how Users authenticate with the phase2 interface via the authenticate method.
* IAccountModel (IAccountModel.java) Handles the allocation of time to Proposals/Semesters.
* IHistoryModel (IHistoryModel.java) Handles history associated with the database (records of observations taken and observation failures).
* ILockingModel (ILockingModel.java) Enables locking of groups (i.e. we can't modify a group when it is executing).
* IPhase2Model  (IPhase2Model.java) This contains the methods for adding/modifying/deleting the programmes/proposals/groups/targets/instruments/constraints/observing sequences in the PhaseII database.
* IStatusModel  (IStatusModel.java) This provides a status interface - this seems mainly to do with getting rotator offsets for the science instruments mounted on the telescope.

The model's are implmented in the [model implementation](WEB-INF/src/ngat/oss/impl/mysql/model) directory.

The implementation of those models makes calls to various accessors defined [here](WEB-INF/src/ngat/oss/impl/mysql/accessors)

and those accessors make the actual SQL calls into the database.
