# 📡 Ejemplos de API - Colección de Requests

Esta guía contiene ejemplos completos de todos los endpoints de la API para usar con **cURL**, **Postman**, o cualquier cliente HTTP.

---

## 📋 Índice

1. [Eventos](#eventos)
2. [Bloqueo de Asientos](#bloqueo-de-asientos)
3. [Venta de Asientos](#venta-de-asientos)
4. [Usuarios](#usuarios)
5. [Autenticación](#autenticación)
6. [Flujos Completos](#flujos-completos)

---

## 🎭 Eventos

### Listar Todos los Eventos (Resumido)

**Request:**
```bash
curl -X GET http://localhost:8081/api/db/events/summary
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Concierto Rock Nacional",
    "fecha": "2024-12-20T21:00:00",
    "lugar": "Estadio Central",
    "asientosDisponibles": 150,
    "precioBase": 5000.0
  },
  {
    "id": 2,
    "nombre": "Festival de Jazz",
    "fecha": "2024-12-25T19:00:00",
    "lugar": "Teatro Municipal",
    "asientosDisponibles": 80,
    "precioBase": 7500.0
  }
]
```

### Obtener Evento por ID (Resumido)

**Request:**
```bash
curl -X GET http://localhost:8081/api/db/events/summary/1
```

**Response (200 OK):**
```json
{
  "id": 1,
  "nombre": "Concierto Rock Nacional",
  "fecha": "2024-12-20T21:00:00",
  "lugar": "Estadio Central",
  "asientosDisponibles": 150,
  "precioBase": 5000.0
}
```

**Response (404 Not Found):**
```json
{
  "timestamp": "2024-12-16T20:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Evento no encontrado",
  "path": "/api/db/events/summary/999"
}
```

### Listar Eventos Completos (con Matriz de Asientos)

**Request:**
```bash
curl -X GET http://localhost:8081/api/db/events
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "nombre": "Concierto Rock Nacional",
    "fecha": "2024-12-20T21:00:00",
    "lugar": "Estadio Central",
    "descripcion": "Gran concierto de rock",
    "asientos": [
      [
        {"fila": 1, "columna": 1, "estado": "DISPONIBLE"},
        {"fila": 1, "columna": 2, "estado": "DISPONIBLE"},
        {"fila": 1, "columna": 3, "estado": "VENDIDO"}
      ],
      [
        {"fila": 2, "columna": 1, "estado": "BLOQUEADO"},
        {"fila": 2, "columna": 2, "estado": "DISPONIBLE"},
        {"fila": 2, "columna": 3, "estado": "DISPONIBLE"}
      ]
    ]
  }
]
```

### Obtener Evento Completo por ID

**Request:**
```bash
curl -X GET http://localhost:8081/api/db/events/1
```

---

## 🔒 Bloqueo de Asientos

### Bloquear Asientos

**Request:**
```bash
curl -X POST http://localhost:8081/api/db/block-seats \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "asientos": [
      {
        "fila": 5,
        "columna": 10
      },
      {
        "fila": 5,
        "columna": 11
      }
    ]
  }'
```

**Response (200 OK):**
```json
{
  "resultado": true,
  "descripcion": "Asientos bloqueados exitosamente",
  "eventoId": 1,
  "asientos": [
    {
      "estado": "BLOQUEADO",
      "fila": 5,
      "columna": 10
    },
    {
      "estado": "BLOQUEADO",
      "fila": 5,
      "columna": 11
    }
  ]
}
```

**Response (400 Bad Request) - Asiento ya ocupado:**
```json
{
  "resultado": false,
  "descripcion": "Algunos asientos no están disponibles",
  "eventoId": 1,
  "asientos": [
    {
      "estado": "VENDIDO",
      "fila": 5,
      "columna": 10
    },
    {
      "estado": "BLOQUEADO",
      "fila": 5,
      "columna": 11
    }
  ]
}
```

### Ejemplo con JavaScript (Fetch API)

```javascript
async function bloquearAsientos(eventoId, asientos) {
  const response = await fetch('http://localhost:8081/api/db/block-seats', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      eventoId,
      asientos
    })
  });
  
  if (!response.ok) {
    throw new Error('Error al bloquear asientos');
  }
  
  return await response.json();
}

// Uso
const asientos = [
  { fila: 5, columna: 10 },
  { fila: 5, columna: 11 }
];

bloquearAsientos(1, asientos)
  .then(result => console.log('Bloqueados:', result))
  .catch(error => console.error('Error:', error));
```

---

## 💳 Venta de Asientos

### Vender Asientos

**Request:**
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

**Response (200 OK):**
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
    },
    {
      "fila": 5,
      "columna": 11,
      "persona": "María García",
      "estado": "VENDIDO"
    }
  ]
}
```

**Response (400 Bad Request) - Asientos no disponibles:**
```json
{
  "eventoId": 1,
  "ventaId": null,
  "fechaVenta": null,
  "precioVenta": 10000.00,
  "resultado": false,
  "descripcion": "Asientos no disponibles para venta",
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

### Ejemplo con JavaScript (Axios)

```javascript
import axios from 'axios';

async function venderAsientos(eventoId, asientos, precioVenta) {
  try {
    const response = await axios.post('http://localhost:8081/api/db/sale-seats', {
      eventoId,
      fecha: new Date().toISOString(),
      precioVenta,
      asientos
    });
    
    return response.data;
  } catch (error) {
    console.error('Error en la venta:', error.response?.data || error.message);
    throw error;
  }
}

// Uso
const asientos = [
  { fila: 5, columna: 10, persona: "Juan Pérez" },
  { fila: 5, columna: 11, persona: "María García" }
];

venderAsientos(1, asientos, 10000.00)
  .then(venta => {
    console.log('Venta exitosa!');
    console.log('ID de venta:', venta.ventaId);
    console.log('Asientos vendidos:', venta.asientos);
  })
  .catch(error => console.error('Error:', error));
```

---

## 👥 Usuarios

### Crear Usuario

**Request:**
```bash
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <tu-token-jwt>" \
  -d '{
    "name": "Juan",
    "surname": "Pérez",
    "mail": "juan.perez@example.com",
    "password": "password123"
  }'
```

**Response (201 Created):**
```json
{
  "idUser": 1,
  "name": "Juan",
  "surname": "Pérez",
  "mail": "juan.perez@example.com"
}
```

### Listar Usuarios

**Request:**
```bash
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer <tu-token-jwt>"
```

**Response (200 OK):**
```json
[
  {
    "idUser": 1,
    "name": "Juan",
    "surname": "Pérez",
    "mail": "juan.perez@example.com"
  },
  {
    "idUser": 2,
    "name": "María",
    "surname": "García",
    "mail": "maria.garcia@example.com"
  }
]
```

### Obtener Usuario por ID

**Request:**
```bash
curl -X GET http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer <tu-token-jwt>"
```

**Response (200 OK):**
```json
{
  "idUser": 1,
  "name": "Juan",
  "surname": "Pérez",
  "mail": "juan.perez@example.com"
}
```

### Actualizar Usuario

**Request:**
```bash
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <tu-token-jwt>" \
  -d '{
    "name": "Juan Carlos",
    "surname": "Pérez López",
    "mail": "juancarlos.perez@example.com",
    "password": "newpassword456"
  }'
```

**Response (200 OK):**
```json
{
  "idUser": 1,
  "name": "Juan Carlos",
  "surname": "Pérez López",
  "mail": "juancarlos.perez@example.com"
}
```

### Eliminar Usuario

**Request:**
```bash
curl -X DELETE http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer <tu-token-jwt>"
```

**Response (204 No Content):**
```
(Sin contenido)
```

---

## 🔐 Autenticación

### Login

**Request:**
```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "juan.perez@example.com",
    "password": "password123"
  }'
```

**Response (200 OK):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqdWFuLnBlcmV6QGV4YW1wbGUuY29tIiwiaWF0IjoxNjM5NjgwMDAwLCJleHAiOjE2Mzk3NjY0MDB9.signature",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

**Response (401 Unauthorized):**
```json
{
  "timestamp": "2024-12-16T20:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciales inválidas"
}
```

### Usar Token JWT

Una vez obtenido el token, incluirlo en el header `Authorization`:

```bash
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

---

## 🔄 Flujos Completos

### Flujo 1: Compra de Asientos (Cliente)

```javascript
// 1. Obtener eventos disponibles
const eventos = await fetch('http://localhost:8081/api/db/events/summary')
  .then(res => res.json());

console.log('Eventos disponibles:', eventos);

// 2. Usuario selecciona evento y asientos
const eventoSeleccionado = 1;
const asientosSeleccionados = [
  { fila: 5, columna: 10 },
  { fila: 5, columna: 11 }
];

// 3. Bloquear asientos temporalmente
const bloqueados = await fetch('http://localhost:8081/api/db/block-seats', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    eventoId: eventoSeleccionado,
    asientos: asientosSeleccionados
  })
}).then(res => res.json());

if (!bloqueados.resultado) {
  console.error('No se pudieron bloquear los asientos');
  return;
}

console.log('Asientos bloqueados:', bloqueados);

// 4. Usuario completa formulario con nombres
const asientosConNombres = [
  { fila: 5, columna: 10, persona: "Juan Pérez" },
  { fila: 5, columna: 11, persona: "María García" }
];

// 5. Procesar pago (simulado)
const pagoExitoso = await procesarPago(10000.00);

if (!pagoExitoso) {
  console.error('Error en el pago');
  return;
}

// 6. Confirmar venta
const venta = await fetch('http://localhost:8081/api/db/sale-seats', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    eventoId: eventoSeleccionado,
    fecha: new Date().toISOString(),
    precioVenta: 10000.00,
    asientos: asientosConNombres
  })
}).then(res => res.json());

