CREATE DATABASE  IF NOT EXISTS `forum_api` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `forum_api`;
-- MySQL dump 10.13  Distrib 5.5.41, for debian-linux-gnu (x86_64)
--
-- Host: 127.0.0.1    Database: forum_api
-- ------------------------------------------------------
-- Server version	5.5.41-0ubuntu0.14.04.1

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `follow`
--

DROP TABLE IF EXISTS `follow`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `follow` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `follower` varchar(255) NOT NULL,
  `followee` varchar(255) NOT NULL,
  PRIMARY KEY (`follower`,`followee`),
  UNIQUE KEY `idf_UNIQUE` (`id`),
  KEY `fk_follows_1_idx` (`follower`),
  KEY `fk_follows_2_idx` (`followee`),
  CONSTRAINT `fk_follows_1` FOREIGN KEY (`follower`) REFERENCES `user` (`email`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_follows_2` FOREIGN KEY (`followee`) REFERENCES `user` (`email`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `forum`
--

DROP TABLE IF EXISTS `forum`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forum` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `short_name` varchar(255) NOT NULL,
  `user` varchar(255) NOT NULL,
  PRIMARY KEY (`short_name`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  UNIQUE KEY `short_name_UNIQUE` (`short_name`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `user` (`user`),
  CONSTRAINT `forum_ibfk_1` FOREIGN KEY (`user`) REFERENCES `user` (`email`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `forum_user`
--

DROP TABLE IF EXISTS `forum_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `forum_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `forum` varchar(255) NOT NULL,
  `user` varchar(255) NOT NULL,
  PRIMARY KEY (`user`,`forum`),
  KEY `id` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `post`
--

DROP TABLE IF EXISTS `post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `post` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` datetime NOT NULL,
  `forum` varchar(255) NOT NULL,
  `isApproved` tinyint(1) NOT NULL DEFAULT '0',
  `isDeleted` tinyint(1) NOT NULL DEFAULT '0',
  `isEdited` tinyint(1) NOT NULL DEFAULT '0',
  `isHighlighted` tinyint(1) NOT NULL DEFAULT '0',
  `isSpam` tinyint(1) NOT NULL DEFAULT '0',
  `message` text NOT NULL,
  `parent` int(11) DEFAULT NULL,
  `thread` int(11) NOT NULL,
  `user` varchar(255) NOT NULL,
  `likes` int(11) NOT NULL DEFAULT '0',
  `dislikes` int(11) NOT NULL DEFAULT '0',
  `mpath` varchar(255) DEFAULT NULL,
  `root_mpath` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_posts_1_idx` (`user`),
  KEY `fk_posts_2_idx` (`forum`),
  KEY `user_date` (`user`,`date`),
  KEY `fk_posts_3_idx` (`thread`),
  KEY `thread_date` (`thread`,`date`),
  KEY `forum_date` (`forum`,`date`),
  KEY `thread_rootMpath_mpath` (`thread`,`root_mpath`,`mpath`),
  CONSTRAINT `fk_posts_1` FOREIGN KEY (`user`) REFERENCES `user` (`email`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_posts_2` FOREIGN KEY (`forum`) REFERENCES `forum` (`short_name`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_posts_4` FOREIGN KEY (`thread`) REFERENCES `thread` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`user`@`localhost`*/ /*!50003 TRIGGER `post_BINS` BEFORE INSERT ON `post` FOR EACH ROW
BEGIN
  DECLARE next_id INT;
  SET next_id = (SELECT AUTO_INCREMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='post');

  IF (NEW.parent IS NOT NULL) THEN
	SET NEW.mpath = concat((SELECT IFNULL(mpath,'') FROM post WHERE id = NEW.parent),'/',CONV(next_id,10,36));
	SET NEW.root_mpath = (SELECT root_mpath FROM post WHERE id = NEW.parent);
  else
	SET NEW.root_mpath = CONV(next_id,10,36);
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`user`@`localhost`*/ /*!50003 TRIGGER `post_AINS` AFTER INSERT ON `post` FOR EACH ROW
begin
  IF (NEW.isDeleted = 0) THEN
	UPDATE thread SET posts = posts + 1 WHERE id = NEW.thread;
  END IF;
  INSERT IGNORE INTO forum_user (forum, user) VALUES (NEW.forum, NEW.user);
end */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8 */ ;
/*!50003 SET character_set_results = utf8 */ ;
/*!50003 SET collation_connection  = utf8_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `post_AUPD` AFTER UPDATE ON `post` FOR EACH ROW
BEGIN
 IF OLD.isDeleted = false AND NEW.isDeleted = true THEN
    UPDATE thread SET posts = posts - 1 WHERE thread.id = NEW.thread;
 ELSEIF OLD.isDeleted = true AND NEW.isDeleted = false THEN
    UPDATE thread SET posts = posts + 1 WHERE thread.id = NEW.thread;
 END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `subscribe`
--

DROP TABLE IF EXISTS `subscribe`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `subscribe` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user` varchar(255) NOT NULL,
  `thread` int(11) NOT NULL,
  PRIMARY KEY (`user`,`thread`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_subscribes_1_idx` (`user`),
  KEY `fk_subscribes_2_idx` (`thread`),
  CONSTRAINT `fk_subscribes_1` FOREIGN KEY (`user`) REFERENCES `user` (`email`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_subscribes_2` FOREIGN KEY (`thread`) REFERENCES `thread` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `thread`
--

DROP TABLE IF EXISTS `thread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `thread` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` datetime NOT NULL,
  `forum` varchar(255) NOT NULL,
  `slug` varchar(255) NOT NULL,
  `message` text NOT NULL,
  `isClosed` tinyint(1) NOT NULL DEFAULT '0',
  `isDeleted` tinyint(1) NOT NULL DEFAULT '0',
  `user` varchar(255) NOT NULL,
  `title` varchar(255) NOT NULL,
  `likes` int(11) NOT NULL DEFAULT '0',
  `dislikes` int(11) NOT NULL DEFAULT '0',
  `posts` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `fk_threads_1_idx` (`forum`),
  KEY `fk_threads_2_idx` (`user`),
  KEY `user_date` (`user`,`date`),
  KEY `forum_date` (`forum`,`date`),
  CONSTRAINT `fk_threads_1` FOREIGN KEY (`forum`) REFERENCES `forum` (`short_name`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_threads_2` FOREIGN KEY (`user`) REFERENCES `user` (`email`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(255) DEFAULT NULL,
  `about` text,
  `isAnonymous` tinyint(1) NOT NULL DEFAULT '0',
  `name` varchar(255) DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  PRIMARY KEY (`email`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `name_email` (`name`,`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2016-01-13 19:46:51
