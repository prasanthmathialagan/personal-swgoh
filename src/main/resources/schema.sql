CREATE TABLE IF NOT EXISTS `Users` (
  `id` bigint AUTO_INCREMENT NOT NULL,
  `userId` char(40) NOT NULL,
  `name` char(100) NOT NULL,
  `lastUpdated` timestamp,
  PRIMARY KEY (`id`),
  UNIQUE (`userId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `Toons` (
  `id` bigint AUTO_INCREMENT NOT NULL,
  `name` char(100) NOT NULL,
  `lastUpdated` timestamp,
  PRIMARY KEY (`id`),
  UNIQUE (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE IF NOT EXISTS `GuildToons` (
  `id` bigint AUTO_INCREMENT NOT NULL,
  `userId` bigint NOT NULL,
  `toonId` bigint NOT NULL,
  `star` smallint NOT NULL DEFAULT 0,
  `galacticPower` bigint NOT NULL DEFAULT 0,
  `lastUpdated` timestamp,
  PRIMARY KEY (`id`),
  UNIQUE (`userId`, `toonId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;