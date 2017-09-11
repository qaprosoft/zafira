-- MySQL Script generated by MySQL Workbench
-- Mon Sep 11 14:12:22 2017
-- Model: New Model    Version: 1.0
-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema zafira
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `zafira` ;

-- -----------------------------------------------------
-- Schema zafira
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `zafira` DEFAULT CHARACTER SET latin1 ;
USE `zafira` ;

-- -----------------------------------------------------
-- Table `USERS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `USERS` ;

CREATE TABLE IF NOT EXISTS `USERS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `USERNAME` VARCHAR(100) NOT NULL,
  `PASSWORD` VARCHAR(50) NULL DEFAULT '',
  `EMAIL` VARCHAR(100) NULL,
  `FIRST_NAME` VARCHAR(100) NULL,
  `LAST_NAME` VARCHAR(100) NULL,
  `ENABLED` TINYINT(1) NOT NULL DEFAULT 0,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `USERNAME_UNIQUE` (`USERNAME` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `TEST_SUITES`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `TEST_SUITES` ;

CREATE TABLE IF NOT EXISTS `TEST_SUITES` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(200) NOT NULL,
  `DESCRIPTION` MEDIUMTEXT NULL,
  `FILE_NAME` VARCHAR(255) NOT NULL DEFAULT '',
  `USER_ID` INT UNSIGNED NOT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `NAME_FILE_USER_UNIQUE` (`NAME` ASC, `FILE_NAME` ASC, `USER_ID` ASC),
  INDEX `FK_TEST_SUITE_USER_ASC` (`USER_ID` ASC),
  CONSTRAINT `fk_TEST_SUITES_USERS1`
    FOREIGN KEY (`USER_ID`)
    REFERENCES `USERS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `PROJECTS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `PROJECTS` ;

CREATE TABLE IF NOT EXISTS `PROJECTS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(255) NOT NULL,
  `DESCRIPTION` TINYTEXT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `NAME_UNIQUE` (`NAME` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `TEST_CASES`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `TEST_CASES` ;

CREATE TABLE IF NOT EXISTS `TEST_CASES` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `TEST_CLASS` VARCHAR(255) NOT NULL,
  `TEST_METHOD` VARCHAR(100) NOT NULL,
  `INFO` TINYTEXT NULL,
  `TEST_SUITE_ID` INT UNSIGNED NOT NULL,
  `PRIMARY_OWNER_ID` INT UNSIGNED NOT NULL,
  `SECONDARY_OWNER_ID` INT UNSIGNED NULL,
  `STATUS` VARCHAR(20) NOT NULL DEFAULT 'UNKNOWN',
  `PROJECT_ID` INT UNSIGNED NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `FK_TEST_CASE_SUITE_ASC` (`TEST_SUITE_ID` ASC),
  INDEX `FK_TEST_CASE_USER_ASC` (`PRIMARY_OWNER_ID` ASC),
  INDEX `fk_TEST_CASES_PROJECTS1_idx` (`PROJECT_ID` ASC),
  INDEX `fk_TEST_CASES_USERS2_idx` (`SECONDARY_OWNER_ID` ASC),
  CONSTRAINT `fk_TEST_CASE_TEST_SUITE1`
    FOREIGN KEY (`TEST_SUITE_ID`)
    REFERENCES `TEST_SUITES` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TEST_CASES_USERS1`
    FOREIGN KEY (`PRIMARY_OWNER_ID`)
    REFERENCES `USERS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TEST_CASES_PROJECTS1`
    FOREIGN KEY (`PROJECT_ID`)
    REFERENCES `PROJECTS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TEST_CASES_USERS2`
    FOREIGN KEY (`SECONDARY_OWNER_ID`)
    REFERENCES `USERS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `WORK_ITEMS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `WORK_ITEMS` ;

CREATE TABLE IF NOT EXISTS `WORK_ITEMS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `JIRA_ID` VARCHAR(45) NOT NULL,
  `TYPE` VARCHAR(45) NOT NULL DEFAULT 'TASK',
  `BLOCKER` TINYINT(1) NOT NULL DEFAULT 0,
  `HASH_CODE` INT NULL,
  `DESCRIPTION` TINYTEXT NULL,
  `USER_ID` INT UNSIGNED NULL,
  `TEST_CASE_ID` INT UNSIGNED NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `WORK_ITEM_UNIQUE` (`JIRA_ID` ASC, `TYPE` ASC, `HASH_CODE` ASC),
  INDEX `fk_WORK_ITEMS_USERS1_idx` (`USER_ID` ASC),
  INDEX `fk_WORK_ITEMS_TEST_CASES1_idx` (`TEST_CASE_ID` ASC),
  CONSTRAINT `fk_WORK_ITEMS_USERS1`
    FOREIGN KEY (`USER_ID`)
    REFERENCES `USERS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_WORK_ITEMS_TEST_CASES1`
    FOREIGN KEY (`TEST_CASE_ID`)
    REFERENCES `TEST_CASES` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `JOBS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `JOBS` ;

CREATE TABLE IF NOT EXISTS `JOBS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `USER_ID` INT UNSIGNED NULL,
  `NAME` VARCHAR(100) NOT NULL,
  `JOB_URL` VARCHAR(255) NOT NULL,
  `JENKINS_HOST` VARCHAR(255) NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `fk_JOBS_USERS1_idx` (`USER_ID` ASC),
  UNIQUE INDEX `JOB_URL_UNIQUE` (`JOB_URL` ASC),
  CONSTRAINT `fk_JOBS_USERS1`
    FOREIGN KEY (`USER_ID`)
    REFERENCES `USERS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `TEST_RUNS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `TEST_RUNS` ;

CREATE TABLE IF NOT EXISTS `TEST_RUNS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `CI_RUN_ID` VARCHAR(50) NULL,
  `USER_ID` INT UNSIGNED NULL,
  `TEST_SUITE_ID` INT UNSIGNED NOT NULL,
  `STATUS` VARCHAR(20) NOT NULL,
  `SCM_URL` VARCHAR(255) NULL,
  `SCM_BRANCH` VARCHAR(100) NULL,
  `SCM_COMMIT` VARCHAR(100) NULL,
  `CONFIG_XML` TINYTEXT NULL,
  `WORK_ITEM_ID` INT UNSIGNED NULL,
  `JOB_ID` INT UNSIGNED NOT NULL,
  `BUILD_NUMBER` INT NOT NULL,
  `UPSTREAM_JOB_ID` INT UNSIGNED NULL,
  `UPSTREAM_JOB_BUILD_NUMBER` INT NULL,
  `STARTED_BY` VARCHAR(45) NOT NULL,
  `PROJECT_ID` INT UNSIGNED NULL,
  `KNOWN_ISSUE` TINYINT(1) NOT NULL DEFAULT 0,
  `BLOCKER` TINYINT(1) NOT NULL DEFAULT 0,
  `ENV` VARCHAR(50) NULL,
  `PLATFORM` VARCHAR(30) NULL,
  `APP_VERSION` VARCHAR(255) NULL,
  `STARTED_AT` TIMESTAMP NULL,
  `ELAPSED` INT NULL,
  `ETA` INT NULL,
  `COMMENTS` TINYTEXT NULL,
  `DRIVER_MODE` VARCHAR(50) NOT NULL DEFAULT 'METHOD_MODE',
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `REVIEWED` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`ID`),
  INDEX `FK_TEST_RUN_USER_ASC` (`USER_ID` ASC),
  INDEX `FK_TEST_RUN_TEST_SUITE_ASC` (`TEST_SUITE_ID` ASC),
  INDEX `fk_TEST_RUNS_WORK_ITEMS1_idx` (`WORK_ITEM_ID` ASC),
  INDEX `fk_TEST_RUNS_JOBS1_idx` (`JOB_ID` ASC),
  INDEX `fk_TEST_RUNS_JOBS2_idx` (`UPSTREAM_JOB_ID` ASC),
  UNIQUE INDEX `CI_RUN_ID_UNIQUE` (`CI_RUN_ID` ASC),
  INDEX `fk_TEST_RUNS_PROJECTS1_idx` (`PROJECT_ID` ASC),
  INDEX `TEST_RUNS_STARTED_AT_INDEX` (`STARTED_AT` ASC),
  CONSTRAINT `fk_TEST_RESULTS_USERS1`
    FOREIGN KEY (`USER_ID`)
    REFERENCES `USERS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TEST_RESULTS_TEST_SUITES1`
    FOREIGN KEY (`TEST_SUITE_ID`)
    REFERENCES `TEST_SUITES` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TEST_RUNS_WORK_ITEMS1`
    FOREIGN KEY (`WORK_ITEM_ID`)
    REFERENCES `WORK_ITEMS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TEST_RUNS_JOBS1`
    FOREIGN KEY (`JOB_ID`)
    REFERENCES `JOBS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TEST_RUNS_JOBS2`
    FOREIGN KEY (`UPSTREAM_JOB_ID`)
    REFERENCES `JOBS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TEST_RUNS_PROJECTS1`
    FOREIGN KEY (`PROJECT_ID`)
    REFERENCES `PROJECTS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `TEST_CONFIGS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `TEST_CONFIGS` ;

CREATE TABLE IF NOT EXISTS `TEST_CONFIGS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `URL` VARCHAR(512) NULL,
  `ENV` VARCHAR(50) NULL,
  `PLATFORM` VARCHAR(30) NULL,
  `PLATFORM_VERSION` VARCHAR(30) NULL,
  `BROWSER` VARCHAR(30) NULL,
  `BROWSER_VERSION` VARCHAR(30) NULL,
  `APP_VERSION` VARCHAR(255) NULL,
  `LOCALE` VARCHAR(30) NULL,
  `LANGUAGE` VARCHAR(30) NULL,
  `DEVICE` VARCHAR(50) NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `TESTS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `TESTS` ;

CREATE TABLE IF NOT EXISTS `TESTS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(255) NOT NULL,
  `STATUS` VARCHAR(20) NOT NULL,
  `TEST_ARGS` TINYTEXT NULL,
  `TEST_RUN_ID` INT UNSIGNED NOT NULL,
  `TEST_CASE_ID` INT UNSIGNED NOT NULL,
  `TEST_GROUP` VARCHAR(255) NULL,
  `MESSAGE` TINYTEXT NULL,
  `MESSAGE_HASH_CODE` INT NULL,
  `START_TIME` TIMESTAMP NULL,
  `FINISH_TIME` TIMESTAMP NULL,
  `DEMO_URL` TINYTEXT NULL,
  `LOG_URL` TINYTEXT NULL,
  `RETRY` INT NOT NULL DEFAULT 0,
  `TEST_CONFIG_ID` INT UNSIGNED NULL,
  `KNOWN_ISSUE` TINYINT(1) NOT NULL DEFAULT 0,
  `BLOCKER` TINYINT(1) NOT NULL DEFAULT 0,
  `NEED_RERUN` TINYINT(1) NOT NULL DEFAULT 1,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `fk_TESTS_TEST_RUNS1_idx` (`TEST_RUN_ID` ASC),
  INDEX `fk_TESTS_TEST_CASES1_idx` (`TEST_CASE_ID` ASC),
  INDEX `fk_TESTS_TEST_CONFIGS1_idx` (`TEST_CONFIG_ID` ASC),
  CONSTRAINT `fk_TESTS_TEST_RUNS1`
    FOREIGN KEY (`TEST_RUN_ID`)
    REFERENCES `TEST_RUNS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TESTS_TEST_CASES1`
    FOREIGN KEY (`TEST_CASE_ID`)
    REFERENCES `TEST_CASES` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TESTS_TEST_CONFIGS1`
    FOREIGN KEY (`TEST_CONFIG_ID`)
    REFERENCES `TEST_CONFIGS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `TEST_WORK_ITEMS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `TEST_WORK_ITEMS` ;

CREATE TABLE IF NOT EXISTS `TEST_WORK_ITEMS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `TEST_ID` INT UNSIGNED NOT NULL,
  `WORK_ITEM_ID` INT UNSIGNED NOT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `fk_TEST_WORK_ITEMS_TESTS1_idx` (`TEST_ID` ASC),
  INDEX `fk_TEST_WORK_ITEMS_WORK_ITEMS1_idx` (`WORK_ITEM_ID` ASC),
  CONSTRAINT `fk_TEST_WORK_ITEMS_TESTS1`
    FOREIGN KEY (`TEST_ID`)
    REFERENCES `TESTS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_TEST_WORK_ITEMS_WORK_ITEMS1`
    FOREIGN KEY (`WORK_ITEM_ID`)
    REFERENCES `WORK_ITEMS` (`ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `TEST_METRICS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `TEST_METRICS` ;

CREATE TABLE IF NOT EXISTS `TEST_METRICS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `OPERATION` VARCHAR(127) NOT NULL,
  `ELAPSED` BIGINT UNSIGNED NOT NULL COMMENT 'Operation elapsed in ms.',
  `TEST_ID` INT UNSIGNED NOT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `fk_TEST_METRICS_TESTS1_idx` (`TEST_ID` ASC),
  INDEX `TEST_OPERATION` (`OPERATION` ASC),
  CONSTRAINT `fk_TEST_METRICS_TESTS1`
    FOREIGN KEY (`TEST_ID`)
    REFERENCES `TESTS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `WIDGETS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `WIDGETS` ;

CREATE TABLE IF NOT EXISTS `WIDGETS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `TITLE` VARCHAR(255) NOT NULL,
  `TYPE` VARCHAR(20) NOT NULL DEFAULT 'linechart',
  `SQL` TINYTEXT NOT NULL,
  `MODEL` TINYTEXT NOT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `TITLE_UNIQUE` (`TITLE` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `I`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `I` ;

CREATE TABLE IF NOT EXISTS `I` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(255) NOT NULL,
  `VALUE` VARCHAR(255) NULL,
  `IS_ENCRYPTED` TINYINT(1) NULL DEFAULT 0,
  `TOOL` VARCHAR(255) NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `NAME_UNIQUE` (`NAME` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `DEVICES`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `DEVICES` ;

CREATE TABLE IF NOT EXISTS `DEVICES` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `MODEL` VARCHAR(255) NOT NULL,
  `SERIAL` VARCHAR(255) NOT NULL,
  `ENABLED` TINYINT(1) NOT NULL DEFAULT 0,
  `LAST_STATUS` TINYINT(1) NOT NULL DEFAULT 0,
  `DISCONNECTS` INT NOT NULL DEFAULT 0,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `SERIAL_UNIQUE` (`SERIAL` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `DASHBOARDS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `DASHBOARDS` ;

CREATE TABLE IF NOT EXISTS `DASHBOARDS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `TITLE` VARCHAR(255) NOT NULL,
  `TYPE` VARCHAR(255) NOT NULL DEFAULT 'GENERAL',
  `POSITION` INT UNSIGNED NOT NULL DEFAULT 0,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `TITLE_UNIQUE` (`TITLE` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `DASHBOARDS_WIDGETS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `DASHBOARDS_WIDGETS` ;

CREATE TABLE IF NOT EXISTS `DASHBOARDS_WIDGETS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `DASHBOARD_ID` INT UNSIGNED NOT NULL,
  `WIDGET_ID` INT UNSIGNED NOT NULL,
  `POSITION` INT UNSIGNED NOT NULL DEFAULT 0,
  `SIZE` INT UNSIGNED NOT NULL DEFAULT 1,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `fk_DASHBOARDS_WIDGETS_DASHBOARDS1_idx` (`DASHBOARD_ID` ASC),
  INDEX `fk_DASHBOARDS_WIDGETS_WIDGETS1_idx` (`WIDGET_ID` ASC),
  UNIQUE INDEX `DASHBOARD_WIDGET_UNIQUE` (`DASHBOARD_ID` ASC, `WIDGET_ID` ASC),
  CONSTRAINT `fk_DASHBOARDS_WIDGETS_DASHBOARDS1`
    FOREIGN KEY (`DASHBOARD_ID`)
    REFERENCES `DASHBOARDS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_DASHBOARDS_WIDGETS_WIDGETS1`
    FOREIGN KEY (`WIDGET_ID`)
    REFERENCES `WIDGETS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `EVENTS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `EVENTS` ;

CREATE TABLE IF NOT EXISTS `EVENTS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `TYPE` VARCHAR(45) NOT NULL,
  `TEST_RUN_ID` VARCHAR(100) NULL,
  `TEST_ID` VARCHAR(100) NULL,
  `DATA` TINYTEXT NULL,
  `RECEIVED` TIMESTAMP NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `GROUPS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `GROUPS` ;

CREATE TABLE IF NOT EXISTS `GROUPS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(255) NOT NULL,
  `ROLE` VARCHAR(255) NOT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `NAME_UNIQUE` (`NAME` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `USER_GROUPS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `USER_GROUPS` ;

CREATE TABLE IF NOT EXISTS `USER_GROUPS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `GROUP_ID` INT UNSIGNED NOT NULL,
  `USER_ID` INT UNSIGNED NOT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `fk_USER_GROUPS_GROUPS1_idx` (`GROUP_ID` ASC),
  INDEX `fk_USER_GROUPS_USERS1_idx` (`USER_ID` ASC),
  UNIQUE INDEX `USER_GROUP_UNIQUE` (`GROUP_ID` ASC, `USER_ID` ASC),
  CONSTRAINT `fk_USER_GROUPS_GROUPS1`
    FOREIGN KEY (`GROUP_ID`)
    REFERENCES `GROUPS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_USER_GROUPS_USERS1`
    FOREIGN KEY (`USER_ID`)
    REFERENCES `USERS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `DASHBOARD_ATTRIBUTES`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `DASHBOARD_ATTRIBUTES` ;

CREATE TABLE IF NOT EXISTS `DASHBOARD_ATTRIBUTES` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `KEY` VARCHAR(255) NOT NULL,
  `VALUE` VARCHAR(255) NOT NULL,
  `DASHBOARD_ID` INT UNSIGNED NOT NULL,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `fk_DASHBOARD_ATTRIBUTES_DASHBOARDS1_idx` (`DASHBOARD_ID` ASC),
  UNIQUE INDEX `DASHBOARD_KEY_UNIQUE` (`KEY` ASC, `DASHBOARD_ID` ASC),
  CONSTRAINT `fk_DASHBOARD_ATTRIBUTES_DASHBOARDS1`
    FOREIGN KEY (`DASHBOARD_ID`)
    REFERENCES `DASHBOARDS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `VIEWS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `VIEWS` ;

CREATE TABLE IF NOT EXISTS `VIEWS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(255) NOT NULL,
  `PROJECT_ID` INT UNSIGNED NOT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `fk_VIEWS_PROJECTS1_idx` (`PROJECT_ID` ASC),
  UNIQUE INDEX `VIEW_NAME_UNIQUE` (`NAME` ASC),
  CONSTRAINT `fk_VIEWS_PROJECTS1`
    FOREIGN KEY (`PROJECT_ID`)
    REFERENCES `PROJECTS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `JOB_VIEWS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `JOB_VIEWS` ;

CREATE TABLE IF NOT EXISTS `JOB_VIEWS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `VIEW_ID` INT UNSIGNED NOT NULL,
  `JOB_ID` INT UNSIGNED NOT NULL,
  `ENV` VARCHAR(50) NOT NULL,
  `POSITION` INT NOT NULL DEFAULT 0,
  `SIZE` INT NOT NULL DEFAULT 1,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL,
  PRIMARY KEY (`ID`),
  INDEX `fk_JOB_VIEWS_VIEWS1_idx` (`VIEW_ID` ASC),
  INDEX `fk_JOB_VIEWS_JOBS1_idx` (`JOB_ID` ASC),
  UNIQUE INDEX `JOB_ID_ENV_UNIQUE` (`ENV` ASC, `JOB_ID` ASC),
  CONSTRAINT `fk_JOB_VIEWS_VIEWS1`
    FOREIGN KEY (`VIEW_ID`)
    REFERENCES `VIEWS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  CONSTRAINT `fk_JOB_VIEWS_JOBS1`
    FOREIGN KEY (`JOB_ID`)
    REFERENCES `JOBS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `UA_INSPECTIONS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `UA_INSPECTIONS` ;

CREATE TABLE IF NOT EXISTS `UA_INSPECTIONS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `SYSTEM_ID` VARCHAR(255) NULL,
  `SERIAL_NUMBER` VARCHAR(255) NULL,
  `FIRMWARE_REV` VARCHAR(255) NULL,
  `HARDWARE_REV` VARCHAR(255) NULL,
  `BATTERY_LEVEL` INT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `TEST_ARTIFACTS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `TEST_ARTIFACTS` ;

CREATE TABLE IF NOT EXISTS `TEST_ARTIFACTS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(255) NOT NULL,
  `LINK` TINYTEXT NOT NULL,
  `EXPIRES_AT` TIMESTAMP NULL,
  `TEST_ID` INT UNSIGNED NOT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  INDEX `fk_TEST_ARTIFACTS_TESTS1_idx` (`TEST_ID` ASC),
  CONSTRAINT `fk_TEST_ARTIFACTS_TESTS1`
    FOREIGN KEY (`TEST_ID`)
    REFERENCES `TESTS` (`ID`)
    ON DELETE CASCADE
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `MONITORS`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `MONITORS` ;

CREATE TABLE IF NOT EXISTS `MONITORS` (
  `ID` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `NAME` VARCHAR(45) NOT NULL,
  `URL` VARCHAR(255) NOT NULL,
  `HTTP_METHOD` VARCHAR(45) NULL,
  `REQUEST_BODY` TEXT(1000) NULL,
  `IS_ACTIVE` TINYINT(1) NOT NULL DEFAULT 0,
  `TYPE` VARCHAR(45) NOT NULL,
  `EMAILS` VARCHAR(255) NULL,
  `CRON_EXPRESSION` VARCHAR(45) NOT NULL,
  `EXPECTED_RESPONSE_STATUS` INT NOT NULL,
  `MODIFIED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `CREATED_AT` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `NAME_UNIQUE` (`NAME` ASC),
  UNIQUE INDEX `URL_UNIQUE` (`URL` ASC))
ENGINE = InnoDB;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
