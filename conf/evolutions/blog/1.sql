--Archive Schema
-- !Ups
CREATE TABLE if not exists `archive`
(
    `id`          INT          NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(256) NOT NULL COMMENT 'post title',
    `author`      VARCHAR(64)  NOT NULL COMMENT 'author',
    `publishTime` DATETIME     NOT NULL COMMENT 'publish time',
    `content`     LONGTEXT     NOT NULL COMMENT 'post content',
    `createTime`  DATETIME     NOT NULL COMMENT 'post created time',
    `catalog`     TINYTEXT     NULL COMMENT 'post directory',
    PRIMARY KEY (`id`)
);
--Category Schema
CREATE TABLE if not exists `category`
(
    `id`       INT          NOT NULL AUTO_INCREMENT,
    `category` VARCHAR(128) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT "unique_category" UNIQUE (`category`)
);
--Tag Schema
CREATE TABLE if not exists `tag`
(
    `id`  INT          NOT NULL AUTO_INCREMENT,
    `tag` VARCHAR(128) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT "unique_tag" UNIQUE (`tag`)
);
CREATE TABLE if not exists `archive_category`
(
    `id`          INT NOT NULL AUTO_INCREMENT,
    `archive_id`  INT,
    `category_id` INT,
    CONSTRAINT `fk_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
    CONSTRAINT `fk_archive_1` FOREIGN KEY (`archive_id`) REFERENCES `archive` (`id`),
    PRIMARY KEY (`id`)
);
CREATE TABLE if not exists `archive_tag`
(
    `id`         INT NOT NULL AUTO_INCREMENT,
    `archive_id` INT,
    `tag_id`     INT,
    CONSTRAINT `fk_archive_2` FOREIGN KEY (`archive_id`) REFERENCES `archive` (`id`),
    CONSTRAINT `fk_tag` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`),
    PRIMARY KEY (`id`)
);
CREATE TABLE if not exists `account`
(
    `id`       INT          NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(128) NOT NULL,
    `password` VARCHAR(64)  NOT NULL,
    PRIMARY KEY (`id`)
);
insert into `account`
values (1, 'admin', 'admin');
-- !Downs
-- DROP TABLE `archive_tag`;
-- DROP TABLE `archive_category`;
-- DROP TABLE `archive`;
-- DROP TABLE `category`;
-- DROP TABLE `tag`;
-- DROP TABLE `account`;