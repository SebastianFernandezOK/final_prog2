# Guía de Presentación para el Profesor

**Proyecto:** Sistema de Gestión de Eventos  
**Estudiante:** Santiago Fernández  
**Materia:** Programación 2

---

## 📋 Resumen Ejecutivo

Este documento es una guía rápida para presentar el proyecto al profesor, destacando los aspectos más importantes y las funcionalidades implementadas.

---

## 🎯 Objetivos del Proyecto

1. **Crear un backend REST** para gestión de eventos y venta de asientos
2. **Integrar con servicio externo** mediante API REST
3. **Implementar seguridad** con JWT y Spring Security
4. **Persistir datos** en MySQL con JPA/Hibernate
5. **Sincronización automática** mediante Kafka

---

## 🏗️ Arquitectura en 3 Capas

### Capa de Presentación (Controllers)
```
Cliente → Controller → Service → Repository → Database
                    ↓
              Servicio Externo
```

**Controllers implementados:**
- `EventSummary_db_Controller` - Consulta de eventos resumidos
- `Events_db_Controller` - Consulta de eventos completos
- `Block_seat_db_Controller` - Bloqueo de asientos
- `Sale_seat_db_Controller` - Venta de asientos
- `UserController` - CRUD de usuarios
- `InternalController` - Sincronización Kafka

### Capa de Negocio (Services)
- Lógica de negocio centralizada
- Validaciones
- Integración con servicios externos
- Transacciones

### Capa de Datos (Repositories)
- Spring Data JPA
- Consultas automáticas
- Persistencia en MySQL

---

## 🔄 Flujo de Venta de Asientos (Caso de Uso Principal)

### Diagrama de Secuencia

```
Cliente          Backend          Servicio Externo
  |                |                     |
  |--1. GET eventos---------------->    |
  |<------Lista de eventos-----------|   |
  |                |                     |
  |--2. POST bloquear asientos----->    |
  |                |--WebClient--------->|
  |                |<---Bloqueados-------|
  |<------Confirmación---------------|   |
  |                |                     |
  |--3. Procesar pago (frontend)--->    |
  |                |                     |
  |--4. POST vender asientos------->    |
  |                |--WebClient--------->|
  |                |<---Venta OK---------|
  |<------Ticket de venta------------|   |
```

### Paso a Paso

**1. Consultar Eventos Disponibles**
```http
GET /api/db/events/summary
```
- Sin autenticación
- Retorna lista de eventos con asientos disponibles
- Datos en caché local (sincronizados con Kafka)

**2. Bloquear Asientos Temporalmente**
```http
POST /api/db/block-seats
{
  "eventoId": 1,
  "asientos": [
    {"fila": 5, "columna": 10},
    {"fila": 5, "columna": 11}
  ]
}
```
- Reserva temporal (timeout configurable)
- Previene doble venta
- Comunicación con servicio externo

**3. Confirmar Venta**
```http
POST /api/db/sale-seats
{
  "eventoId": 1,
  "fecha": "2024-12-16T20:00:00-03:00",
  "precioVenta": 10000.00,
  "asientos": [
    {"fila": 5, "columna": 10, "persona": "Juan Pérez"},
    {"fila": 5, "columna": 11, "persona": "María García"}
  ]
}
```
- Venta definitiva
- Asignación de nombres a asientos
- Generación de ID de venta

---

## 🔐 Sistema de Seguridad

### Arquitectura de Seguridad

```
Request → JwtAuthFilter → SecurityFilterChain → Controller
            |
            ├─ Rutas públicas → Skip JWT
            └─ Rutas protegidas → Validar JWT
```

### Rutas Públicas (No requieren autenticación)
✅ `/api/auth/login` - Login  
✅ `/api/db/events/**` - Consulta de eventos  
✅ `/api/db/block-seats/**` - Bloqueo de asientos  
✅ `/api/db/sale-seats/**` - Venta de asientos  
✅ `/internal/**` - Endpoints internos  

### Rutas Protegidas (Requieren JWT)
🔒 `/api/users/**` - Gestión de usuarios

### Implementación Destacada

