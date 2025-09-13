# AIClass Assistant API

A Spring Boot REST API designed for student performance analysis and AI-powered educational assistance.

## 🚀 Project Overview

AIClass Assistant is a backend API designed to manage educational data, student performance metrics, and provide intelligent recommendations based on AI. The application handles users (professors and students), subjects, classes, grades, and personalized recommendations.

## 🛠️ Technology Stack

- **Java 21** - Latest LTS version
- **Spring Boot 3.5.5** - Modern Spring framework
- **Spring Data JPA** - Data persistence layer
- **Spring Web** - REST API endpoints
- **PostgreSQL** - Primary database (Supabase)
- **Lombok** - Code generation for boilerplate reduction
- **Maven** - Build and dependency management
- **Spring Boot DevTools** - Development productivity

## 📊 Data Model

### Core Entities

#### 👤 User
- **Table:** `usuarios`
- **Fields:** id, nombre, email, rol, fecha_creacion
- **Roles:** profesor, estudiante
- **Relationships:** OneToMany with Clase (as professor), OneToMany with RecomendacionIA

#### 📚 Subject (Materia)
- **Table:** `materias`
- **Fields:** id, nombre, codigo, descripcion
- **Relationships:** OneToMany with Clase

#### 🏫 Class (Clase)
- **Table:** `clases`
- **Fields:** id, grupo, anio, semestre
- **Relationships:** ManyToOne with Materia, ManyToOne with Usuario (professor), OneToMany with EstudianteClase, OneToMany with Nota, OneToMany with RecomendacionIA

#### 👨‍🎓 StudentClass (EstudianteClase)
- **Table:** `estudiantes_clase`
- **Purpose:** Junction table between students and classes
- **Relationships:** ManyToOne with Clase, ManyToOne with Usuario (student)

#### 📝 Grade (Nota)
- **Table:** `notas`
- **Fields:** id, tipo, valor, fecha_registro
- **Relationships:** ManyToOne with Usuario (student), ManyToOne with Clase

#### 🤖 AI Recommendation (RecomendacionIA)
- **Table:** `recomendaciones_ia`
- **Fields:** id, mensaje, tipo, fecha_generacion
- **Relationships:** ManyToOne with Usuario, ManyToOne with Clase

## 🔗 API Endpoints

### Base URL
```
http://localhost:8080/api
```

### 👤 User Management (`/api/usuarios`)

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| `GET` | `/usuarios` | Get all users | - |
| `GET` | `/usuarios/{id}` | Get user by ID | `id` (Long) |
| `POST` | `/usuarios` | Create new user | Body: Usuario |
| `PUT` | `/usuarios/{id}` | Update user | `id` (Long), Body: Usuario |
| `DELETE` | `/usuarios/{id}` | Delete user | `id` (Long) |

**User Example:**
```json
{
  "id": 1,
  "nombre": "Ana Ruiz",
  "email": "ana@univ.edu",
  "rol": "profesor",
  "fechaCreacion": "2025-01-13T10:30:00"
}
```

### 🏫 Class Management (`/api/clases`)

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| `GET` | `/clases` | Get all classes | - |
| `GET` | `/clases/profesor/{profesorId}` | Get classes by professor | `profesorId` (Long) |
| `POST` | `/clases` | Create new class | Body: Clase |

**Class Example:**
```json
{
  "id": 1,
  "materia": {
    "id": 1,
    "nombre": "Matemáticas I",
    "codigo": "MAT101"
  },
  "profesor": {
    "id": 1,
    "nombre": "Ana Ruiz"
  },
  "grupo": "A",
  "anio": 2025,
  "semestre": 2
}
```

### 📚 Subject Management (`/api/materias`)

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| `GET` | `/materias` | Get all subjects | - |
| `POST` | `/materias` | Create new subject | Body: Materia |

**Subject Example:**
```json
{
  "id": 1,
  "nombre": "Matemáticas I",
  "codigo": "MAT101",
  "descripcion": "Introducción al álgebra"
}
```

### 📝 Grade Management (`/api/notas`)

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| `GET` | `/notas/clase/{claseId}` | Get grades by class | `claseId` (Long) |
| `GET` | `/notas/estudiante/{estudianteId}` | Get grades by student | `estudianteId` (Long) |
| `POST` | `/notas` | Create new grade | Body: Nota |
| `PUT` | `/notas/{id}` | Update grade | `id` (Long), Body: Nota |

