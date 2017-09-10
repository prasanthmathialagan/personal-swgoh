CREATE TABLE IF NOT EXISTS `Users` (
  `id` bigint AUTO_INCREMENT NOT NULL,
  `userId` char(40) NOT NULL,
  `name` char(100) NOT NULL,
  `lastUpdated` timestamp,
  PRIMARY KEY (`id`),
  UNIQUE (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;