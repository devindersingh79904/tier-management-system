# Loyalty Tier System

The **Loyalty Tier System** is a Spring Boot application designed to manage loyalty tiers, benefits, and user levels.

## Setup Instructions

### Prerequisites
- **Java Development Kit (JDK) 21** or higher
- **Maven** (bundled using `./mvnw`)

### Installation
1. Clone the repository:
   ```bash
   git clone git@github.com:devindersingh79904/tier-management-system.git
   cd loyalty-tier-system
   ```

2. Verify Java installation:
   ```bash
   java -version
   ```

## Execution Instructions

### Running the Application Locally
To start the application, use the Maven wrapper:
```bash
./mvnw spring-boot:run
```

### Running Tests
To run automated tests:
```bash
./mvnw test
```

### Building the Project
To package the project into a runnable JAR file:
```bash
./mvnw clean package
```

## Environment Variables Example
Below are example environment variables that can be set to configure the application when running in a production or containerized environment:

```bash
# Server Configuration
export SERVER_PORT=8080

# Spring Profiles Active
export SPRING_PROFILES_ACTIVE=dev

# Optional: JDBC Connection configuration (if database integration is enabled)
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5402/loyalty_db
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=mysecretpassword
```
