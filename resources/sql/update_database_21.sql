/** used to update database related to Phase2/OSS system **/
/** run after update_database_20.sql to update to latest format **/

/** e.g.:
	mysql -u oss -p<password> < update_database_21.sql
**/

/*********** START ***********/
USE phase2odb;

CREATE TABLE `INST_CONFIG_RAPTOR` (
  `id` int NOT NULL auto_increment,
  `filterType` varchar(128) default NULL,
  `nudgematicOffsetSize` tinyint default NULL,
  `coaddExposureLength` int default 100,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/*****************************/
