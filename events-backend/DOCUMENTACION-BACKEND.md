# Sistema de Gestión de Eventos - Documentación Técnica

**Autor:** Sebastián Fernández  
**Curso:** Programación 2  
**Universidad de Mendoza**  
**Fecha:** Diciembre 2025

---

## Índice

1. [Descripción General](#descripción-general)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Tecnologías Utilizadas](#tecnologías-utilizadas)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Funcionalidades Implementadas](#funcionalidades-implementadas)
6. [API REST - Endpoints](#api-rest---endpoints)
7. [Seguridad y Autenticación](#seguridad-y-autenticación)
8. [Base de Datos](#base-de-datos)
9. [Integración con Servicios Externos](#integración-con-servicios-externos)
10. [Guía de Instalación y Ejecución](#guía-de-instalación-y-ejecución)
11. [Ejemplos de Uso](#ejemplos-de-uso)

---

## Descripción General

Este proyecto es un **sistema backend para la gestión de eventos y venta de asientos**, desarrollado con **Spring Boot 3.5.7** y **Java 21**. El sistema permite:

- Gestionar eventos y consultar información resumida
- Bloquear y vender asientos de eventos
- Sincronización automática con servicios externos mediante Kafka
- Autenticación y autorización con JWT
- Persistencia en base de datos MySQL

El sistema actúa como intermediario entre clientes (frontend) y un servicio externo de eventos, proporcionando una capa de abstracción, caché y seguridad.

---

## Arquitectura del Sistema

### Diagrama de Arquitectura

```
┌─────────────┐
│   Cliente   │
│  (Frontend) │
└──────┬──────┘
       │ HTTP/REST
       ▼
┌─────────────────────────────────────┐
│     Events Backend (Puerto 8081)    │
│  ┌───────────────────────────────┐  │
│  │      Controllers (REST)       │  │
│  └───────────┬───────────────────┘  │
│              ▼                      │
│  ┌───────────────────────────────┐  │
│  │         Services              │  │
│  │  - EventService               │  │
│  │  - SaleService                │  │
│  │  - BlockService               │  │
│  └───────────┬───────────────────┘  │
│              ▼                      │ 
│  ┌───────────────────────────────┐  │
│  │    Repositories (JPA)         │  │
│  └───────────┬───────────────────┘  │
│              ▼                      │
│  ┌───────────────────────────────┐  │
│  │      MySQL Database           │  │
│  └───────────────────────────────┘  │
└─────────────┬───────────────────────┘
              │ WebClient
              ▼
┌─────────────────────────────────────┐
│   Servicio Externo (Puerto 8080)    │
│   - API de Eventos                  │
│   - Bloqueo de Asientos             │
│   - Venta de Asientos               │
└─────────────────────────────────────┘
              ▲
              │ Kafka
              │
┌─────────────────────────────────────┐
│      Proxy Service (Puerto 8082)    │
│   - Escucha eventos Kafka           │
│   - Notifica cambios al Backend     │
└─────────────────────────────────────┘
```

### Patrón de Diseño

El proyecto sigue el patrón **MVC (Model-View-Controller)** adaptado para APIs REST:

- **Controllers**: Manejan las peticiones HTTP
- **Services**: Contienen la lógica de negocio
- **Repositories**: Acceso a datos (JPA)
- **Entities**: Modelos de base de datos
- **DTOs**: Objetos de transferencia de datos

---

## Tecnologías Utilizadas

### Backend Framework
- **Spring Boot 3.5.7**
- **Java 21**
- **Maven** (gestión de dependencias)

### Persistencia
- **Spring Data JPA** (ORM)
- **MySQL Connector** (driver de base de datos)
- **Hibernate** (implementación JPA)

### Seguridad
- **Spring Security 6.5.6**
- **JWT (JSON Web Tokens)** - io.jsonwebtoken:jjwt 0.11.5
- Autenticación stateless

### Comunicación HTTP
- **Spring WebFlux** (cliente HTTP reactivo)
- **WebClient** (llamadas a servicios externos)

### Utilidades
- **Lombok** (reducción de código boilerplate)
- **Jakarta Validation** (validación de datos)
- **Spring DevTools** (desarrollo)

---

## Estructura del Proyecto

```
events-backend/
├── src/main/java/ar/edu/um/events_backend/
│   ├── auth/                          # Autenticación y JWT
│   │   ├── ExternalAuthService.java   # Autenticación con servicio externo
│   │   ├── JwtAuthFilter.java         # Filtro de validación JWT
│   │   ├── JwtService.java            # Generación y validación de tokens
│   │   └── JwtUtils.java              # Utilidades JWT
│   │
│   ├── config/                        # Configuraciones
│   │   ├── SecurityConfig.java        # Configuración de seguridad
│   │   ├── WebClientConfig.java       # Configuración de clientes HTTP
│   │   └── EventsInitialLoader.java   # Carga inicial de datos
│   │
│   ├── controller/                    # Controladores REST
│   │   ├── EventSummary_db_Controller.java
│   │   ├── Events_db_Controller.java
│   │   ├── Block_seat_db_Controller.java
│   │   ├── Sale_seat_db_Controller.java
│   │   └── InternalController.java    # Endpoints internos (Kafka)
│   │
│   ├── dto/                           # Data Transfer Objects
│   │   ├── Block_seat_db_Request.java
│   │   ├── Block_seat_db_Response.java
│   │   ├── Sale_seat_db_Request.java
│   │   ├── Sale_seat_db_Response.java
│   │   ├── Event_db_DTO.java
│   │   ├── Event_summary_db_DTO.java
│   │   
│   │
│   ├── entity/                        # Entidades JPA
│   │   ├── Event_db.java
│   │   ├── EventSummary_db.java
│   │   
│   │
│   ├── exception/                     # Manejo de excepciones
│   │   ├── ApiError.java
│   │   ├── ApiExceptionHandler.java
│   │   └── ResourceNotFoundException.java
│   │
│   ├── mapper/                        # Mapeo Entity <-> DTO
│   │   
│   │
│   ├── repository/                    # Repositorios JPA
│   │   ├── Event_db_Repository.java
│   │   ├── EventSummary_db_Repository.java
│   │   
│   │
│   ├── service/                       # Lógica de negocio
│   │   ├── Event_db_Service.java
│   │   ├── EventSummary_db_Service.java
│   │   ├── Block_seat_db_Service.java
│   │   ├── Sale_seat_db_Service.java
│   │   
│   │
│   └── EventsBackendApplication.java  # Clase principal
│
├── src/main/resources/
│   └── application.yaml               # Configuración de la aplicación
│
└── pom.xml                            # Dependencias Maven
```

---

## Funcionalidades Implementadas

### 1. Gestión de Eventos

#### Consulta de Eventos Resumidos
- **Endpoint**: `GET /api/db/events/summary`
- **Descripción**: Obtiene lista resumida de eventos
- **Sincronización**: Automática con servicio externo
- **Caché**: Base de datos local

#### Consulta de Eventos Detallados
- **Endpoint**: `GET /api/db/events`
- **Descripción**: Obtiene información completa de eventos
- **Incluye**: Matriz de asientos con disponibilidad

### 2. Bloqueo de Asientos

- **Endpoint**: `POST /api/db/block-seats`
- **Descripción**: Bloquea temporalmente asientos para un evento
- **Uso**: Reserva temporal durante proceso de compra
- **Timeout**: Configurado en servicio externo

**Request:**
```json
{
  "eventoId": 123,
  "asientos": [
    {"fila": 5, "columna": 10},
    {"fila": 5, "columna": 11}
  ]
}
```

**Response:**
```json
{
  "resultado": true,
  "descripcion": "Asientos bloqueados exitosamente",
  "eventoId": 123,
  "asientos": [
    {"estado": "BLOQUEADO", "fila": 5, "columna": 10},
    {"estado": "BLOQUEADO", "fila": 5, "columna": 11}
  ]
}
```

### 3. Venta de Asientos

- **Endpoint**: `POST /api/db/sale-seats`
- **Descripción**: Realiza la venta definitiva de asientos
- **Validación**: Verifica disponibilidad en tiempo real
- **Transaccional**: Garantiza consistencia de datos

**Request:**
```json
{
  "eventoId": 123,
  "fecha": "2024-12-16T20:00:00-03:00",
  "precioVenta": 5000.00,
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
}
```

**Response:**
```json
{
  "eventoId": 123,
  "ventaId": 456,
  "fechaVenta": "2024-12-16T20:15:30-03:00",
  "precioVenta": 5000.00,
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



### 4. Autenticación JWT

- **Login**: `POST /api/auth/login`
- **Tokens**: Generación y validación automática
- **Expiración**: 24 horas (configurable)
- **Stateless**: Sin sesiones en servidor

### 5. Sincronización con Kafka

- **Endpoint interno**: `POST /internal/events/sync`
- **Trigger**: Notificaciones desde proxy Kafka
- **Acción**: Sincroniza todos los eventos desde servicio externo
- **Automático**: No requiere intervención manual

---

## API REST - Endpoints

### Eventos

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| GET | `/api/db/events/summary` | Lista eventos resumidos | No |
| GET | `/api/db/events/summary/{id}` | Evento resumido por ID | No |
| GET | `/api/db/events` | Lista eventos completos | No |
| GET | `/api/db/events/{id}` | Evento completo por ID | No |

### Asientos

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/db/block-seats` | Bloquear asientos | No |
| POST | `/api/db/sale-seats` | Vender asientos | No |



### Autenticación

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/auth/login` | Iniciar sesión | No |

### Internos (Kafka)

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/internal/events/sync` | Sincronizar eventos | No |

---

## Seguridad y Autenticación

### Configuración de Seguridad

El sistema implementa **Spring Security** con las siguientes características:

#### Rutas Públicas (sin autenticación)
- `/api/auth/login` - Login
- `/internal/**` - Endpoints internos
- `/api/db/events/**` - Consulta de eventos
- `/api/db/block-seats/**` - Bloqueo de asientos
- `/api/db/sale-seats/**` - Venta de asientos

---

## Base de Datos

### Configuración MySQL

```yaml
spring:
  datasource:
    url: jdbc:mysql://127.0.0.1:3306/eventos_backend
    username: root
    password: root
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    database-platform: org.hibernate.dialect.MySQLDialect
```

### Entidades

#### Event_db
- Almacena información completa de eventos
- Sincronizada con servicio externo

#### EventSummary_db
- Versión resumida de eventos
- Optimizada para listados
- Caché de consultas frecuentes

---

## Integración con Servicios Externos

### Servicio Externo de Eventos

**URL Base**: `http://192.168.194.250:8080`

#### Autenticación Automática
El sistema implementa `ExternalAuthService` que:
- Obtiene token JWT automáticamente
- Refresca token antes de expiración
- Reintenta en caso de 401 Unauthorized
- Maneja credenciales desde configuración

**Configuración:**
```yaml
externalAuth:
  base-url: http://192.168.194.250:8080
  login-path: /api/authenticate
  username: sc.fernandez
  password: 1234
  buffer-seconds: 30
```

#### Endpoints Externos Utilizados

| Endpoint Externo | Uso |
|------------------|-----|
| `/api/authenticate` | Autenticación |
| `/api/endpoints/v1/eventos` | Obtener eventos |
| `/api/endpoints/v1/eventos-resumidos` | Obtener resumen |
| `/api/endpoints/v1/bloquear-asientos` | Bloquear asientos |
| `/api/endpoints/v1/realizar-venta` | Vender asientos |

### WebClient Configurado

El sistema usa **Spring WebFlux WebClient** con:
- **Filtro de autenticación**: Inyecta token automáticamente
- **Retry en 401**: Refresca token y reintenta
- **Timeout**: Configurado para operaciones largas
- **Base URL**: Configurada centralmente

---

## Guía de Instalación y Ejecución

### Requisitos Previos

- **Java 21** o superior
- **Maven 3.6+**
- **MySQL 8.0+**
- **Git**

### Paso 1: Clonar el Repositorio

```bash
git clone <url-repositorio>
cd events-backend
```

### Paso 2: Configurar Base de Datos

```bash
# Conectar a MySQL
mysql -u root -p

# Crear base de datos
CREATE DATABASE eventos_backend;
```

### Paso 3: Configurar Variables de Entorno (Opcional)

```bash
export EXTERNAL_AUTH_USER=sc.fernandez
export EXTERNAL_AUTH_PASS=1234
```

O editar `src/main/resources/application.yaml`

### Paso 4: Compilar el Proyecto

```bash
mvn clean install
```

### Paso 5: Ejecutar la Aplicación

```bash
mvn spring-boot:run
```

O ejecutar el JAR:

```bash
java -jar target/events-backend-0.0.1-SNAPSHOT.jar
```

### Paso 6: Verificar

```bash
curl http://localhost:8081/api/db/events/summary
```

La aplicación estará disponible en: **http://localhost:8081**

---

## Ejemplos de Uso

### 1. Consultar Eventos Disponibles

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

### 2. Obtener Detalle de Evento con Asientos

```bash
curl -X GET http://localhost:8081/api/db/events/1
```

**Response:**
```json
{
  "id": 1,
  "nombre": "Concierto Rock",
  "fecha": "2024-12-20T21:00:00",
  "lugar": "Estadio Central",
  "asientos": [
    [
      {"fila": 1, "columna": 1, "estado": "DISPONIBLE"},
      {"fila": 1, "columna": 2, "estado": "VENDIDO"}
    ]
  ]
}
```

### 3. Bloquear Asientos

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

### 4. Realizar Venta de Asientos

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

### 5. Login de Usuario

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "usuario@example.com",
    "password": "password123"
  }'
```


### 4. Sincronización Automática

El sistema escucha notificaciones de Kafka a través del proxy y sincroniza automáticamente los eventos cuando hay cambios.

### 5. Caché Local

Los eventos se almacenan localmente para:
- Reducir latencia
- Minimizar llamadas al servicio externo
- Disponibilidad offline parcial

---

## Conclusiones

Este sistema backend demuestra:

✅ **Arquitectura REST** bien estructurada  
✅ **Integración con servicios externos** mediante WebClient  
✅ **Seguridad robusta** con JWT y Spring Security  
✅ **Persistencia de datos** con JPA/Hibernate  
✅ **Manejo de eventos** en tiempo real con Kafka  
✅ **Código limpio** siguiendo principios SOLID  
✅ **Documentación completa** de API  


## Contacto

**Desarrollador:** Sebastián Fernández  
**Email:** sc.fernandez@um.edu.ar  
**Universidad:** Universidad de Mendoza  
**Materia:** Programación 2

---

**Última actualización:** Diciembre 2025
