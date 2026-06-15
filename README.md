# Stationery Shop App

This is a stationery shop Android application connected to a Spring Boot backend API and a MySQL database.

The system allows users to register, log in, view products, place orders, and submit product reviews.

## Project Structure

```text
stationery-shop-app
├── backend-api
│   └── Spring Boot API
│
├── android-app
│   └── Android Studio mobile app
│
├── database
│   └── stationery_shop.sql
│
└── README.md
```

## Tech Stack

### Backend

* Java
* Spring Boot
* MySQL
* DataSource
* PreparedStatement
* Maven

### Android App

* Java
* XML layouts
* Retrofit
* Android Studio

### Database

* MySQL
* Database name: `stationery_shop`

## Backend API

The backend is inside:

```text
backend-api
```

To open the backend:

1. Open the `backend-api` folder in VS Code.
2. Make sure MySQL is running.
3. Create a local `application.properties` file using the example file.

The real `application.properties` file is not uploaded to GitHub because it contains the local MySQL password.

Use this file as a guide:

```text
backend-api/src/main/resources/application-example.properties
```

Create your own local file:

```text
backend-api/src/main/resources/application.properties
```

Example:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/stationery_shop
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
server.port=8080
```

To run the backend:

```bash
cd backend-api
.\mvnw spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

## Working API Endpoints

### Products

```http
GET /products
GET /products/{id}
GET /products/search?name=pen
```

### Users

```http
GET /users/{id}
POST /users/register
POST /users/login
```

### Orders

```http
POST /orders
GET /orders/user/{userId}
GET /orders/{orderId}
```

### Reviews

```http
POST /reviews
GET /reviews/product/{productId}
```

## Database Setup

The database SQL file is inside:

```text
database/stationery_shop.sql
```

To set up the database on a new PC:

1. Open MySQL Workbench.
2. Connect to the local MySQL server.
3. Open the SQL file:

```text
database/stationery_shop.sql
```

4. Run the script.
5. It will create the `stationery_shop` database with all tables and sample data.

The database includes these tables:

```text
categories
products
users
orders
order_items
reviews
```

## Android App

The Android app is inside:

```text
android-app
```

To open it:

1. Open Android Studio.
2. Click **Open**.
3. Select the `android-app` folder.
4. Wait for Gradle sync to finish.
5. Connect a real Android phone or use an emulator.
6. Run the app.

The Android app uses Retrofit to call the Spring Boot API.

The Retrofit base URL is inside:

```text
android-app/app/src/main/java/com/example/stationeryshopapp/api/RetrofitClient.java
```

Example:

```java
private static final String BASE_URL = "http://192.168.1.117:8080/";
```

Important:

* If the backend is running on the same PC as the Android emulator, use:

```java
http://10.0.2.2:8080/
```

* If the backend is running on another PC, use that PC's IPv4 address:

```java
http://YOUR_BACKEND_PC_IP:8080/
```

Example:

```java
http://192.168.1.117:8080/
```

The phone and backend PC must be connected to the same Wi-Fi network.

## Current Android Features

The Android app currently has:

```text
Register screen
Login screen
Home screen
Product loading using Retrofit
Session storage using SharedPreferences
```

After login, the user goes to the home page.

## GitHub Team Workflow

Before starting work, always run:

```bash
git pull
```

After making changes, run:

```bash
git status
git add .
git commit -m "Explain what you changed"
git push
```

Example:

```bash
git add .
git commit -m "Add home page menu"
git push
```

## Team Rules

To avoid Git conflicts:

* Do not edit the same file at the same time.
* Always pull before starting work.
* Always push after finishing work.
* Write clear commit messages.
* Do not upload passwords.
* Do not upload build folders.

## Important Ignored Files

These files/folders should not be uploaded:

```text
backend-api/target/
backend-api/src/main/resources/application.properties
android-app/.gradle/
android-app/local.properties
android-app/.idea/
android-app/app/build/
android-app/build/
```

These are ignored using `.gitignore`.

## Notes

Passwords are currently stored as plain text in the database because this is a student project. In a real production system, passwords should be hashed securely.

## Main Developer

Ayabee-creator