**JwtAuthFilter.java** - Filtro personalizado que:
- Extrae token del header `Authorization: Bearer <token>`
- Valida firma y expiración
- **Excluye rutas públicas** (mejora implementada)
- Inyecta usuario en contexto de seguridad

```java
// Verificación de rutas públicas
if (path.startsWith("/api/db/events/") || 
    path.startsWith("/api/db/block-seats") ||
    path.startsWith("/api/db/sale-seats")) {
    filterChain.doFilter(request, response);
    return;
}
```

---

## 🔌 Integración con Servicio Externo

### ExternalAuthService - Autenticación Automática

**Características:**
- Obtiene token JWT automáticamente al iniciar
- Refresca token antes de expiración (buffer de 30 segundos)
- Reintenta automáticamente en caso de 401
- Scheduler para renovación proactiva

**Flujo:**
```
App Start → Login Externo → Obtener Token → Guardar Token
                                              ↓
                                    Programar Refresh
                                              ↓
                                    Antes de Expirar → Renovar Token
```

### WebClient Configurado

**Filtros implementados:**
1. **authFilter** - Inyecta token en cada request
2. **retryOn401** - Reintenta con nuevo token si falla

```java
@Bean("externalApiClient")
public WebClient externalApiClient(
    WebClient.Builder builder, 
    ExternalAuthService authService, 
    @Value("${externalAuth.base-url}") String baseUrl
) {
    return builder
        .baseUrl(baseUrl)
        .filter(authFilter)
        .filter(retryOn401)
        .build();
}
```

---

## 📊 Base de Datos

### Modelo de Datos

**Entidades:**

1. **User**
   - `id_user` (PK)
   - `name`
   - `surname`
   - `mail`
   - `password`

2. **Event_db**
   - `id` (PK)
   - `nombre`
   - `fecha`
   - `lugar`
   - `asientos` (JSON serializado)
   - Sincronizado con servicio externo

3. **EventSummary_db**
   - `id` (PK)
   - `nombre`
   - `fecha`
   - `lugar`
   - `asientosDisponibles`
   - `precioBase`
   - Versión optimizada para listados

### Configuración JPA

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update  # Crea/actualiza tablas automáticamente
    show-sql: true      # Muestra queries SQL en consola
```

---

## 🔄 Sincronización con Kafka

### Arquitectura de Sincronización

```
Servicio Externo → Kafka → Proxy Service → Backend
                                              |
                                    POST /internal/events/sync
                                              |
                                    Sincronizar todos los eventos
                                              |
                                    Actualizar base de datos local
```

### InternalController

```java
@PostMapping("/internal/events/sync")
public ResponseEntity<String> syncEvents(@RequestBody String message) {
    logger.info("Recibida notificación de sincronización desde proxy");
    eventSummaryService.syncAllEventsFromExternal();
    return ResponseEntity.ok("Sincronización completada");
}
```

**Ventajas:**
- Datos siempre actualizados
- Reducción de latencia (caché local)
- Disponibilidad parcial sin servicio externo

---

## 🛠️ Tecnologías y Herramientas

### Stack Tecnológico

| Categoría | Tecnología | Versión |
|-----------|-----------|---------|
| Lenguaje | Java | 21 |
| Framework | Spring Boot | 3.5.7 |
| Persistencia | Spring Data JPA | - |
| Base de Datos | MySQL | 8.0+ |
| Seguridad | Spring Security | 6.5.6 |
| JWT | jjwt | 0.11.5 |
| HTTP Client | WebFlux/WebClient | - |
| Build Tool | Maven | 3.6+ |
| Utilidades | Lombok | - |

### Dependencias Clave

```xml
<!-- Spring Boot Starter Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Spring Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Spring Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>

