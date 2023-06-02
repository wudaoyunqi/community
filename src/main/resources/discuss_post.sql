DROP TABLE IF EXISTS `discuss_post_new_3`;
SET
character_set_client = utf8mb4 ;
CREATE TABLE `discuss_post_new_3`
(
    `id`            int(11) NOT NULL AUTO_INCREMENT,
    `user_id`       varchar(45)  DEFAULT NULL,
    `title`         varchar(100) DEFAULT NULL,
    `content`       text,
    `type`          int(11) DEFAULT '0' COMMENT '0-普通; 1-置顶;',
    `status`        int(11) DEFAULT '0' COMMENT '0-正常; 1-精华; 2-拉黑;',
    `create_time`   timestamp NULL DEFAULT NULL,
    `comment_count` int(11) DEFAULT '0',
    `score`         double       DEFAULT '0',
    PRIMARY KEY (`id`),
    KEY             `index_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;