/** used to update database related to Phase2/OSS system **/
/** run after create_database.sql to update to latest format **/

/** e.g.:
	mysql -u root -p < update_database_1.sql
**/

/*********** START ***********/

USE phase2odb;

alter table PROPOSAL add column allowUrgentGroups tinyint default NULL;
alter table PROPOSAL add column priorityOffset int default NULL; /** type changed to double in update_database_3 **/
alter table PROPOSAL add column enabled tinyint default NULL;

alter table OBSERVATION_GROUP add column urgent tinyint default NULL;

/*****************************/
