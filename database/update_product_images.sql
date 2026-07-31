-- Update product images with real URLs
-- Run this against your stationery_shop database to add product images

USE stationery_shop;

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1585336261022-680e295ce3fe?w=400&h=400&fit=crop' WHERE id = 1;
-- Blue Ballpoint Pen

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=400&h=400&fit=crop' WHERE id = 2;
-- HB Pencil Pack

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1607703703520-bb638e84caf2?w=400&h=400&fit=crop' WHERE id = 3;
-- Yellow Highlighter

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=400&h=400&fit=crop' WHERE id = 4;
-- A5 Lined Notebook

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1517842645767-c639042777db?w=400&h=400&fit=crop' WHERE id = 5;
-- Spiral Journal

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400&h=400&fit=crop' WHERE id = 6;
-- Watercolour Paint Set

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1596464716127-f2a82984de30?w=400&h=400&fit=crop' WHERE id = 7;
-- Sketching Pencil Set

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?w=400&h=400&fit=crop' WHERE id = 8;
-- Stapler

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1582281298055-e25b84a30b0b?w=400&h=400&fit=crop' WHERE id = 9;
-- Scissors 20cm

UPDATE products SET image_url = 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop' WHERE id = 10;
-- A4 Printing Paper
