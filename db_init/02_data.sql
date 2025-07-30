-- MySQL dump 10.13  Distrib 9.3.0, for macos15.2 (arm64)
--
-- Host: 127.0.0.1    Database: Booking
-- ------------------------------------------------------
-- Server version	8.0.43

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `Booking`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `Booking` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `Booking`;

--
-- Dumping data for table `bookings`
--

LOCK TABLES `bookings` WRITE;
/*!40000 ALTER TABLE `bookings` DISABLE KEYS */;
INSERT INTO `bookings` VALUES (1,1,10001,101,'BOOKED','2025-07-29 10:00:00',NULL),(2,2,10003,102,'BOOKED','2025-07-29 11:00:00',NULL),(4,3,10005,103,'CANCELED','2025-07-29 13:00:00','2025-07-30 02:33:26'),(5,5,10002,105,'CANCELED','2025-07-29 15:40:00','2025-07-30 00:31:56'),(6,4,10001,104,'BOOKED','2025-07-29 16:00:00',NULL),(7,2,10001,102,'BOOKED','2025-07-29 16:05:00',NULL),(8,3,10002,103,'CANCELED','2025-07-29 16:10:00','2025-07-30 01:53:07'),(9,5,10001,103,'BOOKED','2025-07-29 23:36:03',NULL),(10,5,10002,103,'CANCELED','2025-07-30 00:34:05','2025-07-30 00:40:04'),(11,5,10002,103,'CANCELED','2025-07-30 01:53:07','2025-07-30 02:27:10'),(12,3,10002,103,'CANCELED','2025-07-30 02:27:10','2025-07-30 02:32:11');
/*!40000 ALTER TABLE `bookings` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `class_info`
--

LOCK TABLES `class_info` WRITE;
/*!40000 ALTER TABLE `class_info` DISABLE KEYS */;
INSERT INTO `class_info` VALUES (101,'Yoga Flow','A dynamic and energetic yoga class suitable for all levels.',60,1),(102,'Pilates Mat','Focus on core strength, flexibility, and body awareness.',50,1),(103,'Zumba Dance','Dance fitness program with Latin rhythms and easy-to-follow moves.',45,2),(104,'Spin Cycle','High-intensity indoor cycling workout.',55,2),(105,'Barre Fusion','Combines ballet, yoga, and Pilates for a full-body workout.',60,3),(106,'Meditation','Guided meditation session for stress reduction and mindfulness.',30,1);
/*!40000 ALTER TABLE `class_info` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `class_schedules`
--

LOCK TABLES `class_schedules` WRITE;
/*!40000 ALTER TABLE `class_schedules` DISABLE KEYS */;
INSERT INTO `class_schedules` VALUES (10001,101,'2025-07-30 10:00:00','2025-07-30 11:00:00',2,5,'2025-07-25 09:00:00'),(10002,101,'2025-08-01 18:30:00','2025-08-01 19:30:00',2,1,'2025-07-25 09:00:00'),(10003,103,'2025-07-31 09:00:00','2025-07-31 10:00:00',1,3,'2025-07-26 10:00:00'),(10004,103,'2025-08-03 17:00:00','2025-08-03 18:00:00',1,2,'2025-07-26 10:00:00'),(10005,106,'2025-08-05 08:00:00','2025-08-05 08:45:00',1,3,'2025-07-27 11:00:00');
/*!40000 ALTER TABLE `class_schedules` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `countries`
--

LOCK TABLES `countries` WRITE;
/*!40000 ALTER TABLE `countries` DISABLE KEYS */;
INSERT INTO `countries` VALUES (1,'Singapore','SG'),(2,'Thailand','TH'),(3,'Malaysia','MY'),(4,'Indonesia','ID');
/*!40000 ALTER TABLE `countries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `packages`
--

LOCK TABLES `packages` WRITE;
/*!40000 ALTER TABLE `packages` DISABLE KEYS */;
INSERT INTO `packages` VALUES (1001,'Basic Package (SG)',5,50.00,30,'2025-07-15 09:00:00',1),(1002,'Premium Package (SG)',10,90.00,60,'2025-07-15 09:00:00',1),(2001,'Basic Package (TH)',5,45.00,30,'2025-07-16 10:00:00',2),(2002,'Premium Package (TH)',10,80.00,60,'2025-07-16 10:00:00',2),(3001,'Trial Package (MY)',3,25.00,15,'2025-07-17 11:00:00',3);
/*!40000 ALTER TABLE `packages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `user_packages`
--

LOCK TABLES `user_packages` WRITE;
/*!40000 ALTER TABLE `user_packages` DISABLE KEYS */;
INSERT INTO `user_packages` VALUES (101,1,1001,5,'2025-08-29','ACTIVE'),(102,2,2002,10,'2025-09-29','ACTIVE'),(103,3,1002,13,'2025-08-29','ACTIVE'),(104,4,3001,3,'2025-08-13','ACTIVE'),(105,5,1001,7,'2025-08-29','ACTIVE'),(106,5,1001,5,'2025-08-28','ACTIVE');
/*!40000 ALTER TABLE `user_packages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'alice.s@example.com','$2a$10$SmhB1zgehp158jYqC20MQuouSJU6ZA3gEoXzauq30ldGhxqEK/lXe','Alice Smith',1,'2025-07-20 10:00:00','2025-07-29 09:48:05'),(2,'bob.j@example.com','$2a$10$SmhB1zgehp158jYqC20MQuouSJU6ZA3gEoXzauq30ldGhxqEK/lXe','Bob Johnson',1,'2025-07-21 11:00:00','2025-07-29 09:48:05'),(3,'charlie.b@example.com','$2a$10$SmhB1zgehp158jYqC20MQuouSJU6ZA3gEoXzauq30ldGhxqEK/lXe','Charlie Brown',1,'2025-07-22 12:00:00','2025-07-29 09:48:05'),(4,'diana.p@example.com','$2a$10$SmhB1zgehp158jYqC20MQuouSJU6ZA3gEoXzauq30ldGhxqEK/lXe','Diana Prince',1,'2025-07-23 13:00:00','2025-07-29 09:48:05'),(5,'banbann@gmail.com','$2a$10$waJpQLdIn/UZR0VKEdlnSOYP1HrKhu7L3EQSoVePjS0hRT1iH57CC','Ban Bann',1,'2025-07-29 15:30:00','2025-07-29 19:20:25');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping data for table `waitlists`
--

LOCK TABLES `waitlists` WRITE;
/*!40000 ALTER TABLE `waitlists` DISABLE KEYS */;
INSERT INTO `waitlists` VALUES (1,4,10004,'2025-07-29 15:00:00','CONVERTED_TO_BOOKED',101,0),(3,3,10002,'2025-07-30 02:20:12','CONVERTED_TO_BOOKED',103,0);
/*!40000 ALTER TABLE `waitlists` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-30 13:14:56
