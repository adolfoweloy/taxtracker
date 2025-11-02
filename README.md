# Tax Tracker

## Purpose

This project is a personal tool designed to convert banking reports from Bradesco (BRL) to AUD and calculate interest-based earnings for Australian Tax Office (ATO) reporting purposes.

## Context

This is a **very specific, personal-use project** that solves a niche problem. It's unlikely to benefit others directly, but the source code is available for anyone interested in exploring a Spring Boot application that is useful for someone (myself) built with:

- **Kotlin** as the primary programming language
- **PostgreSQL** as the database
- **Spring Boot** framework
- External currency conversion services for exchange rates

Feel free to explore the codebase as a reference for Spring Boot and Kotlin development patterns.

## Requirements

- **Java 17+** (or the JDK version specified in `build.gradle.kts`)
- **Docker** and **Docker Compose** (for running PostgreSQL)
- **Gradle** (wrapper included in the project)

## How to Run the Project

### 1. Start the Database

The project uses PostgreSQL via Docker Compose:

```bash
docker-compose up -d
```

### 2. Run the Application

Using the Gradle wrapper:

```bash
./gradlew bootRun
```

### 3. Run Tests

Execute unit tests:

```bash
./gradlew test
```

Execute integration tests:

```bash
./gradlew integrationTest
```

### 4. Access the Application

Once running, the application should be accessible at `http://localhost:8080` (or the port configured in `application.yaml`).

## Project Structure

- `/src/main/kotlin` - Main application source code
- `/src/test/kotlin` - Unit tests
- `/src/integrationTest/kotlin` - Integration tests
- `/src/main/resources` - Configuration files and templates
- `/sql` - SQL queries and scripts

## How to Contribute

**Please note:** This is a personal project tailored to my specific needs, and I'm **not actively seeking contributions**.

However, if you're interested in this project:

- **Feel free to fork** and adapt it for your own purposes
- **If you find a bug or issue**, please [report it](../../issues) so I can fix it
- For substantial changes or new features, consider maintaining your own fork independently

## License

**DISCLAIMER:** This software is provided for personal use and reference purposes only. By using this software, you acknowledge and agree that you do so at your own risk. 
The author is not responsible for any issues, losses, damages, or security breaches that may occur as a result of using this software or any bugs that may exist in this repository.

MIT License

Copyright (c) 2025 Adolfo Eloy

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

*This project is provided as-is for personal use and reference purposes.*