**Grade Example:**
```json
{
  "id": 1,
  "estudiante": {
    "id": 2,
    "nombre": "Carlos Gómez"
  },
  "clase": {
    "id": 1,
    "grupo": "A"
  },
  "tipo": "Parcial 1",
  "valor": 2.8,
  "fechaRegistro": "2025-01-13T10:30:00"
}
```

### 🤖 AI Recommendations (`/api/recomendaciones`)

| Method | Endpoint | Description | Parameters |
|--------|----------|-------------|------------|
| `GET` | `/recomendaciones/usuario/{usuarioId}` | Get recommendations by user | `usuarioId` (Long) |
| `GET` | `/recomendaciones/clase/{claseId}` | Get recommendations by class | `claseId` (Long) |
| `GET` | `/recomendaciones/tipo/{tipo}` | Get recommendations by type | `tipo` (String) |

**Recommendation Example:**
```json
{
  "id": 1,
  "usuario": {
    "id": 2,
    "nombre": "Carlos Gómez"
  },
  "clase": {
    "id": 1,
    "grupo": "A"
  },
  "mensaje": "Tu rendimiento en Álgebra está por debajo del promedio. Revisa los temas de factorización y ecuaciones.",
  "tipo": "estudiante",
  "fechaGeneracion": "2025-01-13T10:30:00"
}
```

### 🔧 Utility Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/test/db-connection` | Test database connection |

## 📚 API Documentation

### Swagger UI
La documentación interactiva de la API está disponible en:
```
http://localhost:8080/swagger-ui/index.html
```

### OpenAPI JSON
El esquema OpenAPI está disponible en:
```
http://localhost:8080/v3/api-docs
```

### Características de la Documentación
- ✅ **Interfaz Interactiva**: Prueba los endpoints directamente desde el navegador
- ✅ **Documentación Completa**: Descripción detallada de cada endpoint
- ✅ **Ejemplos de Respuesta**: Códigos de estado y mensajes de error
- ✅ **Parámetros Documentados**: Descripción de todos los parámetros de entrada
- ✅ **Modelos de Datos**: Esquemas de las entidades del sistema

## ⚙️ Installation & Setup

### 1. Prerequisites
- **Java 21** installed (OpenJDK recommended)
- **Maven 3.6+** installed
- **PostgreSQL** (Supabase configured)
- **IDE** (IntelliJ IDEA, Eclipse, or VS Code)

### 2. Clone Repository
```bash
git clone ethx42/student-performance-api
cd student-performance-api
```

### 3. Environment Variables Setup

This project uses environment variables for configuration following professional Spring Boot practices.

**Using Environment Variables**

1. **Set up environment variables in your shell:**
   ```bash
   # Copy the template and edit with your credentials
   cp setup-env.sh.template setup-env.sh
   nano setup-env.sh  # Edit with your actual Supabase credentials
   
   # Run the setup script
   source setup-env.sh
   ```

2. **Start the application:**
   ```bash
   source ~/.sdkman/bin/sdkman-init.sh
   ./mvnw spring-boot:run
   ```

**Note:** The `setup-env.sh` file is ignored by Git for security reasons. Never commit sensitive data to version control.

**Required Environment Variables:**
```bash
# Database Configuration
DB_URL=jdbc:postgresql://your-pooler-host:5432/postgres
DB_USERNAME=postgres.your-project-ref
DB_PASSWORD=your-database-password
DB_DRIVER=org.postgresql.Driver

# Hibernate Configuration
HIBERNATE_DDL_AUTO=update
HIBERNATE_SHOW_SQL=true
HIBERNATE_FORMAT_SQL=true
HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect

# Server Configuration
SERVER_PORT=8080

# Application Configuration
APP_NAME=aiclass
```

**Note:** Replace the placeholder values with your actual Supabase credentials.

### 4. Running the Application

The application automatically loads environment variables from the `.env` file:

```bash
# Start the application
source ~/.sdkman/bin/sdkman-init.sh
./mvnw spring-boot:run
```

**Note:** The application will automatically:
- ✅ Load variables from `.env` file
- ✅ Use default values if `.env` is not found
- ✅ Display loading status in console logs

The application will start on `http://localhost:8080`

## 🗄️ Database

### Current Configuration
- **Provider:** Supabase PostgreSQL
- **Pooler:** Session Pooler (IPv4 compatible)
- **DDL:** `update` (preserves data between restarts)
- **Dialect:** PostgreSQL

