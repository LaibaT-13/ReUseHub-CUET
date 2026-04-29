# ♻️ ReUseHub-CUET

A second-hand marketplace platform exclusively designed for the students and community of **Chittagong University of Engineering & Technology (CUET)**. ReUseHub enables students to buy, sell, and exchange used items within a trusted campus environment.

> 🎓 **Academic Project** — Database Management System (DBMS) | Level-2, Term-II  
> 📍 Department of Computer Science & Engineering, CUET

---

## 📌 Table of Contents

- [About the Project](#about-the-project)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Backend Setup](#backend-setup)
  - [Frontend Setup](#frontend-setup)
- [Environment Configuration](#environment-configuration)
- [Contributors](#contributors)

---

## 📖 About the Project

ReUseHub is a full-stack web application built as a DBMS course project. It provides a secure and convenient platform where CUET students can post items for sale or exchange, communicate with buyers/sellers via chat, and manage their listings — all within a verified campus community.

The platform supports OTP-based email verification, JWT-secured authentication, role-based access control (User & Admin), and a full-featured admin dashboard for moderation.

---

## ✨ Features

### 👤 User Features
- Register & login with CUET email verification via OTP
- Post items for sale with images, descriptions, and pricing
- Browse and search all available listings
- View detailed item pages
- Chat with sellers/buyers in real time
- Report suspicious listings or users
- Edit profile and manage own listings
- Forgot password / Reset password flow

### 🛡️ Admin Features
- Admin dashboard with overview statistics
- Manage all users (view, promote, deactivate)
- Manage all items (view, remove)
- Review and resolve user-submitted reports
- Promote users to admin role

---

## 🛠️ Tech Stack

### Backend
| Technology | Purpose |
|---|---|
| Java 17+ | Core language |
| Spring Boot | REST API framework |
| Spring Security | Authentication & authorization |
| JWT (jjwt) | Token-based auth |
| Spring Data JPA / Hibernate | ORM & database access |
| MySQL | Relational database |
| Spring Mail (Gmail SMTP) | OTP email delivery |
| Lombok | Boilerplate reduction |
| Maven | Build tool |

### Frontend
| Technology | Purpose |
|---|---|
| React 18 | UI framework |
| TypeScript | Type-safe JavaScript |
| Vite | Build tool & dev server |
| React Router v7 | Client-side routing |
| Tailwind CSS | Utility-first styling |
| Lucide React | Icon library |

---

## 📁 Project Structure

```
ReUseHub-CUET/
├── java-backend/                  # Spring Boot REST API
│   └── src/main/java/com/reusehubJava/backend/
│       ├── controller/            # REST endpoints
│       │   ├── AuthController.java
│       │   ├── UserController.java
│       │   ├── ItemController.java
│       │   ├── MessageController.java
│       │   ├── ReportController.java
│       │   ├── AdminController.java
│       │   ├── AdminReportController.java
│       │   └── ImageController.java
│       ├── model/                 # JPA entity classes
│       │   ├── User.java
│       │   ├── Item.java
│       │   ├── Message.java
│       │   ├── Report.java
│       │   └── Admin.java
│       ├── repository/            # Spring Data JPA repositories
│       ├── service/               # Business logic
│       ├── dto/                   # Data Transfer Objects
│       └── security/              # JWT filters & Spring Security config
│
└── java-frontend/                 # React + TypeScript frontend
    └── src/
        ├── pages/                 # Route-level page components
        │   ├── Homepage.tsx
        │   ├── Login.tsx
        │   ├── PostItem.tsx
        │   ├── ItemDetail.tsx
        │   ├── Chat.tsx
        │   ├── Profile.tsx
        │   ├── MyItems.tsx
        │   ├── SearchResults.tsx
        │   ├── Reports.tsx
        │   ├── AdminDashboard.tsx
        │   ├── AdminItems.tsx
        │   ├── AdminUsers.tsx
        │   ├── ForgotPassword.tsx
        │   └── ResetPassword.tsx
        ├── components/            # Reusable UI components
        │   ├── Navbar.tsx
        │   ├── ItemCard.tsx
        │   ├── OTPVerification.tsx
        │   ├── PaymentModal.tsx
        │   └── RouteGuard.tsx
        ├── context/               # React context (Auth, Theme, Notification)
        ├── services/              # API service layer
        ├── hooks/                 # Custom React hooks
        └── utils/                 # Utility functions
```

---

## 🚀 Getting Started

### Prerequisites

Make sure you have the following installed:

- [Java 17+](https://adoptium.net/)
- [Maven 3.8+](https://maven.apache.org/)
- [Node.js 18+](https://nodejs.org/)
- [MySQL 8+](https://www.mysql.com/)
- A Gmail account with an [App Password](https://support.google.com/accounts/answer/185833) for OTP emails

---

### Backend Setup

**1. Clone the repository**
```bash
git clone https://github.com/LaibaT-13/ReUseHub-CUET.git
cd ReUseHub-CUET/java-backend
```

**2. Create MySQL database**
```sql
CREATE DATABASE reusehub;
CREATE USER 'reusehub'@'localhost' IDENTIFIED BY 'reusehub123';
GRANT ALL PRIVILEGES ON reusehub.* TO 'reusehub'@'localhost';
FLUSH PRIVILEGES;
```

**3. Configure `application.properties`**

Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/reusehub?useSSL=false&serverTimezone=UTC&createDatabaseIfNotExist=true
spring.datasource.username=reusehub
spring.datasource.password=reusehub123

spring.mail.username=your-email@gmail.com
spring.mail.password=your-gmail-app-password

jwt.secret=your-very-long-secret-key
```

> ⚠️ **Important:** Never commit real credentials to GitHub. Use environment variables or a `.env` file in production.

**4. Run the backend**
```bash
./mvnw spring-boot:run
```
The API will start at `http://localhost:8080`

---

### Frontend Setup

**1. Navigate to the frontend directory**
```bash
cd ../java-frontend
```

**2. Install dependencies**
```bash
npm install
```

**3. Start the development server**
```bash
npm run dev
```
The app will open at `http://localhost:5173`

---

## ⚙️ Environment Configuration

| Config | Default Value | Description |
|---|---|---|
| `server.port` | `8080` | Backend server port |
| `spring.datasource.url` | `localhost:3306/reusehub` | MySQL connection string |
| `jwt.expiration` | `604800000` (7 days) | JWT token validity in ms |
| `reusehub.allowNonCuetEmails` | `true` | Set to `false` to restrict to CUET emails only |
| `spring.mail.host` | `smtp.gmail.com` | SMTP mail server |

---

## 👩‍💻 Contributors

This project was developed as part of the **Database Management System (CSE-252)** course at CUET (Level-2, Term-II, Session: 2022-2023), under the supervision of **Sabiha Anan**, Assistant Professor, Department of CSE.

| Name | Student ID | Role |
|---|---|---|
| Sajnin Akter | 2204070 | Backend Development, Database Design & Queries |
| Debirupa Dutta | 2204071 | Backend Development, Database Design & Queries |
| Laiba Tabassum | 2204077 | Frontend Development, UI/UX Design |
| Tahrima Jahan | 2204078 | Frontend Development, UI/UX Design |

---

## 📄 License

This project is developed for academic purposes at CUET. All rights reserved by the contributors.
