/** used to update database related to Phase2/OSS system **/
/** run after update_database_2.sql to update to latest format **/

/** e.g.:
	mysql -u root -p < update_database_9.sql
**/

/*********** START ***********/

USE phase2odb;

CREATE TABLE `MINIMUM_CLIENT_VERSION` (
  `major` int default NULL,
  `minor` int default NULL,
  `revision` int default NULL,
  `minorRevision` int default NULL) ENGINE = InnoDB;

/*****************************/
