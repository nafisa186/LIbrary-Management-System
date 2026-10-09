# 📚 Library Management System

A Java Swing desktop application for managing library books, members, and book issue/return records using MySQL and JDBC.

## 🚀 Features
- Add, update, search, and delete books
- Register and view library members
- Issue and return books
- Track available book copies
- MySQL database integration using JDBC
- User-friendly graphical interface
- Background database operations

## 🛠️ Technologies Used
- Java 17
- Java Swing
- MySQL
- JDBC
- Maven
- Visual Studio Code

## 📋 Requirements
- JDK 17 or newer
- MySQL Server
- Apache Maven
- VS Code with Extension Pack for Java

## ⚙️ Installation and Setup

**1. Clone the repository**
```bash
git clone YOUR_GITHUB_REPOSITORY_URL
cd LibraryManagementSystem
```

**2. Create the database**

Open MySQL Workbench and execute the `database/schema.sql` file.

**3. Configure database credentials**

In PowerShell, set your MySQL credentials:

```powershell
$env:DB_USER="root"
$env:DB_PASSWORD="your_mysql_password"
```

**4. Compile the project**
```bash
mvn clean compile
```

**5. Run the application**
```bash
mvn exec:java
```

## 📂 Project Structure
```text
LibraryManagementSystem/
├── database/
│   └── schema.sql
├── src/main/java/com/library/
│   ├── model/
│   ├── dao/
│   ├── database/
│   ├── service/
│   ├── exception/
│   ├── ui/
│   └── LibraryApp.java
├── pom.xml
└── README.md
```

## 🗄️ Database Tables
- **books:** Stores book details and availability.
- **members:** Stores library member information.
- **book_issues:** Stores book issue and return records.

## 🔮 Future Improvements
- Login and role-based access
- Automatic fine calculation
- Member update and deletion
- Automated testing
- Enhanced dashboard design

## 👨‍💻 Author
Developed as a Java project for the GUVI–Galgotias Project Board.

## 📄 License
This project is intended for educational purposes.
