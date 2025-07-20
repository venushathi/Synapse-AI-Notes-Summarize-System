# Synapse AI Notes 🧠✨

<p align="center">
  <strong>An intelligent note-taking application designed to transform your scattered notes into a structured and searchable knowledge base.</strong>
</p>


---

Synapse AI Notes is a modern web application built with Java Spring Boot and a dynamic Vanilla JavaScript frontend. It moves beyond simple text storage by integrating AI-powered features to help users summarize long notes and automatically categorize them, making the process of studying and research more efficient and insightful.

---

## ✨ Key Features

* **✍️ Full CRUD Functionality:** Create, read, update, and delete your notes and subjects with ease.
* **🔐 Secure User Authentication:** A robust security system for user registration and login using email and password.
* **🤖 AI-Powered Summarization:** Condense long, complex notes into concise, easy-to-digest summaries with a single click.
* **🏷️ AI-Assisted Categorization:** Get intelligent suggestions for categorizing your notes based on their content, saving you time and effort.
* **📱 Dynamic & Responsive Frontend:** A clean, responsive, and user-friendly interface built with HTML, TailwindCSS, and JavaScript.
* **🔍 Search & Filter:** Instantly find the information you need by searching for keywords or filtering your notes by subject.

---

## 🛠️ Technology Stack

| Component      | Technology Used                                       |
| :------------- | :---------------------------------------------------- |
| **Backend** | Java 17, Spring Boot 3.x                              |
| **Database** | MySQL                                                 |
| **Security** | Spring Security (Email/Password Authentication)       |
| **Build Tool** | Apache Maven                                          |
| **Frontend** | HTML, CSS, TailwindCSS, Vanilla JavaScript            |
| **AI Services**| Hugging Face Inference API                            |
| **Key Deps** | `spring-data-jpa`, `spring-security`, `mysql-connector` |

---

## 🚀 Getting Started

To get a local copy up and running, follow these simple steps.

### Prerequisites

* JDK 17 or later
* Apache Maven 3.8 or later
* MySQL Server (e.g., via XAMPP, WAMP, or standalone installation)

### Installation & Setup

1.  **Clone the repository:**
    ```sh
    git clone [https://github.com/your-username/synapse-ai-notes-app.git](https://github.com/your-username/synapse-ai-notes-app.git)
    cd synapse-ai-notes-app
    ```
    *(Remember to replace `your-username` with your actual GitHub username)*

2.  **Database Setup:**
    * Ensure your MySQL server is running.
    * Open a MySQL client (like phpMyAdmin, MySQL Workbench, or the command line).
    * Create a new, empty database. You can name it `synapse_ai_db`.
        ```sql
        CREATE DATABASE synapse_ai_db;
        ```

3.  **Configure `application.properties`:**
    
    **⚠️ Important:** This file is intentionally excluded from the repository to protect your secret keys. You must create it manually.

    * In the project, navigate to `src/main/resources/`.
    * Create a new file named `application.properties`.
    * Copy the content below into your new file and update the values for your local environment.

    ```properties
    # Server Port
    server.port=8080

    # Database Connection
    # Replace 'your_mysql_password' with your MySQL root password (or leave blank if none).
    spring.datasource.url=jdbc:mysql://localhost:3306/synapse_ai_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    spring.datasource.username=root
    spring.datasource.password=your_mysql_password

    # JPA and Hibernate Properties
    # 'update' will automatically create and update tables based on your @Entity classes.
    spring.jpa.hibernate.ddl-auto=update
    spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
    spring.jpa.show-sql=true

    # Hugging Face API Key
    # Replace with your own key from huggingface.co
    huggingface.api.key=YOUR_HUGGING_FACE_API_KEY_HERE
    ```

4.  **Build and Run the Application:**
    * Open a terminal in the project's root directory.
    * Run the application using the Maven wrapper:
        ```sh
        ./mvnw spring-boot:run
        ```
    * The application will start, and the necessary database tables will be created automatically. You can access it at **`http://localhost:8080`**.

---

## 📄 License

This project is licensed under the MIT License.
