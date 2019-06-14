SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";

USE `heavenms`;

CREATE TABLE `accountbuffs` (
  `skillid` int(11) NOT NULL,
  `comments` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`skillid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

INSERT INTO `accountbuffs` VALUES
(1301007, 'Spearman.HYPER_BODY'),
(2221000, 'ILArchMage.MAPLE_WARRIOR'),
(2311003, 'Priest.HOLY_SYMBOL'),
(2321000, 'Bishop.MAPLE_WARRIOR'),
(3121002, 'Bowmaster.SHARP_EYES'),
(3221002, 'Marksman.SHARP_EYES'),
(4101004, 'Assassin.HASTE'),
(4201003, 'Bandit.HASTE'),
(5121009, 'Buccaneer.SPEED_INFUSION');

CREATE TABLE IF NOT EXISTS `drop_data_forest` (
  `dropperid` int(11) NOT NULL,
  `dropper` varchar(45) DEFAULT NULL,
  `itemid` int(11) NOT NULL DEFAULT 0,
  `item` varchar(45) DEFAULT NULL,
  `minimum_quantity` int(11) NOT NULL DEFAULT 1,
  `maximum_quantity` int(11) NOT NULL DEFAULT 1,
  `questid` int(11) NOT NULL DEFAULT 0,
  `chance` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`dropperid`,`itemid`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

CREATE TRIGGER ins_replace_drop_data BEFORE INSERT
  ON `drop_data_forest` FOR EACH ROW
  REPLACE INTO `drop_data`
    SELECT NULL, `dropperid`, `itemid`, `minimum_quantity`, `maximum_quantity`, `questid`, `chance`
      FROM `drop_data_forest`;

CREATE TRIGGER upd_replace_drop_data BEFORE UPDATE
  ON `drop_data_forest` FOR EACH ROW
  REPLACE INTO `drop_data`
    SELECT NULL, `dropperid`, `itemid`, `minimum_quantity`, `maximum_quantity`, `questid`, `chance`
      FROM `drop_data_forest`;