if (venta.resultado) {
  console.log('¡Compra exitosa!');
  console.log('ID de venta:', venta.ventaId);
  console.log('Asientos:', venta.asientos);
  
  // Mostrar ticket o confirmación
  mostrarTicket(venta);
} else {
  console.error('Error en la venta:', venta.descripcion);
}
```

### Flujo 2: Gestión de Usuarios (Admin)

```bash
# 1. Login como administrador
TOKEN=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin@example.com", "password": "admin123"}' \
  | jq -r '.token')

echo "Token obtenido: $TOKEN"

# 2. Crear nuevo usuario
curl -X POST http://localhost:8081/api/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Nuevo",
    "surname": "Usuario",
    "mail": "nuevo@example.com",
    "password": "password123"
  }'

# 3. Listar todos los usuarios
curl -X GET http://localhost:8081/api/users \
  -H "Authorization: Bearer $TOKEN"

# 4. Actualizar usuario
curl -X PUT http://localhost:8081/api/users/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Usuario",
    "surname": "Actualizado",
    "mail": "actualizado@example.com",
    "password": "newpass456"
  }'

# 5. Eliminar usuario
curl -X DELETE http://localhost:8081/api/users/1 \
  -H "Authorization: Bearer $TOKEN"
```

### Flujo 3: Sincronización con Kafka (Interno)

```bash
# Este endpoint es llamado automáticamente por el proxy cuando Kafka detecta cambios

