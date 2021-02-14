--Archive Schema
-- !Ups
CREATE TABLE `archive`
(
    `id`          INT          NOT NULL AUTO_INCREMENT,
    `title`       VARCHAR(256) NOT NULL COMMENT '文章标题',
    `author`      VARCHAR(64)  NOT NULL COMMENT '作者',
    `publishTime` DATETIME     NOT NULL COMMENT '发布时间',
    `content`     LONGTEXT     NOT NULL COMMENT '文章内容',
    `createTime`  DATETIME     NOT NULL COMMENT '创建时间',
    PRIMARY KEY (`id`)
);
--Category Schema
CREATE TABLE `category`
(
    `id`       INT          NOT NULL AUTO_INCREMENT,
    `category` VARCHAR(128) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT "unique_category" UNIQUE (`category`)
);
--Tag Schema
CREATE TABLE `tag`
(
    `id`  INT          NOT NULL AUTO_INCREMENT,
    `tag` VARCHAR(128) NOT NULL,
    PRIMARY KEY (`id`),
    CONSTRAINT "unique_tag" UNIQUE (`tag`)
);
CREATE TABLE `archive_category`
(
    `id`          INT NOT NULL AUTO_INCREMENT,
    `archive_id`  INT,
    `category_id` INT,
    CONSTRAINT `FK1sdlkgsikr4yrp5s4mdaayx3v` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
    CONSTRAINT `FK1sdlkgsikr4yrp5s4mdaayx3c` FOREIGN KEY (`archive_id`) REFERENCES `archive` (`id`),
    PRIMARY KEY (`id`)
);
CREATE TABLE `archive_tag`
(
    `id`         INT NOT NULL AUTO_INCREMENT,
    `archive_id` INT,
    `tag_id`     INT,
    CONSTRAINT `FK1sdlkgsikr4yrp5s4mdaayx3h` FOREIGN KEY (`archive_id`) REFERENCES `archive` (`id`),
    CONSTRAINT `FK1sdlkgsikr4yrp5s4mdaayx3l` FOREIGN KEY (`tag_id`) REFERENCES `tag` (`id`),
    PRIMARY KEY (`id`)
);
-- !Downs
-- DROP TABLE `archive_tag`;
-- DROP TABLE `archive_category`;
-- DROP TABLE `archive`;
-- DROP TABLE `category`;
-- DROP TABLE `tag`;