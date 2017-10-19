/** used to update database related to Phase2/OSS system **/
/** run after update_database_2.sql to update to latest format **/

/** e.g.:
	mysql -u root -p < update_database_9.sql
**/

/*********** START ***********/

USE phase2odb;

CREATE TABLE `INST_CONFIG_SPEC_IMAGER` (
  `id` int NOT NULL auto_increment,
  `grismPos` tinyint default NULL,
  `grismRot` tinyint default NULL,
  `slitPos` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/*****************************/
