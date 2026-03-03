# 📦 Inventory Management System - Backend API

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.4-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

A robust and scalable Spring Boot REST API for managing inventory, featuring secure authentication, image uploads, automated email notifications, and professional PDF report generation.

---

## 🚀 Features

- **🔐 Secure Authentication**: JWT-based authentication with Role-Based Access Control (RBAC).
- **📦 Inventory Management**: Full CRUD operations for inventory items with image support.
- **📄 Professional PDF Reports**: Generate detailed PDF invoices for individual items or full inventory reports.
- **📧 Email Notifications**: Automated welcome emails and inventory event notifications (powered by Gmail SMTP).
- **📸 Image Uploads**: Seamless handling of product images with local storage.
- **🔍 Advanced Search**: Search items by PLU code and other attributes.
- **🛠️ Global Exception Handling**: Consistent error responses across all API endpoints.

---

## 🛠️ Tech Stack

- **Framework**: Spring Boot 3.5.4
- **Language**: Java 21
- **Database**: MySQL
- **Security**: Spring Security 6 & JWT
- **Build Tool**: Maven
- **PDF Generation**: iText 7
- **Reporting**: Spring Boot Starter Mail

---

## 📁 Project Structure

```text
src/main/java/com/example/inventory/
├── Config/           # App configurations (Security, Async, Exception Handling)
├── Controllers/      # REST Endpoints (Auth, Inventory, Users, Email)
├── DTO/              # Data Transfer Objects for requests/responses
├── Exceptions/       # Custom business exceptions
├── Model/            # JPA Entities (User, Inventory, Role)
├── Repository/       # Spring Data JPA Repositories
├── Security/         # JWT Filters and Security Utilities
└── Services/         # Business Logic (Inventory, Mail, PDF, User)
```

---

## ⚙️ Getting Started

### Prerequisites

- **JDK 21** or higher
- **Maven 3.6+**
- **MySQL 8.0+**
- **SMTP Server** (e.g., Gmail)

### 1. Database Setup

Create a MySQL database named `inventory_db`:

```sql
CREATE DATABASE inventory_db;
```

### 2. Configuration

Update `src/main/resources/application.properties` with your credentials:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3306/inventory_db
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

# SMTP Configuration (Gmail)
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password

# JWT
app.jwt.secret=your_long_secure_secret_key_here
```

### 3. Run the Application

```bash
mvn spring-boot:run
```
The API will be available at `http://localhost:8085`.

---

## 📑 API Documentation

### Authentication Flow

1. **Register**: `POST /api/v1/auth/register`
2. **Login**: `POST /api/v1/auth/login` -> Returns `accessToken`
3. **Authorize**: Add `Authorization: Bearer <token>` header to subsequent requests.

### Core Endpoints

| Method | Endpoint | Description | Access |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/v1/inventory/create` | Create item (Multipart Form) | User/Admin |
| `GET` | `/api/v1/inventory/list` | List all items | Public/User |
| `GET` | `/api/v1/inventory/search/{pluCode}` | Find item by PLU | Public/User |
| `PUT` | `/api/v1/inventory/update/{pluCode}` | Update item details | User/Admin |
| `DELETE` | `/api/v1/inventory/delete/{pluCode}` | Remove item | Admin |
| `GET` | `/api/v1/inventory/pdf/report` | Download Full PDF Report | User/Admin |
| `GET` | `/api/v1/inventory/pdf/item/{pluCode}` | Download Item PDF | User/Admin |

---

## 🔒 Security & Roles

The system uses two primary roles:
- **USER**: Can view, create, and update inventory.
- **ADMIN**: Full system access, including user management and deletions.

---

## 🤝 Contributing

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 📝 License

Distributed under the MIT License. See `LICENSE` for more information.