curl -X POST http://localhost:8081/internal/events/sync \
  -H "Content-Type: application/json" \
  -d '{"message": "Evento actualizado en sistema externo"}'
```

**Response:**
```json
{
  "status": "success",
  "message": "Sincronización de eventos completada exitosamente"
}
```

---

## 📦 Colección Postman

### Importar en Postman

1. Crear nueva colección "Events Backend"
2. Agregar variable de entorno:
   - `base_url`: `http://localhost:8081`
   - `token`: (se llenará después del login)

### Estructura de Colección

```
Events Backend/
├── Eventos/
│   ├── GET Lista eventos resumidos
│   ├── GET Evento resumido por ID
│   ├── GET Lista eventos completos
│   └── GET Evento completo por ID
├── Asientos/
│   ├── POST Bloquear asientos
│   └── POST Vender asientos
├── Usuarios/
│   ├── POST Crear usuario
│   ├── GET Listar usuarios
│   ├── GET Obtener usuario
│   ├── PUT Actualizar usuario
│   └── DELETE Eliminar usuario
└── Auth/
    └── POST Login
```

---

## 🧪 Tests de Integración

### Script de Test Completo

```bash
#!/bin/bash

BASE_URL="http://localhost:8081"

echo "=== Test 1: Obtener eventos ==="
curl -s "$BASE_URL/api/db/events/summary" | jq '.'

echo -e "\n=== Test 2: Bloquear asientos ==="
curl -s -X POST "$BASE_URL/api/db/block-seats" \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "asientos": [{"fila": 1, "columna": 1}]
  }' | jq '.'

echo -e "\n=== Test 3: Vender asientos ==="
curl -s -X POST "$BASE_URL/api/db/sale-seats" \
  -H "Content-Type: application/json" \
  -d '{
    "eventoId": 1,
    "fecha": "'$(date -u +"%Y-%m-%dT%H:%M:%S%z")'",
    "precioVenta": 5000.00,
    "asientos": [{
      "fila": 1,
      "columna": 1,
      "persona": "Test User"
    }]
  }' | jq '.'

echo -e "\n=== Tests completados ==="
```

