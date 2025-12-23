# Documentación Técnica - Events Proxy Service

## Índice
1. [Descripción General](#descripción-general)
2. [Arquitectura del Sistema](#arquitectura-del-sistema)
3. [Componentes Principales](#componentes-principales)
4. [Funcionalidades Implementadas](#funcionalidades-implementadas)
5. [Flujos de Datos](#flujos-de-datos)
6. [Configuración](#configuración)
7. [Tecnologías Utilizadas](#tecnologías-utilizadas)
8. [Ejemplos de Uso](#ejemplos-de-uso)

---

## Descripción General

**Events Proxy** es un microservicio intermediario diseñado para gestionar la comunicación entre el backend principal de la aplicación de venta de entradas y los servicios externos de infraestructura (Redis y Kafka).

### Propósito

El proxy cumple tres funciones principales:

1. **Intermediario con Redis**: Proporciona acceso controlado a los datos de asientos bloqueados/vendidos almacenados en Redis
2. **Procesamiento de Expiración**: Implementa lógica de negocio para validar y actualizar el estado de asientos bloqueados según su tiempo de expiración
3. **Consumidor de Kafka**: Escucha eventos de cambios en el sistema y notifica al backend para mantener sincronización

### Beneficios de la Arquitectura

- **Separación de responsabilidades**: El backend no necesita conectarse directamente a Redis o Kafka
- **Lógica centralizada**: La validación de expiración de asientos se maneja en un único punto
- **Escalabilidad**: El proxy puede escalarse independientemente del backend
- **Seguridad**: Controla el acceso a los servicios de infraestructura

---

## Arquitectura del Sistema

```
┌─────────────────────────────────────────────────────────────────┐
│                     ARQUITECTURA GENERAL                        │
└─────────────────────────────────────────────────────────────────┘

    ┌──────────────┐                    
    │   Frontend   │                    
    │   (React)    │                    
    └──────┬───────┘                    
           │                                   
           │ HTTP                             
           │                                   
    ┌──────▼──────────────────────────────────-────────┐
    │          Backend Principal (Puerto 8081)         │
    │                                                  │
    │                                                  │
    │  - Gestión de eventos y ventas                   │
    │  - Base de datos MySQL                           │
    └──────┬────────────────────────────────────▲─────┘
           │                                    │
           │ HTTP GET                           │ HTTP POST
           │ /proxy/redis/seats/{id}            │ /internal/events/sync
           │                                    │
    ┌──────▼───────────────────────────────────────────┐
    │       Events Proxy (Puerto 8082)  │
    │                                                  │
    │  ┌─────────────────────────────────────────┐     │
    │  │  RedisProxyController                   │     │
    │  │  - GET /proxy/redis/seats/{eventId}     │     │
    │  └─────────────┬───────────────────────────┘     │
    │                │                                 │
    │  ┌─────────────▼───────────────────────────┐     │
    │  │  RedisProxyService                      │     │
    │  │  - Consulta Redis                       │     │
    │  │  - Procesa expiración de asientos       │     │
    │  │  - Mapea y transforma datos             │     │
    │  └─────────────┬───────────────────────────┘     │
    │                │                                 │
    │  ┌─────────────▼───────────────────────────┐     │
    │  │  EventKafkaConsumerService              │     │
    │  │  - Escucha topic "event-updates"        │     │
    │  │  - Notifica al backend cambios          │     │
    │  └─────────────────────────────────────────┘     │
    └──────┬────────────────────────────────────┬──────┘
           │                                    │
           │ Redis Protocol                     │ Kafka Protocol
           │                                    │
    ┌──────▼──────────┐              ┌──────────▼───────┐
    │  Redis          │              │  Kafka Broker    │
    │  (Puerto 6379)  │              │  (Puerto 9092)   │
    │                 │              │                  │
    │  - Asientos     │              │  Topic:          │
    │    bloqueados   │              │  event-updates   │
    │  - Asientos     │              │                  │
    │    vendidos     │              └──────────────────┘
    └─────────────────┘
```

---

## Componentes Principales

### 1. Controllers

#### `RedisProxyController`
**Ubicación**: `src/main/java/ar/edu/um/events_proxy/controller/RedisProxyController.java`

**Responsabilidad**: Exponer endpoints REST para que el backend consulte datos de Redis.

**Endpoints**:

- `GET /proxy/redis/seats/{eventId}`
  - **Descripción**: Obtiene los asientos ocupados (bloqueados o vendidos) de un evento
  - **Parámetro**: `eventId` - ID del evento
  - **Respuesta exitosa**: HTTP 200 + JSON con asientos
  - **Respuesta sin datos**: HTTP 404

**Características**:
- Logging detallado de cada petición y respuesta
- Manejo de errores con códigos HTTP apropiados
- Validación de datos antes de responder

### 2. Services

#### `RedisProxyService`
**Ubicación**: `src/main/java/ar/edu/um/events_proxy/service/RedisProxyService.java`

**Responsabilidad**: Lógica de negocio para acceso a Redis y procesamiento de datos.

**Funcionalidades principales**:

1. **Consulta a Redis**:
   - Conecta con Redis usando `StringRedisTemplate`
   - Busca datos con clave `evento_{eventId}`
   - Maneja casos de datos no encontrados

2. **Procesamiento de Expiración** (FUNCIONALIDAD DESTACADA):
   - Deserializa JSON de Redis a objetos Java
   - Para cada asiento con campo `expira`:
     - Parsea la fecha de expiración (formato ISO 8601 con zona horaria)
     - Calcula tiempo transcurrido desde la expiración
     - Si pasaron más de 5 minutos: cambia estado a "libre" y elimina campo `expira`
     - Si no pasaron 5 minutos: mantiene el asiento bloqueado
   - Serializa de vuelta a JSON y devuelve al backend

3. **Mapeo de Datos**:
   - Usa Jackson ObjectMapper para conversión JSON ↔ Objetos
   - Maneja campos opcionales con `@JsonInclude(JsonInclude.Include.NON_NULL)`

#### `EventKafkaConsumerService`
**Ubicación**: `src/main/java/ar/edu/um/events_proxy/service/EventKafkaConsumerService.java`

**Responsabilidad**: Consumir mensajes de Kafka y notificar al backend.

**Funcionalidades**:

1. **Escucha de Kafka**:
   - Topic: `event-updates`
   - Group ID: `events-backend-group`
   - Auto-offset: `earliest` (procesa desde el inicio si es nuevo consumidor)

2. **Notificación al Backend**:
   - Usa WebClient (cliente HTTP reactivo)
   - Endpoint: `http://localhost:8081/internal/events/sync`
   - Envía el mensaje recibido de Kafka al backend
   - Logging detallado del proceso

3. **Manejo de Errores**:
   - Try-catch para capturar errores de conexión
   - Logging de errores sin detener el consumidor

### 3. DTOs (Data Transfer Objects)

#### `EventSeatsDTO`
**Ubicación**: `src/main/java/ar/edu/um/events_proxy/dto/EventSeatsDTO.java`

**Estructura**:
```json
{
  "eventoId": 3,
  "asientos": [...]
}
```

**Campos**:
- `eventoId` (Integer): ID del evento
- `asientos` (List<SeatDTO>): Lista de asientos

#### `SeatDTO`
**Ubicación**: `src/main/java/ar/edu/um/events_proxy/dto/SeatDTO.java`

**Estructura**:
```json
{
  "fila": 6,
  "columna": 5,
  "estado": "Bloqueado",
  "expira": "2025-12-16T03:09:35.917167965Z"
}
```

**Campos**:
- `fila` (Integer): Número de fila del asiento
- `columna` (Integer): Número de columna del asiento
- `estado` (String): Estado del asiento ("libre", "Bloqueado", "Vendido")
- `expira` (String, opcional): Fecha/hora de expiración en formato ISO 8601 con zona horaria

**Anotaciones importantes**:
- `@JsonInclude(JsonInclude.Include.NON_NULL)`: No incluye campos null en el JSON de salida
- `@JsonProperty`: Mapea nombres de campos JSON a propiedades Java

### 4. Configuration

#### `RedisConfig`
**Ubicación**: `src/main/java/ar/edu/um/events_proxy/config/RedisConfig.java`

**Beans configurados**:

1. `StringRedisTemplate`: Template para operaciones con Redis usando Strings
2. `ObjectMapper`: Configuración de Jackson para serialización/deserialización JSON

---

## Funcionalidades Implementadas

### 1. Proxy de Acceso a Redis

**Problema que resuelve**: El backend necesita consultar datos de asientos en Redis sin conectarse directamente.

**Solución**:
- El proxy expone un endpoint REST
- El backend hace peticiones HTTP al proxy
- El proxy consulta Redis y devuelve los datos

**Ventajas**:
- Desacoplamiento entre backend y Redis
- Centralización de la lógica de acceso a Redis
- Facilita cambios futuros en la estructura de datos

### 2. Validación de Expiración de Asientos (FUNCIONALIDAD PRINCIPAL)

**Problema que resuelve**: 

Los asientos bloqueados en Redis tienen un tiempo de expiración de 5 minutos. Sin embargo, Redis no actualiza automáticamente el estado de estos asientos cuando expiran. Esto significa que un usuario podría ver asientos como "bloqueados" cuando en realidad ya deberían estar disponibles.

**Solución implementada**:

El proxy implementa una lógica inteligente que:

1. **Recibe datos de Redis** con asientos en diferentes estados
2. **Identifica asientos bloqueados** que tienen campo `expira`
3. **Calcula el tiempo transcurrido** desde la fecha de expiración
4. **Aplica regla de negocio**: Si pasaron más de 5 minutos desde `expira`:
   - Cambia `estado` de "Bloqueado" a "libre"
   - Elimina el campo `expira` (ya no es relevante)
5. **Devuelve datos actualizados** al backend

**Ejemplo práctico**:

**Redis almacena** (a las 01:30):
```json
{
  "eventoId": 3,
  "asientos": [
    {
      "fila": 6,
      "columna": 5,
      "estado": "Bloqueado",
      "expira": "2025-12-16T01:25:00Z"
    },
    {
      "fila": 6,
      "columna": 4,
      "estado": "Bloqueado",
      "expira": "2025-12-16T01:29:00Z"
    },
    {
      "fila": 1,
      "columna": 1,
      "estado": "Vendido"
    }
  ]
}
```

**El proxy procesa y devuelve** (a las 01:30):
```json
{
  "eventoId": 3,
  "asientos": [
    {
      "fila": 6,
      "columna": 5,
      "estado": "libre"
    },
    {
      "fila": 6,
      "columna": 4,
      "estado": "Bloqueado",
      "expira": "2025-12-16T01:29:00Z"
    },
    {
      "fila": 1,
      "columna": 1,
      "estado": "Vendido"
    }
  ]
}
```

**Análisis**:
- Asiento (6,5): Expiró hace 5+ minutos → Cambiado a "libre", sin campo `expira`
- Asiento (6,4): Expiró hace 1 minuto → Mantiene "Bloqueado" con `expira`
- Asiento (1,1): Vendido → Sin cambios

**Beneficios**:
- Los usuarios ven asientos disponibles en tiempo real
- No se requiere actualización manual de Redis
- Mejora la experiencia de usuario
- Aumenta las posibilidades de venta

### 3. Consumidor de Eventos Kafka

**Problema que resuelve**: El backend necesita saber cuándo hay cambios en eventos para sincronizar su base de datos local.

**Solución**:
- El proxy escucha el topic `event-updates` de Kafka
- Cuando llega un mensaje, lo reenvía al backend
- El backend actualiza su base de datos MySQL

**Flujo**:
1. Sistema administrativo crea/modifica un evento
2. Publica mensaje en Kafka topic `event-updates`
3. Proxy consume el mensaje
4. Proxy notifica al backend vía HTTP POST
5. Backend sincroniza su base de datos

**Ventajas**:
- Arquitectura event-driven
- Desacoplamiento entre sistemas
- Procesamiento asíncrono
- Escalabilidad

### 4. Logging y Monitoreo

**Implementación**:
- Logs detallados en cada operación
- Emojis para identificación rápida:
  - 🔵 Petición recibida
  - 🟢 Respuesta exitosa
  - 🔴 Error o no encontrado
- Información de debugging (eventId, datos devueltos)

**Ejemplo de logs**:
```
🔵 PROXY: Recibida petición del backend para eventId: 3
🟢 PROXY: Respondiendo al backend - Datos encontrados para eventId: 3
   Datos: {"eventoId":3,"asientos":[...]}
```

---

## Flujos de Datos

### Flujo 1: Consulta de Asientos

```
┌─────────┐                ┌─────────┐                ┌─────────┐
│ Backend │                │  Proxy  │                │  Redis  │
└────┬────┘                └────┬────┘                └────┬────┘
     │                          │                          │
     │ GET /proxy/redis/        │                          │
     │ seats/3                  │                          │
     ├─────────────────────────>│                          │
     │                          │                          │
     │                          │ 🔵 Log: Petición recibida│
     │                          │                          │
     │                          │ GET evento_3             │
     │                          ├─────────────────────────>│
     │                          │                          │
     │                          │ JSON con asientos        │
     │                          │<─────────────────────────┤
     │                          │                          │
     │                          │ Procesa expiración:      │
     │                          │ - Parsea JSON            │
     │                          │ - Valida cada asiento    │
     │                          │ - Actualiza estados      │
     │                          │ - Serializa JSON         │
     │                          │                          │
     │ HTTP 200 + JSON          │                          │
     │ (datos procesados)       │                          │
     │<─────────────────────────┤                          │
     │                          │                          │
     │                          │ 🟢 Log: Respuesta exitosa│
     │                          │                          │
```

### Flujo 2: Sincronización vía Kafka

```
┌────────┐        ┌────────┐        ┌─────────┐        ┌─────────┐
│ Admin  │        │ Kafka  │        │  Proxy  │        │ Backend │
└───┬────┘        └───┬────┘        └────┬────┘        └────┬────┘
    │                 │                  │                  │
    │ Crea/modifica   │                  │                  │
    │ evento          │                  │                  │
    │                 │                  │                  │
    │ Publica mensaje │                  │                  │
    │ en topic        │                  │                  │
    ├────────────────>│                  │                  │
    │                 │                  │                  │
    │                 │ Mensaje en       │                  │
    │                 │ event-updates    │                  │
    │                 ├─────────────────>│                  │
    │                 │                  │                  │
    │                 │                  │ Log: Mensaje     │
    │                 │                  │ recibido         │
    │                 │                  │                  │
    │                 │                  │ POST /internal/  │
    │                 │                  │ events/sync      │
    │                 │                  ├─────────────────>│
    │                 │                  │                  │
    │                 │                  │                  │ Actualiza
    │                 │                  │                  │ MySQL
    │                 │                  │                  │
    │                 │                  │ HTTP 200         │
    │                 │                  │<─────────────────┤
    │                 │                  │                  │
    │                 │                  │ Log: Backend     │
    │                 │                  │ notificado       │
    │                 │                  │                  │
```

---

## Configuración

### application.yaml

**Ubicación**: `src/main/resources/application.yaml`

```yaml
server:
  port: 8082  # Puerto del proxy

spring:
  application:
    name: events-proxy

  data:
    redis:
      host: 192.168.194.250  # IP del servidor Redis
      port: 6379
      database: 0

  kafka:
    bootstrap-servers: 192.168.194.250:9092  # IP del broker Kafka
    consumer:
      group-id: events-backend-group
      auto-offset-reset: earliest
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.apache.kafka.common.serialization.StringDeserializer

backend:
  base-url: http://localhost:8081  # URL del backend principal
  sync-endpoint: /internal/events/sync  # Endpoint de sincronización
```

### Puertos utilizados

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| Backend Principal | 8081 | API REST principal |
| Events Proxy | 8082 | Este microservicio |
| Redis | 6379 | Base de datos en memoria |
| Kafka | 9092 | Message broker |

---

## Tecnologías Utilizadas

### Framework y Lenguaje
- **Java 21**: Lenguaje de programación
- **Spring Boot 3.5.7**: Framework principal
- **Maven**: Gestión de dependencias

### Dependencias principales

#### Spring Boot Starters
- `spring-boot-starter-web`: Para crear REST APIs
- `spring-boot-starter-webflux`: Cliente HTTP reactivo (WebClient)
- `spring-boot-starter-data-redis`: Integración con Redis

#### Integración
- `spring-kafka`: Cliente de Kafka para consumir mensajes

#### Serialización
- `jackson-databind`: Conversión JSON ↔ Objetos Java

#### Utilidades
- `lombok`: Reducción de código boilerplate (opcional)

### Infraestructura Externa
- **Redis**: Base de datos en memoria para asientos
- **Kafka**: Sistema de mensajería distribuida
- **MySQL**: Base de datos del backend (indirecta)

---

## Ejemplos de Uso

### Ejemplo 1: Consultar asientos de un evento

**Request**:
```http
GET http://localhost:8082/proxy/redis/seats/3
```

**Response exitosa** (HTTP 200):
```json
{
  "eventoId": 3,
  "asientos": [
    {
      "fila": 6,
      "columna": 5,
      "estado": "libre"
    },
    {
      "fila": 6,
      "columna": 4,
      "estado": "Bloqueado",
      "expira": "2025-12-16T01:29:00Z"
    },
    {
      "fila": 1,
      "columna": 1,
      "estado": "Vendido"
    }
  ]
}
```

**Response sin datos** (HTTP 404):
```
(Sin body)
```

### Ejemplo 2: Logs del sistema

**Logs en consola del proxy**:
```
🔵 PROXY: Recibida petición del backend para eventId: 3
🟢 PROXY: Respondiendo al backend - Datos encontrados para eventId: 3
   Datos: {"eventoId":3,"asientos":[{"fila":6,"columna":5,"estado":"libre"},...]}
```

### Ejemplo 3: Mensaje de Kafka

**Mensaje recibido en topic `event-updates`**:
```json
{
  "eventId": 5,
  "action": "UPDATE",
  "timestamp": "2025-12-16T01:30:00Z"
}
```

**Logs del proxy**:
```
=== Kafka message received ===
Content: {"eventId":5,"action":"UPDATE","timestamp":"2025-12-16T01:30:00Z"}
Notifying backend at: http://localhost:8081/internal/events/sync
Backend successfully notified about event change
```

---

## Ejecución del Proyecto

### Requisitos previos
1. Java 21 instalado
2. Maven instalado
3. Redis corriendo en 192.168.194.250:6379
4. Kafka corriendo en 192.168.194.250:9092
5. Backend principal corriendo en localhost:8081

### Comandos

**Compilar**:
```bash
mvn clean install
```

**Ejecutar**:
```bash
mvn spring-boot:run
```

**Ejecutar con limpieza**:
```bash
mvn clean spring-boot:run
```

### Verificación

El proxy está funcionando correctamente cuando ves:
```
Started EventsProxyApplication in X.XXX seconds
```

---

## Conclusiones

Este microservicio **Events Proxy** implementa una arquitectura moderna y escalable que:

1. ✅ **Desacopla** el backend de los servicios de infraestructura
2. ✅ **Centraliza** la lógica de validación de expiración de asientos
3. ✅ **Mejora** la experiencia de usuario mostrando datos en tiempo real
4. ✅ **Facilita** la sincronización entre sistemas mediante eventos
5. ✅ **Proporciona** logging detallado para debugging y monitoreo

### Funcionalidad destacada

La **validación automática de expiración de asientos bloqueados** es la funcionalidad más importante de este proxy, ya que resuelve un problema crítico de negocio: asegurar que los usuarios vean la disponibilidad real de asientos sin depender de actualizaciones manuales o procesos batch en Redis.

---

**Autor**: Sebastian Fernandez  
**Fecha**: Diciembre 2025  
**Versión**: 1.0
