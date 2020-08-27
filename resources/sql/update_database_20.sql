/** used to update database related to Phase2/OSS system **/
/** run after update_database_19.sql to update to latest format **/

/** e.g.:
	mysql -u oss -p<password> < update_database_20.sql
**/

/*********** START ***********/
USE phase2odb;

/* Drop wrong MOPTOP table in update_database_19.sql */
DROP TABLE IF EXISTS `INST_CONFIG_MOPTOP`;

CREATE TABLE `INST_CONFIG_MOPTOP` (
  `id` int NOT NULL auto_increment,
  `filterType` varchar(128) default NULL,
  `rotorSpeed` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/*****************************/
