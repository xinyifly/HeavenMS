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