<!-- WebFlux (WebClient) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>
```

---

## 📝 Patrones de Diseño Implementados

### 1. MVC (Model-View-Controller)
- **Model**: Entities + DTOs
- **View**: JSON responses (REST)
- **Controller**: REST Controllers

### 2. Repository Pattern
```java
public interface Event_db_Repository extends JpaRepository<Event_db, Long> {
    // Métodos automáticos: findAll, findById, save, delete
}
```

### 3. Service Layer Pattern
```java
@Service
public class Sale_seat_db_Service {
    // Lógica de negocio separada de controllers
}
```

### 4. DTO Pattern
```java
// Separación entre entidades de BD y objetos de transferencia
Event_db (Entity) ←→ Event_db_DTO (DTO)
```

### 5. Filter Chain Pattern
```java
// Cadena de filtros de seguridad
Request → Filter1 → Filter2 → ... → Controller
```

---

## 🧪 Pruebas y Validación

### Endpoints de Prueba

**1. Health Check**
```bash
curl http://localhost:8081/api/db/events/summary
```

**2. Bloqueo de Asientos**
```bash
curl -X POST http://localhost:8081/api/db/block-seats \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "asientos": [{"fila": 1, "columna": 1}]
  }'
```

**3. Venta de Asientos**
```bash
curl -X POST http://localhost:8081/api/db/sale-seats \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "fecha": "2024-12-16T20:00:00-03:00",
    "precioVenta": 5000.00,
    "asientos": [{
      "fila": 1,
      "columna": 1,
      "persona": "Test User"
    }]
  }'
```

### Herramientas de Prueba

- **Postman** - Colección de requests
- **cURL** - Línea de comandos
- **Navegador** - Endpoints GET
- **MySQL Workbench** - Verificar datos

---

## 🎓 Conceptos Aprendidos y Aplicados

### Spring Framework
✅ Inyección de dependencias  
✅ Anotaciones (@RestController, @Service, @Repository)  
✅ Configuración con @Bean  
✅ Properties con @Value  

### Spring Boot
✅ Auto-configuración  
✅ Embedded Tomcat  
✅ application.yaml  
✅ DevTools para desarrollo  

### Spring Data JPA
✅ Repositorios automáticos  
✅ Entidades con @Entity  
✅ Relaciones entre tablas  
✅ Queries personalizadas  

### Spring Security
✅ Configuración de seguridad  
✅ Filtros personalizados  
✅ Autenticación stateless  
✅ Autorización por rutas  

### REST API
✅ Verbos HTTP (GET, POST, PUT, DELETE)  
✅ Status codes apropiados  
✅ JSON como formato de intercambio  
✅ Versionado de API  

### Integración de Servicios
✅ WebClient para llamadas HTTP  
✅ Manejo de tokens  
✅ Retry logic  
✅ Timeout handling  

---

## 🚀 Mejoras Implementadas Durante el Desarrollo

### 1. Filtro JWT Optimizado
**Problema:** Error 403 en rutas públicas  
**Solución:** Exclusión de rutas públicas antes de validar JWT

```java
// Antes: Validaba JWT en todas las rutas
// Después: Skip JWT en rutas públicas
if (path.startsWith("/api/db/events/") || ...) {
    filterChain.doFilter(request, response);
    return;
}
```

### 2. Autenticación Automática con Servicio Externo
**Problema:** Tokens expiraban durante operaciones  
**Solución:** Refresh automático con scheduler

### 3. Sincronización con Kafka
**Problema:** Datos desactualizados  
**Solución:** Endpoint interno para sincronización automática

### 4. Manejo de Excepciones Centralizado
**Problema:** Errores inconsistentes  
**Solución:** @RestControllerAdvice para manejo global

---

## 📈 Escalabilidad y Mejoras Futuras

### Posibles Extensiones

1. **Redis Cache**
   - Caché distribuido para eventos
   - Reducir carga en MySQL

2. **Tests Automatizados**
   - JUnit + Mockito
   - Tests de integración
   - Coverage > 80%

3. **Documentación OpenAPI/Swagger**
   - Interfaz interactiva
   - Generación automática de docs

4. **Métricas y Monitoreo**
   - Spring Boot Actuator
   - Prometheus + Grafana
   - Logs centralizados

5. **Microservicios**
   - Separar eventos, ventas, usuarios
   - Service discovery
   - API Gateway

6. **Containerización**
   - Docker
   - Docker Compose
   - Kubernetes

---

## 🎬 Demostración en Vivo

### Script de Demostración

**1. Mostrar Estructura del Proyecto** (2 min)
- Explicar arquitectura en capas
- Mostrar organización de paquetes

**2. Consultar Eventos** (2 min)
```bash
curl http://localhost:8081/api/db/events/summary
```
- Mostrar respuesta JSON
- Explicar sincronización con servicio externo

**3. Bloquear Asientos** (3 min)
```bash
curl -X POST http://localhost:8081/api/db/block-seats \
  -H "Content-Type: application/json" \
  -d '{"eventoId": 1, "asientos": [{"fila": 1, "columna": 1}]}'
