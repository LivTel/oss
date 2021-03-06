/** used to update database related to Phase2/OSS system **/
/** run after update_database_2.sql to update to latest format **/

/** e.g.:
	mysql -u root -p < update_database_2.sql
**/

/*********** START ***********/

USE phase2odb;

ALTER TABLE PROPOSAL MODIFY COLUMN priorityOffset double;
ALTER TABLE PROPOSAL ADD COLUMN allowFixedGroups tinyint DEFAULT NULL;

/*****************************/