### Data Initialization
The application includes a `DatabaseSeeder` that:
- ✅ Checks if data already exists before inserting
- ✅ Creates sample users (professor and students)
- ✅ Creates subjects and classes
- ✅ Assigns students to classes
- ✅ Registers sample grades
- ✅ Generates sample AI recommendations

## 🧪 Testing

### Run Tests
```bash
./mvnw test
```

### Database Connection Test
```bash
./mvnw test -Dtest=DatabaseConnectionTest
```

### Test Connection Endpoint
```bash
curl http://localhost:8080/api/test/db-connection
```

## 📦 Building for Production

### Create JAR
```bash
./mvnw clean package
```

### Run JAR
```bash
java -jar target/aiclass-0.0.1-SNAPSHOT.jar
```

## 🏗️ Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/viveek/aiclass/
│   │       ├── AiclassApplication.java          # Main application class
│   │       ├── controller/                      # REST Controllers
│   │       │   ├── ClaseController.java
│   │       │   ├── MateriaController.java
│   │       │   ├── NotaController.java
│   │       │   ├── RecomendacionController.java
│   │       │   ├── UsuarioController.java
│   │       │   └── DatabaseTestController.java
│   │       ├── model/                           # JPA Entities
│   │       │   ├── Clase.java
│   │       │   ├── EstudianteClase.java
│   │       │   ├── Materia.java
│   │       │   ├── Nota.java
│   │       │   ├── RecomendacionIA.java
│   │       │   └── Usuario.java
│   │       ├── repository/                      # JPA Repositories
│   │       │   ├── ClaseRepository.java
│   │       │   ├── EstudianteClaseRepository.java
│   │       │   ├── MateriaRepository.java
│   │       │   ├── NotaRepository.java
│   │       │   ├── RecomendacionIARepository.java
│   │       │   └── UsuarioRepository.java
│   │       └── config/                         # Configuration
│   │           └── DatabaseSeeder.java
│   └── resources/
│       ├── application.yml                     # Main configuration
│       └── application.yml.template           # Configuration template
└── test/
    └── java/
        └── com/viveek/aiclass/
            ├── AiclassApplicationTests.java
            └── DatabaseConnectionTest.java
```

## 🔧 Advanced Configuration

### Environment Variables
The application supports the following environment variables:

| Variable | Description | Default Value |
|----------|-------------|---------------|
| `DB_URL` | Database connection URL | *(required)* |
| `DB_USERNAME` | Database username | *(required)* |
| `DB_PASSWORD` | Database password | *(required)* |
| `DB_DRIVER` | Database driver | `org.postgresql.Driver` |
| `HIBERNATE_DDL_AUTO` | Hibernate DDL mode | `update` |
| `HIBERNATE_SHOW_SQL` | Show SQL queries | `true` |
| `SERVER_PORT` | Application port | `8080` |

### Production Environment Variables
```bash
export DB_URL=jdbc:postgresql://your-production-host:5432/your-database
export DB_USERNAME=your-username
export DB_PASSWORD=your-secure-password
export HIBERNATE_DDL_AUTO=validate
export HIBERNATE_SHOW_SQL=false
export SERVER_PORT=80
```

### DDL Configuration
- **Development:** `update` (preserves data, updates schema as needed)
- **Production:** `validate` or `none` (doesn't modify schema)

## 🚀 Implemented Features

### ✅ Completed
- [x] Complete data model with JPA
- [x] REST controllers for all entities
- [x] Supabase PostgreSQL connection
- [x] Sample data seeder
- [x] Database connection test
- [x] Development configuration with DevTools

### 🔄 In Development
- [ ] Authentication and authorization
- [ ] Data validation
- [ ] Custom error handling
- [ ] API documentation with Swagger
- [ ] Complete unit tests

### 📋 Roadmap
- [ ] AI services integration
- [ ] Student performance analytics
- [ ] Metrics dashboard
- [ ] Real-time notifications
- [ ] Advanced reporting API
- [ ] Redis caching
- [ ] Monitoring and logging
- [ ] Docker deployment

## ⚠️ Security Notes

- 🔒 Update database credentials before production deployment
- 🔐 Implement authentication/authorization
- 🌐 Use HTTPS in production
- 🔄 Update dependencies regularly
- 📝 Use environment variables for sensitive configuration

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 👥 Authors

- **Santiago Torres** - Initial development

## 🆘 Support

If you encounter any issues or have questions:

1. Check existing issues in the repository
2. Create a new issue with detailed information
3. Contact the development team

---

**Happy Coding! :)🎓💻** 