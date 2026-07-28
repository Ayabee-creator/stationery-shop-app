# Multi-PC Network Setup Guide (Spring Boot + Android Studio)

This document explains how to set up and run the **Stationery Shop App** when the Spring Boot backend and MySQL database run on **PC #1** and the Android Studio frontend runs on **PC #2** across different Wi-Fi networks (e.g., Campus Wi-Fi, Hotspot, or Remote networks).

---

## Architecture Overview

```text
+-----------------------------------+             +-----------------------------------+
|               PC #1               |             |               PC #2               |
|  [ MySQL ]                        |             |  [ Android Studio ]               |
|     ^                             |  Internet   |     |                             |
|     |                             |  <=======>  |     v                             |
|  [ Spring Boot ] <-- [ ngrok ] <--|-- Tunnel ---|-- [ Retrofit Client ]             |
|    (Port 8080)                    |             |  (Base URL: ngrok public URL)     |
+-----------------------------------+             +-----------------------------------+