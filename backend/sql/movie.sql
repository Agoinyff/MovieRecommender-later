/*
 Navicat Premium Dump SQL

 Source Server         : localhost8.0
 Source Server Type    : MySQL
 Source Server Version : 80040 (8.0.40)
 Source Host           : localhost:3306
 Source Schema         : movie

 Target Server Type    : MySQL
 Target Server Version : 80040 (8.0.40)
 File Encoding         : 65001

 Date: 25/02/2026 08:47:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for movie_links
-- ----------------------------
DROP TABLE IF EXISTS `movie_links`;
CREATE TABLE `movie_links`  (
  `movieID` int NOT NULL,
  `imdbID` int NULL DEFAULT NULL,
  `tmdbID` int NULL DEFAULT NULL,
  PRIMARY KEY (`movieID`) USING BTREE,
  INDEX `idx_imdb`(`imdbID` ASC) USING BTREE,
  INDEX `idx_tmdb`(`tmdbID` ASC) USING BTREE,
  CONSTRAINT `fk_links_movie` FOREIGN KEY (`movieID`) REFERENCES `movies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for movie_preferences
-- ----------------------------
DROP TABLE IF EXISTS `movie_preferences`;
CREATE TABLE `movie_preferences`  (
  `userID` int NOT NULL,
  `movieID` int NOT NULL,
  `preference` int NOT NULL DEFAULT 0,
  `timestamp` int NOT NULL DEFAULT 0,
  INDEX `movie_preferences_index2`(`userID` ASC) USING BTREE,
  INDEX `movie_preferences_index3`(`movieID` ASC) USING BTREE,
  INDEX `movie_preferences_index1`(`userID` ASC, `movieID` ASC) USING BTREE,
  CONSTRAINT `movie_preferences_ibfk_1` FOREIGN KEY (`movieID`) REFERENCES `movies` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for movie_tags
-- ----------------------------
DROP TABLE IF EXISTS `movie_tags`;
CREATE TABLE `movie_tags`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `userID` int NOT NULL,
  `movieID` int NOT NULL,
  `tag` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `timestamp` int UNSIGNED NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tag_movie`(`movieID` ASC) USING BTREE,
  INDEX `idx_tag_user`(`userID` ASC) USING BTREE,
  INDEX `idx_tag_text`(`tag` ASC) USING BTREE,
  INDEX `idx_tag_user_movie`(`userID` ASC, `movieID` ASC) USING BTREE,
  CONSTRAINT `fk_tags_movie` FOREIGN KEY (`movieID`) REFERENCES `movies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 95581 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for movies
-- ----------------------------
DROP TABLE IF EXISTS `movies`;
CREATE TABLE `movies`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `published_year` varchar(4) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `type` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `poster_url` varchar(500) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL COMMENT '电影海报URL',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3953 CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;


CREATE TABLE IF NOT EXISTS app_user (
                                        id BIGINT NOT NULL AUTO_INCREMENT,
                                        username VARCHAR(32) NOT NULL,
                                        password_hash VARCHAR(255) NOT NULL,
                                        display_name VARCHAR(32) NOT NULL,
                                        role VARCHAR(16) NOT NULL,
                                        created_at BIGINT NOT NULL,
                                        updated_at BIGINT NOT NULL,
                                        PRIMARY KEY (id),
                                        UNIQUE KEY uk_app_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 AUTO_INCREMENT=1000000;
