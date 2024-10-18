USE `heavenms`;

UPDATE `drop_data` SET `chance` = 5000 WHERE `chance` > 0 AND `chance` < 5000;

UPDATE `drop_data` SET `chance` = 10000 WHERE
    NOT `itemid` < 2000000 AND `itemid` < 3000000
    AND `chance` > 0 AND `chance` < 10000;
