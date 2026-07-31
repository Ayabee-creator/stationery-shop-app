-- ===================================================
-- Add high-demand student stationery products
-- Run this against your stationery_shop database
-- ===================================================

USE stationery_shop;

-- ===== Pens & Pencils (category_id = 1) =====

INSERT INTO products (category_id, name, description, price, stock_quantity, image_url) VALUES
(1, 'Zebra Mildliner Set (5 Pack)', 'Pastel double-ended highlighters, popular for aesthetic notes and bullet journaling', 9.99, 120, 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=400&h=400&fit=crop&q=80'),
(1, 'Black Gel Pen 0.5mm', 'Smooth-flow black gel ink pen, perfect for neat handwriting and exam papers', 1.49, 300, 'https://images.unsplash.com/photo-1583485088034-697b5bc54ccd?w=400&h=400&fit=crop&q=80'),
(1, 'Colored Fineliners Set (10 Pack)', 'Ultra-fine 0.4mm tips in vibrant colors for mind maps, diagrams, and color-coding notes', 7.99, 85, 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400&h=400&fit=crop&q=80'),
(1, 'Mechanical Pencil 0.5mm', 'Premium mechanical pencil with rubber grip and built-in eraser, includes 2 lead refills', 3.99, 150, 'https://images.unsplash.com/photo-1589330694653-ded6df03f754?w=400&h=400&fit=crop&q=80'),
(1, 'Red Marking Pen', 'Fine-tip red ballpoint pen, ideal for self-marking practice papers and corrections', 1.79, 200, 'https://images.unsplash.com/photo-1585336261022-680e295ce3fe?w=400&h=400&fit=crop&q=80');

-- ===== Notebooks (category_id = 2) =====

INSERT INTO products (category_id, name, description, price, stock_quantity, image_url) VALUES
(2, 'Dot Grid Bullet Journal A5', '180 pages, 120gsm thick paper, lay-flat binding, perfect for bullet journaling and planning', 12.99, 60, 'https://images.unsplash.com/photo-1531346878377-a5be20888e57?w=400&h=400&fit=crop&q=80'),
(2, 'Index Card Pack (200 Cards)', 'Ruled white index cards 3x5 inch, essential for exam revision and flashcards', 4.49, 180, 'https://images.unsplash.com/photo-1568702846914-96b305d2ead1?w=400&h=400&fit=crop&q=80'),
(2, 'Sticky Notes Multicolor (8 Pads)', 'Assorted neon and pastel sticky notes in various sizes for bookmarking and reminders', 5.99, 200, 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop&q=80'),
(2, 'A4 Exam Pad (100 Pages)', 'Ruled feint and margin exam pad, standard for tests and assignments', 3.49, 250, 'https://images.unsplash.com/photo-1517842645767-c639042777db?w=400&h=400&fit=crop&q=80'),
(2, 'A5 Hardcover Planner 2026', 'Weekly and monthly planner with goal-setting pages, habit tracker, and note sections', 14.99, 45, 'https://images.unsplash.com/photo-1506784365847-bbad939e9335?w=400&h=400&fit=crop&q=80');

-- ===== Art Supplies (category_id = 3) =====

INSERT INTO products (category_id, name, description, price, stock_quantity, image_url) VALUES
(3, 'Washi Tape Set (10 Rolls)', 'Decorative paper tape in pastel and floral patterns, great for planners and scrapbooking', 6.99, 95, 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400&h=400&fit=crop&q=80'),
(3, 'Colored Pencils 24-Pack', 'Professional-grade colored pencils with soft core for smooth blending and vibrant color', 11.49, 70, 'https://images.unsplash.com/photo-1596464716127-f2a82984de30?w=400&h=400&fit=crop&q=80'),
(3, 'Fine Tip Markers (12 Pack)', 'Dual-tip brush and fine markers for calligraphy, lettering, and planner decoration', 9.49, 55, 'https://images.unsplash.com/photo-1607703703520-bb638e84caf2?w=400&h=400&fit=crop&q=80'),
(3, 'A3 Sketch Pad 50 Sheets', '160gsm heavyweight cartridge paper, spiral-bound, suitable for pencil, charcoal and pastel', 8.99, 40, 'https://images.unsplash.com/photo-1513364776144-60967b0f800f?w=400&h=400&fit=crop&q=80');

-- ===== Office Supplies (category_id = 4) =====

INSERT INTO products (category_id, name, description, price, stock_quantity, image_url) VALUES
(4, 'Desk Organizer Caddy', 'Multi-compartment mesh desk organizer for pens, scissors, sticky notes and accessories', 8.99, 65, 'https://images.unsplash.com/photo-1612287230202-1ff1d85d1bdf?w=400&h=400&fit=crop&q=80'),
(4, 'Binder Clips Assorted (50 Pack)', 'Mixed size metal binder clips in black, essential for keeping documents together', 3.49, 140, 'https://images.unsplash.com/photo-1582281298055-e25b84a30b0b?w=400&h=400&fit=crop&q=80'),
(4, 'Clear Plastic Folders (10 Pack)', 'A4 transparent document sleeves for assignment submission and organization', 4.99, 160, 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop&q=80'),
(4, 'Scientific Calculator', 'Casio-style 240+ function scientific calculator, exam-approved for STEM courses', 15.99, 50, 'https://images.unsplash.com/photo-1564939558297-fc396f18e5c7?w=400&h=400&fit=crop&q=80');

-- ===== Paper (category_id = 5) =====

INSERT INTO products (category_id, name, description, price, stock_quantity, image_url) VALUES
(5, 'Graph Paper Pad A4 (50 Sheets)', '5mm squared grid paper, ideal for engineering drawings, math graphs and technical sketches', 4.99, 110, 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop&q=80'),
(5, 'Tracing Paper Pack (25 Sheets)', 'A3 translucent tracing paper 90gsm for architecture, design overlays and art transfers', 6.49, 60, 'https://images.unsplash.com/photo-1517842645767-c639042777db?w=400&h=400&fit=crop&q=80'),
(5, 'Colored Cardstock Pack (30 Sheets)', 'A4 assorted vibrant colors, 200gsm heavyweight card for presentations and craft projects', 7.99, 80, 'https://images.unsplash.com/photo-1586075010923-2dd4570fb338?w=400&h=400&fit=crop&q=80');
