/** used to update database related to Phase2/OSS system **/
/** run after update_database_1.sql to update to latest format **/

/** e.g.:
	mysql -u root -p < update_database_2.sql
**/

/*********** START ***********/

USE phase2odb;

CREATE TABLE `EA_SLEW` (
  `id` int NOT NULL auto_increment,
  `targetRef` int default NULL,						/** the id of the related TARGET entry **/
  `rotatorRef` int default NULL,					/** the id of the related ROTATOR_CONFIG**/
  `usesNonSiderealTracking` tinyint default NULL,	/** boolean **/
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/*****************************/
