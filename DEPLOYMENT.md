# StudyGrid - Deployment Guide

This guide will get your app running online 24/7 so students can download it from the Play Store.

---

## What Gets Deployed Where

| Component | Hosted On | Cost |
|-----------|-----------|------|
| Spring Boot Backend API | Railway | Free tier (500 hours/month) |
| MySQL Database | Railway | Free tier (1GB) |
| Admin Panel Website | Netlify | Free |

---

## Step 1: Deploy MySQL Database on Railway

1. Go to [railway.app](https://railway.app) and sign up with GitHub
2. Click **"New Project"** → **"Provision MySQL"**
3. Once created, click on the MySQL service
4. Go to **"Variables"** tab — you'll see:
   - `MYSQL_HOST`
   - `MYSQL_PORT`
   - `MYSQL_DATABASE`
   - `MYSQL_USER`
   - `MYSQL_PASSWORD`
5. Click **"Connect"** tab → copy the **MySQL connection URL**
6. Connect to this database using MySQL Workbench:
   - Use the host, port, user, and password from Railway
7. Run the `database/stationery_shop.sql` file to create all tables and data
8. Run `database/add_new_products.sql` to add the new products

---

## Step 2: Deploy Spring Boot Backend on Railway

1. In the same Railway project, click **"New Service"** → **"GitHub Repo"**
2. Select your `stationery-shop-app` repository
3. Set the **Root Directory** to: `backend-api`
4. Go to the **"Variables"** tab and add:

```
DATABASE_URL=jdbc:mysql://YOUR_RAILWAY_MYSQL_HOST:PORT/railway
DATABASE_USER=root
DATABASE_PASSWORD=YOUR_RAILWAY_MYSQL_PASSWORD
PORT=8080
```

5. Railway will detect the Dockerfile and build automatically
6. Once deployed, go to **"Settings"** → **"Generate Domain"**
7. You'll get a URL like: `https://studygrid-backend-production.up.railway.app`
8. Test it: open `https://your-url.up.railway.app/products` in a browser — you should see JSON

---

## Step 3: Update Your App to Use the Live Backend

### Android App
Open `android-app/app/src/main/java/.../api/RetrofitClient.java`

Change:
```java
private static final String BASE_URL = "https://your-railway-url.up.railway.app/";
```

### Admin Panel
Open `admin-panel/js/api.js`

Change:
```javascript
const API_BASE = window.STUDYGRID_API || 'https://your-railway-url.up.railway.app';
```

---

## Step 4: Deploy Admin Panel on Netlify

1. Go to [netlify.com](https://www.netlify.com) and sign up
2. Click **"Add new site"** → **"Deploy manually"**
3. Drag and drop the entire `admin-panel` folder
4. Done! You'll get a URL like: `https://studygrid-admin.netlify.app`
5. (Optional) Set a custom domain in Netlify settings

---

## Ongoing Maintenance

- **Adding products**: Use the Admin Panel (no code changes needed)
- **Updating the app**: Push to GitHub → Railway auto-redeploys the backend
- **Checking sales**: Open the Admin Panel dashboard
- **Updating stock**: Use the "+50 Restock" button in the admin panel

---

## Environment Variables Summary

### Railway Backend
| Variable | Example |
|----------|---------|
| `DATABASE_URL` | `jdbc:mysql://containers-us-west-xxx.railway.app:6312/railway` |
| `DATABASE_USER` | `root` |
| `DATABASE_PASSWORD` | `abc123xyz` |
| `PORT` | `8080` |

---

## Troubleshooting

**App shows "Error connecting":**
- Check that Railway backend is running (green status)
- Verify BASE_URL in RetrofitClient.java matches Railway domain
- Ensure the URL ends with `/`

**Admin panel shows "Failed to load":**
- Check browser console for errors (F12)
- Verify API_BASE in api.js matches Railway domain
- Make sure CORS is enabled (CorsConfig.java is in the project)

**Database connection fails on Railway:**
- Double-check DATABASE_URL format includes `jdbc:mysql://`
- Verify username and password match Railway's MySQL credentials
- Make sure you ran the SQL scripts on the Railway database

---

## Cost Summary

| Item | Cost |
|------|------|
| Railway (backend + DB) | Free (500 hrs/month) |
| Netlify (admin panel) | Free |
| **Total to go live** | **R0** |

After the free tier, Railway is about $5/month if you need it running 24/7 without sleeping.
