/** used to update database related to Phase2/OSS system **/
/** run after update_database_2.sql to update to latest format **/

/** e.g.:
	mysql -u root -p < update_database_17.sql
**/

/*********** START ***********/

USE phase2odb;

CREATE TABLE `INST_CONFIG_SPEC_TWO_SLIT` (
  `id` int NOT NULL auto_increment,
  `slitWidth` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/*****************************/
