/** used to update database related to Phase2/OSS system **/
/** run after update_database_2.sql to update to latest format **/

/** e.g.:
	mysql -u oss -p<password> < update_database_19.sql
**/

/*********** START ***********/
USE phase2odb;

CREATE TABLE `INST_CONFIG_MOPTOP` (
  `id` int NOT NULL auto_increment,
  `dichroicState` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/*****************************/