---

## 📊 Códigos de Estado HTTP

| Código | Significado | Cuándo se usa |
|--------|-------------|---------------|
| 200 | OK | Operación exitosa |
| 201 | Created | Recurso creado exitosamente |
| 204 | No Content | Eliminación exitosa |
| 400 | Bad Request | Datos inválidos |
| 401 | Unauthorized | Sin autenticación o token inválido |
| 403 | Forbidden | Sin permisos |
| 404 | Not Found | Recurso no encontrado |
| 500 | Internal Server Error | Error del servidor |

---

## 💡 Tips y Buenas Prácticas

### 1. Manejo de Errores

Siempre verificar el código de estado HTTP:

```javascript
const response = await fetch(url, options);

if (!response.ok) {
  const error = await response.json();
  console.error('Error:', error.message);
  throw new Error(error.message);
}

const data = await response.json();
```

### 2. Timeout en Requests

Configurar timeout para evitar esperas infinitas:

```javascript
const controller = new AbortController();
const timeoutId = setTimeout(() => controller.abort(), 5000);

try {
  const response = await fetch(url, {
    signal: controller.signal
  });
  clearTimeout(timeoutId);
} catch (error) {
  if (error.name === 'AbortError') {
    console.error('Request timeout');
  }
}
```

### 3. Retry Logic

Reintentar en caso de errores temporales:

```javascript
async function fetchWithRetry(url, options, maxRetries = 3) {
  for (let i = 0; i < maxRetries; i++) {
    try {
      const response = await fetch(url, options);
      if (response.ok) return response;
    } catch (error) {
      if (i === maxRetries - 1) throw error;
      await new Promise(resolve => setTimeout(resolve, 1000 * (i + 1)));
    }
  }
}
```

---

## 🔍 Debugging

### Ver Headers de Response

```bash
curl -v http://localhost:8081/api/db/events/summary
```

### Ver Solo Status Code

```bash
curl -o /dev/null -s -w "%{http_code}\n" http://localhost:8081/api/db/events/summary
```

### Medir Tiempo de Respuesta

```bash
curl -o /dev/null -s -w "Time: %{time_total}s\n" http://localhost:8081/api/db/events/summary
```

---

**¡Listo para usar! 🚀**

Para más información, consulta la [documentación completa](DOCUMENTACION.md).
