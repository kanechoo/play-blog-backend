--Article Schema
-- !Ups
CREATE TABLE `article`
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
    KEY        `category_index` (`category`) USING BTREE,
    PRIMARY KEY (`id`)
);
--Tag Schema
CREATE TABLE `tag`
(
    `id`  INT          NOT NULL AUTO_INCREMENT,
    `tag` VARCHAR(128) NOT NULL,
    KEY   `tag_index` (`tag`) USING BTREE,
    PRIMARY KEY (`id`)
);
CREATE TABLE `article_category`
(
    `id`          INT NOT NULL AUTO_INCREMENT,
    `article_id`  INT,
    `category_id` INT,
    PRIMARY KEY (`id`)
);
CREATE TABLE `article_tag`
(
    `id`         INT NOT NULL AUTO_INCREMENT,
    `article_id` INT,
    `tag_id`     INT,
    PRIMARY KEY (`id`)
);
-- !Downs
DROP TABLE `article_tag`;
DROP TABLE `article_category`;
DROP TABLE `article`;
DROP TABLE `category`;
DROP TABLE `tag`;