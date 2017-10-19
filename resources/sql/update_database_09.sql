/** used to update database related to Phase2/OSS system **/
/** run after update_database_2.sql to update to latest format **/

/** e.g.:
	mysql -u root -p < update_database_9.sql
**/

/*********** START ***********/

USE phase2odb;

CREATE TABLE `EA_FOCUSCONTROL` (
  `id` int NOT NULL auto_increment,
  `instrumentName` varchar(32) default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_OPTICALSLIDECONFIG` (
  `id` int NOT NULL auto_increment,
  `slideNumber` int default NULL,
  `positionNumber` int default NULL,	
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_BEAMSTEERINGCONFIG` (
  `id` int NOT NULL auto_increment,
  `slideConfig1Ref` int default NULL,	/** the id of the first related SLIDE_CONFIG entry **/
  `slideConfig2Ref` int default NULL,	/** the id of the second related SLIDE_CONFIG entry **/
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;  

CREATE TABLE `EA_TIPTILTABSOLUTEOFFSET` (
  `id` int NOT NULL auto_increment,
  `offset1` double default NULL, 				/** x or ra offset **/
  `offset2` double default NULL, 				/** y or dec offset **/
  `instrumentName` varchar(32) default NULL,
  `offsetType` int default NULL,				/** radec or focal plane **/
  `tipTiltId` int default NULL,					/** the id of the til tilt mirror being offset **/
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;  
 
/*****************************/
