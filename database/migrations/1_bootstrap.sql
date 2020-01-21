CREATE DATABASE  IF NOT EXISTS `foreign-language-reader`;
USE `foreign-language-reader`;

--
-- Table structure for table `language`
--

DROP TABLE IF EXISTS `language`;
CREATE TABLE `language` (
  `id` varchar(15) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
);

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `email` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`)
);

--
-- Table structure for table `vocabulary`
--

DROP TABLE IF EXISTS `vocabulary`;
CREATE TABLE `vocabulary` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user` int NOT NULL,
  `word` int NOT NULL,
  `added` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `user_UNIQUE` (`user`),
  UNIQUE KEY `word_UNIQUE` (`word`),
  CONSTRAINT `user` FOREIGN KEY (`user`) REFERENCES `users` (`id`),
  CONSTRAINT `word` FOREIGN KEY (`id`) REFERENCES `words` (`id`)
);

--
-- Table structure for table `words`
--

DROP TABLE IF EXISTS `words`;
CREATE TABLE `words` (
  `id` int NOT NULL AUTO_INCREMENT,
  `language` varchar(15) NOT NULL,
  `text` varchar(50) NOT NULL,
  `part_of_speech` varchar(15) NOT NULL,
  `lemma` varchar(50) DEFAULT NULL,
  `hsk` int DEFAULT NULL,
  `pinyin` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `WORD` (`language`,`text`),
  CONSTRAINT `language` FOREIGN KEY (`language`) REFERENCES `language` (`id`)
);