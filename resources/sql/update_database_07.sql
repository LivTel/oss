/** used to update database related to Phase2/OSS system **/
/** run after update_database_2.sql to update to latest format **/

/** e.g.:
	mysql -u root -p < update_database_2.sql
**/

/*********** START ***********/

USE phase2odb;

CREATE TABLE `INST_CONFIG_TIP_TILT` (
  `id` int NOT NULL auto_increment,
  `filterType` varchar(64) default NULL,	
  `gain` int default NULL,					
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

ALTER TABLE EA_EXPOSURE ADD COLUMN totalDuration double default NULL;
ALTER TABLE EA_EXPOSURE ADD COLUMN runAtTime double default NULL;

/*****************************/
