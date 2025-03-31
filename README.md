# OSS

Source code for the Observer Support System (OSS). This contains source code for:
* The OSS web-service, that exposes a JibX interface for invoking the PhaseII database models. This sits on the Liverpool Telescope proxy machine.
* The ModelRMILauncher (ngat.oss.impl.mysql.ModelRMILauncher), that creates a series of Models that access the PhaseII database, and exposes their methods by registering them as RMI (Remote Method Invocation)  objects in the rmi registry. This process sits on the oss machine (where the underlying PhaseII mysql database runs).

The OSS web-service picks up the RMI objects by querying the rmiresistry, and then invokes methods within them. The underlying implementation of the models then create SQL calls to communicate with the underlying database.

 
