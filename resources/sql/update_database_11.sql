/** used to update database related to Phase2/OSS system **/
/** run after update_database_2.sql to update to latest format **/

/** e.g.:
	mysql -u root -p < update_database_9.sql
**/

/*********** START ***********/

USE phase2odb;

CREATE TABLE `LOGIN` (
  `id` int NOT NULL auto_increment,
  `loginTime` double default NULL,
  `uid` int default NULL,
  `javaVersion` varchar(32) default NULL,
  `osArch` varchar(32) default NULL,
  `osName` varchar(32) default NULL,
  `osVersion` varchar(32) default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/*****************************/
