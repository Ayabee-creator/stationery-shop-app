<div align="center">

<img src="https://capsule-render.vercel.app/api?type=rounded&color=gradient&customColorList=6,11,20&height=120&section=header&text=StudyGrid&fontSize=40&fontColor=fff&animation=fadeIn&fontAlignY=50"/>

**A full-stack e-commerce platform for student stationery**

Android App • Spring Boot API • Admin Dashboard • MySQL

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-4-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Android](https://img.shields.io/badge/Android-Material_3-3DDC84?style=flat-square&logo=android&logoColor=white)
![License](https://img.shields.io/badge/License-Educational-blue?style=flat-square)

</div>

---

## Overview

StudyGrid is a complete e-commerce system where students can browse and buy stationery supplies through a mobile app, while the shop owner manages everything from a web-based admin dashboard. The Spring Boot backend ties it all together.

```
stationery-shop-app/
├── 📱 android-app/      → Android mobile app (Java + Material Design 3)
├── ⚙️ backend-api/      → Spring Boot REST API (Java + MySQL)
├── 🖥️ admin-panel/      → Admin dashboard (HTML/CSS/JS + Chart.js)
├── 🗄️ database/         → SQL schema and seed data
├── 📄 DEPLOYMENT.md     → Cloud hosting guide (Railway + Netlify)
└── 📄 README.md         → You are here
```

---

## Tech Stack

| Layer | Technology |
|:------|:-----------|
| **Mobile** | Java, XML, Retrofit, Glide, Material Design 3 |
| **Backend** | Java 17, Spring Boot 4, JDBC, MySQL |
| **Admin Web** | HTML5, CSS3, Vanilla JS, Chart.js |
| **Database** | MySQL 8.0 (8 tables, relational schema) |
| **Build** | Gradle (Android), Maven (Backend) |
| **Deploy** | Railway (API + DB), Netlify (Admin Panel) |

---

## Features at a Glance

<table>
<tr>
<td width="33%" valign="top">

### 📱 Android App

- Register & login with sessions
- Browse products by category
- Search by name/description
- Product detail with reviews
- Shopping cart with checkout
- Wishlist (save for later)
- Order history with status tracking
- Profile management
- Material Design 3 UI
- Student discount banners

</td>
<td width="33%" valign="top">

### 🖥️ Admin Panel

- Dashboard with live stats
- Sales chart (Chart.js)
- Order management (single & bulk)
- Product CRUD + image preview
- Quick restock (+50 button)
- Customer overview
- Revenue analytics
- Invoice generation & print
- Dark/light mode toggle
- Global search
- Auto-refresh (30s)

</td>
<td width="33%" valign="top">

### ⚙️ Backend API

- 30+ REST endpoints
- Product search & filter
- User auth (register/login)
- Cart management
- Wishlist system
- Order placement + stock deduction
- Review system (1–5 stars)
- Admin analytics endpoints
- Bulk operations
- CORS configured

</td>
</tr>
</table>

---

## API Endpoints

<details>
<summary><strong>Products</strong></summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/products` | All products with ratings |
| GET | `/products/{id}` | Single product |
| GET | `/products/search?name=` | Search by name |
| GET | `/products/category/{id}` | Filter by category |
| GET | `/products/popular` | Top sellers |
| GET | `/categories` | All categories |

</details>

<details>
<summary><strong>Users</strong></summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/users/register` | Create account |
| POST | `/users/login` | Login |
| GET | `/users/{id}` | Get profile |
| PUT | `/users/{id}` | Update profile |

</details>

<details>
<summary><strong>Cart & Wishlist</strong></summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/cart` | Add to cart |
| GET | `/cart/user/{userId}` | Get cart |
| PUT | `/cart/{id}` | Update quantity |
| DELETE | `/cart/{id}` | Remove item |
| DELETE | `/cart/user/{userId}` | Clear cart |
| POST | `/wishlists` | Add to wishlist |
| GET | `/wishlists/user/{userId}` | Get wishlist |
| DELETE | `/wishlists/{id}` | Remove item |

</details>

<details>
<summary><strong>Orders & Reviews</strong></summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/orders` | Place order |
| GET | `/orders/user/{userId}` | Order history |
| GET | `/orders/{orderId}` | Order detail |
| POST | `/reviews` | Submit review |
| GET | `/reviews/product/{productId}` | Product reviews |

</details>

<details>
<summary><strong>Admin</strong></summary>

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/admin/dashboard` | Stats overview |
| GET | `/admin/orders` | All orders |
| PUT | `/admin/orders/{id}/status` | Update status |
| PUT | `/admin/orders/bulk-status` | Bulk update |
| GET | `/admin/customers` | All customers |
| GET | `/admin/revenue-chart` | Revenue/day (30d) |
| GET | `/admin/top-products` | Top sellers |
| GET | `/admin/activity` | Recent activity |
| POST | `/admin/products` | Add product |
| PUT | `/admin/products/{id}` | Edit product |
| DELETE | `/admin/products/{id}` | Delete product |
| PUT | `/admin/products/{id}/stock` | Update stock |

</details>

---

## Database Schema

```
┌──────────┐     ┌────────────┐     ┌──────────────┐
│  users   │────<│   orders   │────<│ order_items  │
└──────────┘     └────────────┘     └──────────────┘
     │                                      │
     │           ┌────────────┐     ┌──────────────┐
     ├──────────<│ cart_items │────>│  products    │
     │           └────────────┘     └──────────────┘
     │                                 │        │
     │           ┌────────────┐        │   ┌────────────┐
     ├──────────<│ wishlists  │────────┘   │ categories │
     │           └────────────┘            └────────────┘
     │           ┌────────────┐
     └──────────<│  reviews   │
                 └────────────┘
```

---

## Quick Start

### 1. Database

```bash
# Install MySQL 8.0, then run:
mysql -u root -p < database/stationery_shop.sql
mysql -u root -p < database/add_new_products.sql
mysql -u root -p < database/update_product_images.sql
```

### 2. Backend API

```properties
# backend-api/src/main/resources/application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/stationery_shop
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
server.port=8080
```

```bash
cd backend-api
./mvnw spring-boot:run
# Test: http://localhost:8080/products
```

### 3. Android App

1. Open `android-app/` in Android Studio
2. Set `BASE_URL` in `RetrofitClient.java`:
   - Emulator: `http://10.0.2.2:8080/`
   - Physical device: `http://YOUR_PC_IP:8080/`
3. Run on device

### 4. Admin Panel

1. Open `admin-panel/index.html` in browser
2. Login: `admin` / `admin123`
3. Done — no install needed

---

## Deployment

Deployable for free using Railway (backend + DB) and Netlify (admin panel).  
Full guide: [DEPLOYMENT.md](./DEPLOYMENT.md)

| Service | Host | Cost |
|---------|------|------|
| Backend API | Railway | Free tier |
| MySQL DB | Railway | Free tier |
| Admin Panel | Netlify | Free |

---

## Design System

The Android app uses a custom color palette with Material Design 3:

| Color | Hex | Usage |
|-------|-----|-------|
| Electric Lavender | `#8B5CF6` | Primary actions, headers |
| Cyber Cyan | `#06B6D4` | Accents, links |
| Neo-Mint | `#10B981` | Success states, stock badges |

Glassmorphism-inspired cards, 16dp rounded corners, dark/light mode support.

---

## Project Status

- [x] User authentication (register/login)
- [x] Product browsing with categories & search
- [x] Shopping cart with checkout
- [x] Wishlist
- [x] Order placement & history
- [x] Review system
- [x] Admin dashboard with analytics
- [x] Admin product/order management
- [x] Dark/light mode (admin panel)
- [x] Cloud deployment ready

---

<div align="center">

Built as a student project — Android + Spring Boot + MySQL

<img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=6,11,20&height=80&section=footer"/>

</div>
