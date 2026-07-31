CREATE DATABASE  IF NOT EXISTS `stationery_shop` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `stationery_shop`;
-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: stationery_shop
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `categories`
--

DROP TABLE IF EXISTS `categories`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `categories` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `categories`
--

LOCK TABLES `categories` WRITE;
/*!40000 ALTER TABLE `categories` DISABLE KEYS */;
INSERT INTO `categories` VALUES (1,'Pens & Pencils','Ballpoint pens, pencils, markers and highlighters'),(2,'Notebooks','Notebooks, journals, notepads and diaries'),(3,'Art Supplies','Paints, brushes, canvases and drawing tools'),(4,'Office Supplies','Staplers, scissors, tape, clips and organizers'),(5,'Paper','Printing paper, coloured paper and cardstock');
/*!40000 ALTER TABLE `categories` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_items`
--

DROP TABLE IF EXISTS `order_items`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `order_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  `unit_price` decimal(10,2) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `order_id` (`order_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_items`
--

LOCK TABLES `order_items` WRITE;
/*!40000 ALTER TABLE `order_items` DISABLE KEYS */;
INSERT INTO `order_items` VALUES (1,1,1,2,1.99),(2,1,4,1,6.99),(3,2,6,1,12.99),(4,2,7,1,8.75),(5,3,10,1,5.99),(6,4,1,2,1.99),(7,4,4,1,6.99);
/*!40000 ALTER TABLE `order_items` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `total_amount` decimal(10,2) NOT NULL,
  `status` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending',
  `ordered_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,1,10.47,'delivered','2026-06-12 21:15:39'),(2,2,21.74,'shipped','2026-06-12 21:15:39'),(3,1,5.99,'pending','2026-06-12 21:15:39'),(4,4,10.97,'pending','2026-06-15 00:50:08');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `products`
--

DROP TABLE IF EXISTS `products`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `category_id` int NOT NULL,
  `name` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci,
  `price` decimal(10,2) NOT NULL,
  `stock_quantity` int NOT NULL DEFAULT '0',
  `image_url` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `category_id` (`category_id`),
  CONSTRAINT `products_ibfk_1` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=32 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `products`
--

LOCK TABLES `products` WRITE;
/*!40000 ALTER TABLE `products` DISABLE KEYS */;
INSERT INTO `products` VALUES (1,1,'Blue Ballpoint Pen','Smooth writing ballpoint pen, blue ink',1.99,144,'https://images.unsplash.com/photo-1585336261022-680e295ce3fe?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(2,1,'HB Pencil Pack','Pack of 10 HB pencils, great for writing',3.49,80,'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(3,1,'Yellow Highlighter','Bright yellow highlighter, chisel tip',2.25,200,'https://images.unsplash.com/photo-1607703703520-bb638e84caf2?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(4,2,'A5 Lined Notebook','200 pages, hardcover, lined pages',6.99,59,'https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(5,2,'Spiral Journal','A4 spiral bound journal, 150 pages',5.49,45,'https://images.unsplash.com/photo-1517842645767-c639042777db?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(6,3,'Watercolour Paint Set','24 colour watercolour set with brush',12.99,30,'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(7,3,'Sketching Pencil Set','Set of 8 sketching pencils, 2H to 8B',8.75,40,'https://images.unsplash.com/photo-1596464716127-f2a82984de30?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(8,4,'Stapler','Standard desktop stapler, uses 26/6 staples',4.99,55,'https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(9,4,'Scissors 20cm','Stainless steel scissors, comfortable grip',3.99,70,'https://images.unsplash.com/photo-1582281298055-e25b84a30b0b?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(10,5,'A4 Printing Paper','500 sheets, 80gsm white printing paper',5.99,90,'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop','2026-06-12 21:15:19'),(11,1,'Zebra Mildliner Set (5 Pack)','Pastel double-ended highlighters, popular for aesthetic notes and bullet journaling',9.99,120,'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(12,1,'Black Gel Pen 0.5mm','Smooth-flow black gel ink pen, perfect for neat handwriting and exam papers',1.49,300,'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(13,1,'Colored Fineliners Set (10 Pack)','Ultra-fine 0.4mm tips in vibrant colors for mind maps, diagrams, and color-coding notes',7.99,85,'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(14,1,'Mechanical Pencil 0.5mm','Premium mechanical pencil with rubber grip and built-in eraser, includes 2 lead refills',3.99,150,'https://images.unsplash.com/photo-1589330694653-ded6df03f754?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(15,1,'Red Marking Pen','Fine-tip red ballpoint pen, ideal for self-marking practice papers and corrections',1.79,200,'https://images.unsplash.com/photo-1585336261022-680e295ce3fe?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(16,2,'Dot Grid Bullet Journal A5','180 pages, 120gsm thick paper, lay-flat binding, perfect for bullet journaling and planning',12.99,60,'https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(17,2,'Index Card Pack (200 Cards)','Ruled white index cards 3x5 inch, essential for exam revision and flashcards',4.49,180,'https://images.unsplash.com/photo-1568702846914-96b305d2ead1?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(18,2,'Sticky Notes Multicolor (8 Pads)','Assorted neon and pastel sticky notes in various sizes for bookmarking and reminders',5.99,200,'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(19,2,'A4 Exam Pad (100 Pages)','Ruled feint and margin exam pad, standard for tests and assignments',3.49,250,'https://images.unsplash.com/photo-1517842645767-c639042777db?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(20,2,'A5 Hardcover Planner 2026','Weekly and monthly planner with goal-setting pages, habit tracker, and note sections',14.99,45,'https://images.unsplash.com/photo-1506784365847-bbad939e9335?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(21,3,'Washi Tape Set (10 Rolls)','Decorative paper tape in pastel and floral patterns, great for planners and scrapbooking',6.99,95,'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(22,3,'Colored Pencils 24-Pack','Professional-grade colored pencils with soft core for smooth blending and vibrant color',11.49,70,'https://images.unsplash.com/photo-1596464716127-f2a82984de30?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(23,3,'Fine Tip Markers (12 Pack)','Dual-tip brush and fine markers for calligraphy, lettering, and planner decoration',9.49,55,'https://images.unsplash.com/photo-1607703703520-bb638e84caf2?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(24,3,'A3 Sketch Pad 50 Sheets','160gsm heavyweight cartridge paper, spiral-bound, suitable for pencil, charcoal and pastel',8.99,40,'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(25,4,'Desk Organizer Caddy','Multi-compartment mesh desk organizer for pens, scissors, sticky notes and accessories',8.99,65,'https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(26,4,'Binder Clips Assorted (50 Pack)','Mixed size metal binder clips in black, essential for keeping documents together',3.49,140,'https://images.unsplash.com/photo-1582281298055-e25b84a30b0b?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(27,4,'Clear Plastic Folders (10 Pack)','A4 transparent document sleeves for assignment submission and organization',4.99,160,'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(28,4,'Scientific Calculator','Casio-style 240+ function scientific calculator, exam-approved for STEM courses',15.99,50,'https://images.unsplash.com/photo-1564939558297-fc396f18e5c7?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(29,5,'Graph Paper Pad A4 (50 Sheets)','5mm squared grid paper, ideal for engineering drawings, math graphs and technical sketches',4.99,110,'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(30,5,'Tracing Paper Pack (25 Sheets)','A3 translucent tracing paper 90gsm for architecture, design overlays and art transfers',6.49,60,'https://images.unsplash.com/photo-1517842645767-c639042777db?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00'),(31,5,'Colored Cardstock Pack (30 Sheets)','A4 assorted vibrant colors, 200gsm heavyweight card for presentations and craft projects',7.99,80,'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop&q=80','2026-07-31 00:00:00');
/*!40000 ALTER TABLE `products` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reviews`
--

DROP TABLE IF EXISTS `reviews`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reviews` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `rating` int NOT NULL,
  `comment` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `user_id` (`user_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `reviews_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE,
  CONSTRAINT `chk_rating` CHECK ((`rating` between 1 and 5))
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reviews`
--

LOCK TABLES `reviews` WRITE;
/*!40000 ALTER TABLE `reviews` DISABLE KEYS */;
INSERT INTO `reviews` VALUES (1,1,1,5,'Great pen, writes very smoothly!','2026-06-12 21:15:57'),(2,1,4,4,'Good notebook, paper quality is excellent','2026-06-12 21:15:57'),(3,2,6,5,'Amazing paint set, highly recommend','2026-06-12 21:15:57'),(4,3,7,4,'Very good pencil set for the price','2026-06-12 21:15:57'),(9,4,1,5,'This pen writes very smoothly.','2026-06-15 00:54:11');
/*!40000 ALTER TABLE `reviews` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `address` text COLLATE utf8mb4_unicode_ci,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'john_doe','john@email.com','hashed_password_1','0712345678','12 Main Street, Cape Town','2026-06-12 21:15:31'),(2,'sarah_smith','sarah@email.com','hashed_password_2','0823456789','45 Oak Avenue, Johannesburg','2026-06-12 21:15:31'),(3,'mike_jones','mike@email.com','hashed_password_3',NULL,NULL,'2026-06-12 21:15:31'),(4,'Test Student','test23@student.com','12345','0712345678','Port Elizabeth','2026-06-15 00:28:13'),(5,'ayabee','ayabee@gmail.com','ayabee','0693845652','ayabee','2026-06-15 04:47:24'),(6,'Jonathan','Jonathan@gmail.com','jona','06542351638','hfff','2026-06-15 18:38:07'),(7,'aya','aya@gmail.com','aya','0693854162','ffdgxgx','2026-06-15 19:10:12');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-15 21:06:23


-- ===================================================
-- New tables added for Stationery Za features
-- ===================================================

--
-- Table structure for table `wishlists`
--

DROP TABLE IF EXISTS `wishlists`;
CREATE TABLE `wishlists` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_wishlist_item` (`user_id`, `product_id`),
  KEY `user_id` (`user_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `wishlists_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `wishlists_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Table structure for table `cart_items`
--

DROP TABLE IF EXISTS `cart_items`;
CREATE TABLE `cart_items` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `product_id` int NOT NULL,
  `quantity` int NOT NULL DEFAULT 1,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_cart_item` (`user_id`, `product_id`),
  KEY `user_id` (`user_id`),
  KEY `product_id` (`product_id`),
  CONSTRAINT `cart_items_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `cart_items_ibfk_2` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
