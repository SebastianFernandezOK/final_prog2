# 🎫 Sistema de Gestión de Eventos - Backend

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue.svg)](https://www.mysql.com/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Sistema backend REST API para la gestión de eventos, bloqueo y venta de asientos, desarrollado con Spring Boot.

---

## 📋 Tabla de Contenidos

- [Características](#características)
- [Requisitos Previos](#requisitos-previos)
- [Instalación](#instalación)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Endpoints API](#endpoints-api)
- [Documentación](#documentación)
- [Tecnologías](#tecnologías)
- [Autor](#autor)

---

## ✨ Características

- 🎭 **Gestión de Eventos**: Consulta de eventos con disponibilidad de asientos
- 🔒 **Bloqueo de Asientos**: Reserva temporal durante proceso de compra
- 💳 **Venta de Asientos**: Procesamiento de ventas con asignación de nombres
- 🔐 **Autenticación JWT**: Seguridad con tokens JSON Web
- 👥 **Gestión de Usuarios**: CRUD completo de usuarios
- 🔄 **Sincronización Kafka**: Actualización automática desde servicio externo
- 🗄️ **Persistencia MySQL**: Almacenamiento local con JPA/Hibernate
- 🌐 **Integración Externa**: Comunicación con API externa mediante WebClient

---

## 📦 Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:

- **Java 21** o superior ([Descargar](https://www.oracle.com/java/technologies/downloads/))
- **Maven 3.6+** ([Descargar](https://maven.apache.org/download.cgi))
- **MySQL 8.0+** ([Descargar](https://dev.mysql.com/downloads/))
- **Git** ([Descargar](https://git-scm.com/downloads))

---

## 🚀 Instalación

### 1. Clonar el repositorio

```bash
git clone <url-del-repositorio>
cd events-backend
```

### 2. Crear base de datos

```bash
# Conectar a MySQL
mysql -u root -p

# Crear base de datos
CREATE DATABASE eventos_backend;
EXIT;
```

### 3. Configurar aplicación

Editar `src/main/resources/application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/eventos_backend
    username: root
    password: tu_password  # Cambiar por tu password
```

### 4. Compilar el proyecto

```bash
mvn clean install
```

---

## ⚙️ Configuración

### Variables de Entorno (Opcional)

```bash
export EXTERNAL_AUTH_USER=sc.fernandez
export EXTERNAL_AUTH_PASS=1234
```

### Configuración Principal

Archivo: `src/main/resources/application.yaml`

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/eventos_backend
    username: root
    password: root

externalAuth:
  base-url: http://192.168.194.250:8080
  login-path: /api/authenticate
  username: ${EXTERNAL_AUTH_USER:sc.fernandez}
  password: ${EXTERNAL_AUTH_PASS:1234}

application:
  security:
    jwt:
      secret-key: <tu-secret-key>
      expiration: 86400000  # 24 horas
```

---

## 🏃 Ejecución

### Opción 1: Maven

```bash
mvn spring-boot:run
```

### Opción 2: JAR

```bash
java -jar target/events-backend-0.0.1-SNAPSHOT.jar
```

### Opción 3: IDE

Ejecutar la clase `EventsBackendApplication.java`

**La aplicación estará disponible en:** `http://localhost:8081`

---

## 🔌 Endpoints API

### 📅 Eventos

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/db/events/summary` | Lista eventos resumidos | ❌ |
| GET | `/api/db/events/summary/{id}` | Evento resumido por ID | ❌ |
| GET | `/api/db/events` | Lista eventos completos | ❌ |
| GET | `/api/db/events/{id}` | Evento completo por ID | ❌ |

### 🎫 Asientos

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/db/block-seats` | Bloquear asientos | ❌ |
| POST | `/api/db/sale-seats` | Vender asientos | ❌ |

### 👤 Usuarios

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/users` | Crear usuario | ✅ |
| GET | `/api/users` | Listar usuarios | ✅ |
| GET | `/api/users/{id}` | Obtener usuario | ✅ |
| PUT | `/api/users/{id}` | Actualizar usuario | ✅ |
| DELETE | `/api/users/{id}` | Eliminar usuario | ✅ |

### 🔐 Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Iniciar sesión | ❌ |

---

## 📖 Ejemplos de Uso

### Consultar Eventos

```bash
curl -X GET http://localhost:8081/api/db/events/summary
```

**Response:**
```json
[
  {
    "id": 1,
    "nombre": "Concierto Rock",
    "fecha": "2024-12-20T21:00:00",
    "lugar": "Estadio Central",
    "asientosDisponibles": 150,
    "precioBase": 5000.0
  }
]
```

### Bloquear Asientos

```bash
curl -X POST http://localhost:8081/api/db/block-seats \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "asientos": [
      {"fila": 5, "columna": 10},
      {"fila": 5, "columna": 11}
    ]
  }'
```

### Vender Asientos

```bash
curl -X POST http://localhost:8081/api/db/sale-seats \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "fecha": "2024-12-16T20:00:00-03:00",
    "precioVenta": 10000.00,
    "asientos": [
      {
        "fila": 5,
        "columna": 10,
        "persona": "Juan Pérez"
      },
      {
        "fila": 5,
        "columna": 11,
        "persona": "María García"
      }
    ]
  }'
```

**Response:**
```json
{
  "eventoId": 1,
  "ventaId": 456,
  "fechaVenta": "2024-12-16T20:15:30-03:00",
  "precioVenta": 10000.00,
  "resultado": true,
  "descripcion": "Venta realizada exitosamente",
  "asientos": [
    {
      "fila": 5,
      "columna": 10,
      "persona": "Juan Pérez",
      "estado": "VENDIDO"
    }
  ]
}
```

---

## 📚 Documentación

- **[DOCUMENTACION.md](DOCUMENTACION.md)** - Documentación técnica completa
- **[GUIA_PRESENTACION.md](GUIA_PRESENTACION.md)** - Guía para presentación
- **[EJEMPLOS_API.md](EJEMPLOS_API.md)** - Colección de ejemplos de API

---

## 🛠️ Tecnologías

### Backend
- **Spring Boot 3.5.7** - Framework principal
- **Spring Data JPA** - Persistencia de datos
- **Spring Security** - Seguridad y autenticación
- **Spring WebFlux** - Cliente HTTP reactivo

### Base de Datos
- **MySQL 8.0** - Base de datos relacional
- **Hibernate** - ORM

### Seguridad
- **JWT (jjwt 0.11.5)** - Tokens de autenticación

### Utilidades
- **Lombok** - Reducción de código boilerplate
- **Jakarta Validation** - Validación de datos
- **Maven** - Gestión de dependencias

---

## 📁 Estructura del Proyecto

```
events-backend/
├── src/main/java/ar/edu/um/events_backend/
│   ├── auth/              # Autenticación y JWT
│   ├── config/            # Configuraciones
│   ├── controller/        # Controladores REST
│   ├── dto/               # Data Transfer Objects
│   ├── entity/            # Entidades JPA
│   ├── exception/         # Manejo de excepciones
│   ├── mapper/            # Mapeo Entity-DTO
│   ├── repository/        # Repositorios JPA
│   └── service/           # Lógica de negocio
├── src/main/resources/
│   └── application.yaml   # Configuración
├── DOCUMENTACION.md       # Documentación técnica
├── GUIA_PRESENTACION.md   # Guía de presentación
├── EJEMPLOS_API.md        # Ejemplos de API
├── pom.xml                # Dependencias Maven
└── README.md              # Este archivo
```

---

## 🧪 Testing

### Verificar que la aplicación está corriendo

```bash
curl http://localhost:8081/api/db/events/summary
```

### Herramientas Recomendadas

- **Postman** - Testing de API
- **cURL** - Línea de comandos
- **MySQL Workbench** - Gestión de base de datos

---

## 🐛 Solución de Problemas

### Error: Puerto 8081 en uso

```bash
# Linux/Mac
lsof -i :8081
kill -9 <PID>

# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F
```

### Error: Conexión a MySQL

- Verificar que MySQL está corriendo
- Verificar credenciales en `application.yaml`
- Verificar que la base de datos existe

### Error 403 Forbidden

- Verificar que la ruta está en rutas públicas
- Si requiere autenticación, incluir header: `Authorization: Bearer <token>`

---

## 🤝 Contribución

Este es un proyecto académico. Para sugerencias o mejoras:

1. Fork el proyecto
2. Crear una rama (`git checkout -b feature/mejora`)
3. Commit cambios (`git commit -m 'Agregar mejora'`)
4. Push a la rama (`git push origin feature/mejora`)
5. Abrir Pull Request

---

## 📄 Licencia

Este proyecto es parte de un trabajo final académico para la Universidad de Mendoza.

---

## 👨‍💻 Autor

**Santiago Fernández**
- Email: sc.fernandez@um.edu.ar
- Universidad: Universidad de Mendoza
- Materia: Programación 2
- Año: 2024

---

## 🙏 Agradecimientos

- Profesor de Programación 2
- Universidad de Mendoza
- Comunidad Spring Boot
- Stack Overflow

---

## 📞 Soporte

Para preguntas o problemas:
- Abrir un issue en GitHub
- Contactar por email: sc.fernandez@um.edu.ar

---

**⭐ Si este proyecto te fue útil, no olvides darle una estrella!**

---

*Última actualización: Diciembre 2024*