```
- Mostrar request y response
- Explicar comunicación con WebClient

**4. Vender Asientos** (3 min)
```bash
curl -X POST http://localhost:8081/api/db/sale-seats \
  -H "Content-Type: application/json" \
  -d '{...}'
```
- Mostrar venta exitosa
- Verificar en base de datos

**5. Mostrar Seguridad JWT** (3 min)
- Intentar acceder a `/api/users` sin token → 403
- Login y obtener token
- Acceder con token → 200

**6. Mostrar Código Relevante** (5 min)
- `JwtAuthFilter.java` - Filtro personalizado
- `ExternalAuthService.java` - Autenticación automática
- `Sale_seat_db_Service.java` - Lógica de negocio

---

## 📚 Documentación Adicional

### Archivos de Documentación

1. **DOCUMENTACION.md** - Documentación técnica completa
2. **GUIA_PRESENTACION.md** - Este archivo
3. **README.md** - Instrucciones de instalación
4. **application.yaml** - Configuración de la aplicación

### Recursos en el Código

- Comentarios en clases principales
- JavaDoc en métodos públicos
- Logs informativos en operaciones críticas

---

## ✅ Checklist de Presentación

### Antes de Presentar

- [ ] Servidor corriendo en puerto 8081
- [ ] MySQL con base de datos `eventos_backend`
- [ ] Servicio externo disponible (192.168.194.250:8080)
- [ ] Postman/cURL configurado con ejemplos
- [ ] Código limpio y comentado
- [ ] Documentación actualizada

### Durante la Presentación

- [ ] Explicar arquitectura general
- [ ] Demostrar flujo de venta completo
- [ ] Mostrar integración con servicio externo
- [ ] Explicar seguridad JWT
- [ ] Mostrar código relevante
- [ ] Responder preguntas técnicas

### Puntos Clave a Destacar

✨ **Arquitectura REST** bien estructurada  
✨ **Integración compleja** con servicio externo  
✨ **Seguridad robusta** con JWT  
✨ **Sincronización automática** con Kafka  
✨ **Código limpio** y mantenible  
✨ **Documentación completa**  

---

## 💡 Preguntas Frecuentes del Profesor

### ¿Por qué usaste Spring Boot?
- Framework estándar de la industria
- Configuración automática
- Gran ecosistema de librerías
- Facilita desarrollo de APIs REST

### ¿Cómo manejas la concurrencia en la venta de asientos?
- El servicio externo maneja el estado real
- Bloqueo temporal previene doble venta
- Validación en cada operación

### ¿Qué pasa si el servicio externo no está disponible?
- Eventos en caché local siguen disponibles
- Operaciones de venta/bloqueo fallan con error claro
- Sistema puede recuperarse automáticamente

### ¿Por qué JWT y no sesiones?
- Stateless (escalable)
- No requiere almacenamiento en servidor
- Estándar de la industria
- Fácil integración con microservicios

### ¿Cómo garantizas la seguridad?
- Tokens JWT firmados
- HTTPS en producción (recomendado)
- Validación en cada request
- Passwords hasheados (BCrypt recomendado)

---

## 🎯 Conclusión

Este proyecto demuestra:

1. **Dominio de Spring Boot** y su ecosistema
2. **Integración de servicios** mediante REST APIs
3. **Implementación de seguridad** con JWT
4. **Persistencia de datos** con JPA/Hibernate
5. **Arquitectura limpia** y mantenible
6. **Documentación profesional**

El sistema está **listo para producción** con mejoras menores y puede servir como base para proyectos más complejos.

---

**¡Gracias por su atención!**

*Santiago Fernández - Programación 2 - Universidad de Mendoza*
