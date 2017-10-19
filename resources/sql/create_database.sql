/** used to create database related to Phase2/OSS system **/

/** e.g.:
	mysql -u root -p < create_database.sql
**/

/**
    to backup database:
	mysqldump -u root -p phase2odb >& /occ/bak/phase2odb_.sql 
**/

/************************************* START *************************************/

DROP DATABASE IF EXISTS phase2odb;

CREATE DATABASE phase2odb;

USE phase2odb;

CREATE TABLE `DETECTOR_CONFIG` (
  `id` int NOT NULL auto_increment,
  `xbin` tinyint default NULL,
  `ybin` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `DETECTOR_WINDOW` (
  `id` int NOT NULL auto_increment,
  `dcid` tinyint default NULL,        /** detector config id **/
  `x` int default NULL,
  `y` int default NULL,
  `w` int default NULL,
  `h` int default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_ACQUISITION_CONFIG` (
  `id` int NOT NULL auto_increment,
  `mode` tinyint default NULL,            /** one of IAcquisitionConfig WCS_FIT; BRIGHTEST **/
  `targetInstrument` varchar(32) default NULL,
  `acquisitionInstrument` varchar(32) default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_APERTURE_CONFIG` (
  `id` int NOT NULL auto_increment,
  `configure` tinyint default NULL,       /** TRUE **/
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_AUTOGUIDER_CONFIG` (   
  `id` int NOT NULL auto_increment, 
  `mode` tinyint default NULL,         	  /** one of  IAutoguiderConfig ON; ON_IF_AVAILABLE; OFF **/
  `name` varchar(64) default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_CALIBRATION` (
  `id` int NOT NULL auto_increment,
  `name` varchar(64) default NULL,
  `type` tinyint default NULL,			  /** one of CalibrationTypes.DARK ARC LAMP BIAS **/
  `exposureTime` double default NULL,
  `lampName` varchar(64) default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_EXPOSURE` (
  `id` int NOT NULL auto_increment,
  `exposureTime` double default NULL,
  `repeats` int default NULL,
  `standard` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_FOCUS_OFFSET` (
  `id` int NOT NULL auto_increment,
  `relative` tinyint default NULL,        /** true if relative, false otherwise **/
  `offset` double default NULL,           /** offset focus amount,  mm **/
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_MOSAIC_OFFSET` (
  `id` int NOT NULL auto_increment,
  `relative` tinyint default NULL,           /** is relative, i.e. not absolute **/
  `raOffset` double default NULL,
  `decOffset` double default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `EA_ROTATOR_CONFIG` (
  `id` int NOT NULL auto_increment,
  `mode` tinyint default NULL,       /** one of IRotatorConfig static vars SKY; MOUNT; VERTICAL; FLOAT; VFLOAT **/
  `angle` double default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;
 
CREATE TABLE `INSTRUMENT_CONFIG` (      /** These are non-specific and used instrument to create EA_INSTRUMENT_CONFIG **/
  `id` int NOT NULL auto_increment,
  `pid` int NOT NULL,                      /** programme id **/ 
  `dcid` int default NULL,                 /** EA_DETECTOR_CONGIG id **/
  `iConfigType` tinyint default NULL,      /** CCD, FRODO, POLAR etc **/
  `iConfigId` int default NULL,            /** instrument config id ref in EA_ table, e.g. EA_INST_CONFIG_CCD **/
  `name` varchar(32) default NULL,
  `instrClassName` varchar(32) default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `INST_CONFIG_CCD` (
  `id` int NOT NULL auto_increment,
  `filterType` varchar(128) default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `INST_CONFIG_FRODO_SPEC` (
  `id` int NOT NULL auto_increment,
  `resolution` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `INST_CONFIG_POLARIMETER` (
  `id` int NOT NULL auto_increment,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `INST_CONFIG_SPECTROGRAPH` (
  `id` int NOT NULL auto_increment,
  `wavelength` double default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;
  
CREATE TABLE LINKAGE (
  `id` int NOT NULL auto_increment,
  `preceding` int default NULL,        /** group id of preceeding group **/
  `following` int default NULL,        /** group id of following group **/
  `minInterval` int default NULL,      /** minimum time interval between linked groups **/
  `maxInterval` int default NULL,      /** maximum time interval between linked groups **/
  `confidence` double default NULL,    /** number representing how likely the relationship is to be consumated **/
  `relationship` tinyint default NULL, /** sort of relationship **/
 PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `OBSERVATION_GROUP` (
  `id` int NOT NULL auto_increment,
  `pid` int default NULL,            /** proposal id **/
  `tcid` int  default NULL,          /** timing constraint id **/
  `osid` int  default NULL,          /** observation sequence id (root element in sequence)**/
  `active` tinyint default NULL,     /** boolean, active true or false **/
  `name` varchar(64) default NULL,
  `priority` int default NULL,       /** rank of group in proposal **/
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `OBSERVING_CONSTRAINT` (
  `id` int NOT NULL auto_increment,
  `gid` int default NULL,            /** group id **/
  `type` tinyint default NULL,       /** from ngat.phase2.impl.mysql.reference.ObservingConstraintTypes **/
  `category` tinyint default NULL,   /** category value, e.g Good, Average, Photometric, Civil Twilight etc **/
  `min` double default NULL,
  `max` double default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `OBSERVING_PREFERENCE` ( /** NOT YET IMPLEMENTED **/
  `id` int NOT NULL auto_increment,
  `gid` int default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `PROGRAMME` (
  `id` int NOT NULL auto_increment,
  `name` varchar(32) default NULL,
  `description` varchar(128) default NULL,
  PRIMARY KEY (`id`)) ENGINE = InnoDB;

CREATE TABLE `PROPOSAL` (
  `id` int NOT NULL auto_increment,
  `pid` int NOT NULL,                /** programme id **/
  `tid` int NOT NULL, 		     	 /** tag id **/
  `name` varchar(32) default NULL,
  `title` varchar(128) default NULL,
  `priority` int default NULL, 
  `activation` double,
  `expiry` double,
  `sci` varchar(128) default NULL,
  `active` tinyint default NULL, 
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `REVISION` (
  `id` int NOT NULL auto_increment,
  `pid` int default NULL,           /** proposal id **/
  `date` double,               
  `comment` varchar(64) default NULL,
  `who` varchar(64) default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `SEMESTER` (
  `id` int NOT NULL auto_increment,
  `name` varchar(64) default NULL,
  `startDate` double,
  `endDate` double,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `SEQUENCE_COMPONENT` (
  `id` int NOT NULL auto_increment,
  `gid` int default NULL,           /** group id, leave in, makes deletion easy **/
  `parent` int default NULL,	    /** id of parent SEQUENCE_COMPONENT, if there is one **/
  `type` tinyint default NULL,      /** one of Branch, Iterator, Executive, one of structure types in SequenceElementTypes **/
  `condType` int default NULL,      /** condition type (e.g. repeat-count, duration, one-shot, etc) **/
  `condVal` int default NULL,       /** dependent on condType, can be repeat count or duration mS etc. **/
  `eaType`  tinyint default NULL,   /** type of executive (action) element, one of executive types in SequenceElementTypes **/
  `eaRef` int default NULL,         /** reference to the actual executive element (id of EA_ or Target or InstrumentConfig) **/
  `name` varchar(64) default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;
 
CREATE TABLE `TAG` (
  `id` int NOT NULL auto_increment,
  `name` varchar(128) default NULL,
  PRIMARY KEY (`id`)) ENGINE = InnoDB;

CREATE TABLE `TARGET` (
  `id` int NOT NULL auto_increment,
  `pid` int default NULL,            /** programme id **/ 
  `name` varchar(32) default NULL,
  `type` tinyint default NULL,         /** catalogue, ephemeris, etc **/
  `targetRef` int  default NULL,       /** ref to underlying table, e.g. EA_TARGET_EPHEMERIS **/
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `TARGET_EXTRA_SOLAR` (
  `id` int NOT NULL auto_increment,
  `ra` double default NULL,
  `decl` double default NULL,
  `pmra` double default NULL,
  `pmdec` double default NULL,
  `radialVel` double default NULL,
  `parallax` double default NULL,
  `epoch` double default NULL,
  `frame` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `TARGET_ORBITAL_ELEMENTS` (
  `id` int NOT NULL auto_increment,
  `epoch` double default NULL,
  `orbinc` double default NULL,
  `anode` double default NULL,
  `perih` double default NULL,
  `aorq` double default NULL,
  `ecc` double default NULL,
  `aorl` double default NULL,
  `dm` double default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `TARGET_CATALOG` (
  `id` int NOT NULL auto_increment,
  `catIndex` int default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `TARGET_EPHEMERIS` (
  `id` int NOT NULL auto_increment,
  `tid` int default NULL,        /** many to one, back link to target id **/
  `time` double default NULL,
  `ra` double default NULL,
  `decl` double default NULL,
  `raDot` double default NULL,
  `decDot` double default NULL,
  PRIMARY KEY  (`id`));

CREATE TABLE `TIMING_CONSTRAINT` (
  `id` int NOT NULL auto_increment,
  `type` tinyint default NULL,
  `start` double,
  `end` double,
  `period` double default NULL, 
  `window` double default NULL,
  `maxCount` int default NULL,
  `phase` double default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/************************************* access ****************************************/

CREATE TABLE `ACCESS_PERMISSION` (
  `id` int NOT NULL auto_increment,
  `uid` int default NULL,              /** user id **/
  `pid` int default NULL,              /** proposal id **/
  `role` tinyint default NULL, 
 PRIMARY KEY (`id`)) ENGINE = InnoDB;

CREATE TABLE `USER` (
  `id` int NOT NULL auto_increment,
  `userName` varchar(32) default NULL,
  `password` varchar(32) default NULL,
  `lastName` varchar(64) default NULL,
  `firstName` varchar(64) default NULL,
  `department` varchar(64) default NULL,
  `organisation` varchar(128) default NULL,
  `address` varchar(64) default NULL,
  `city` varchar(64) default NULL,
  `region` varchar(64) default NULL,
  `country` varchar(64) default NULL,
  `postcode` varchar(32) default NULL,
  `email` varchar(64) default NULL,
  `telephone` varchar(32) default NULL,
  `fax` varchar(32) default NULL,
  `isSuperUser` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/************************************* accounting ************************************/

CREATE TABLE `ACCOUNT` (
  `id` int NOT NULL auto_increment,
  `sid` int default NULL,              /** semester id **/
  `ownerType` int default NULL,        /** type of owner, proposal or tag **/
  `ownerId`  int default NULL,         /** id of owner on proposal or tag table **/
  `name` varchar(32) default NULL,
  `description` varchar(128) default NULL,
  `allocated` double default NULL,     /** allocated time amount **/
  `consumed`double default NULL,       /** consumed time **/
  `chargeable` tinyint default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

CREATE TABLE `TRANSACTION` (
  `id` int NOT NULL auto_increment,
  `aid` int default NULL,               /** account id of this transaction**/
  `clientRef` varchar(32) default NULL, /** signature of app / person etc which carried out the transaction **/
  `time` double default NULL,         	/** time of transaction **/
  `amount` double default NULL,
  `comment` varchar(64) default NULL,
  `balanceType` int NOT NULL, /** TransactionTypes.ALLOCATION_TIME_TRANSACTION | TransactionTypes.CONSUMED_TIME_TRANSACTION **/
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/************************************* gatekeeper ************************************/

CREATE TABLE `LOCKS` (
  `id` int NOT NULL auto_increment,
  `oid` int default NULL,
  `objectType` tinyint default NULL,
  `atTime` double,
  `clientRef` varchar(64) default NULL,
  `keyVal` int default NULL,
  PRIMARY KEY (`id`)) ENGINE = InnoDB;

/*************************************************************************************/

/************************************* history ************************************/

CREATE TABLE `HISTORY_ITEM` (
  `id` int NOT NULL auto_increment,
  `gid` int default NULL,           /** group id **/
  `scheduledTime` double,
  `completionStatus` tinyint default 0,
  `completionTime` double, 
  `errorCode` int NOT NULL,
  `errorMessage` varchar(64)  default NULL,
  PRIMARY KEY  (`id`)) ENGINE = InnoDB;

/*************************************************************************************/

/************************************* exposure item ******************************/

CREATE TABLE `EXPOSURE_ITEM` (
  `id` int NOT NULL auto_increment,
  `hid` int default NULL,           /** hist item id **/
  `fileName` varchar(32) default NULL,
  `time` double,	
  PRIMARY KEY (`id`)) ENGINE = InnoDB;

/*************************************************************************************/


/************************************* QOS sitem *************************************/
/* These should really be matched against QOS requirements specified in the Group definition */
/* But these are not yet defined and indeed do not exist in any shape or form known to mankind */
/* So no big rush */

CREATE TABLE `QOS_ITEM` (
  `id` int NOT NULL auto_increment,
  `hid` int default NULL,           /** hist item id **/
  `qosName` varchar(32) default NULL,
  `qosValue` double default 0.0,
  PRIMARY KEY (`id`)) ENGINE = InnoDB;

/*************************************************************************************/
