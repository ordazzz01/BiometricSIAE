# Guía de Integración API

## Endpoints

### Base URL

```
https://your-api.run.app/api/v1
```

### Autenticación

Todos los endpoints requieren header de autorización:

```
Authorization: Bearer <access_token>
X-Device-Id: <device_id>
X-Tenant-Id: <tenant_id>
X-App-Version: 1.0.0
```

## Endpoints Disponibles

### POST /auth/device-login

Login del dispositivo.

**Request:**
```json
{
  "deviceId": "device_001",
  "deviceName": "Tablet Recepción",
  "branchId": "branch_001",
  "email": "admin@empresa.com",
  "password": "password123",
  "fcmToken": "fcm_token_here",
  "capabilities": ["FINGERPRINT", "FACE"]
}
```

**Response (200):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs...",
  "refreshToken": "refresh_token_here",
  "expiresIn": 3600,
  "tokenType": "Bearer",
  "tenantId": "org_123",
  "deviceId": "device_001",
  "branchId": "branch_001"
}
```

### POST /auth/refresh

Refrescar access token.

**Request:**
```json
"refresh_token_here"
```

**Response (200):**
```json
{
  "accessToken": "new_access_token",
  "refreshToken": "new_refresh_token",
  "expiresIn": 3600,
  "tokenType": "Bearer",
  "tenantId": "org_123",
  "deviceId": "device_001",
  "branchId": "branch_001"
}
```

### POST /checkins

Registrar checada individual.

**Request:**
```json
{
  "employeeId": "emp_001",
  "branchId": "branch_001",
  "type": "ENTRY",
  "timestamp": "2024-01-15T08:02:15-06:00",
  "method": "BIOMETRIC",
  "deviceId": "device_001",
  "location": {
    "lat": 19.4326,
    "lng": -99.1332
  },
  "faceEvidenceId": "face_001",
  "hash": "sha256:abc123..."
}
```

**Response (201):**
```json
{
  "success": true,
  "recordId": "rec_001",
  "serverTimestamp": "2024-01-15T14:02:15.123Z",
  "duplicate": false,
  "warnings": []
}
```

**Response (409 - Duplicado):**
```json
{
  "success": false,
  "error": "DUPLICATE_CHECKIN",
  "message": "Ya existe un registro de entrada en los últimos 5 minutos",
  "existingRecordId": "rec_existing"
}
```

### POST /checkins/bulk-sync

Sincronización masiva de checadas offline.

**Request:**
```json
{
  "records": [
    {
      "employeeId": "emp_001",
      "branchId": "branch_001",
      "type": "ENTRY",
      "timestamp": "2024-01-15T08:00:00-06:00",
      "method": "BIOMETRIC",
      "deviceId": "device_001",
      "hash": "sha256:abc123..."
    }
  ]
}
```

**Response (200):**
```json
{
  "results": [
    {
      "recordId": "rec_001",
      "success": true,
      "duplicate": false
    }
  ],
  "syncedCount": 1,
  "duplicateCount": 0,
  "errorCount": 0
}
```

### GET /employees/{id}

Obtener empleado por ID.

**Response (200):**
```json
{
  "id": "emp_001",
  "code": "EMP001",
  "name": "Juan Pérez",
  "department": "Ventas",
  "position": "Ejecutivo",
  "branchIds": ["branch_001"],
  "biometricEnrolled": true,
  "active": true
}
```

### GET /device-config/{deviceId}

Obtener configuración del dispositivo.

**Response (200):**
```json
{
  "deviceId": "device_001",
  "name": "Tablet Recepción",
  "branchId": "branch_001",
  "type": "TABLET",
  "features": {
    "faceRequired": false,
    "geofenceEnabled": true,
    "locationRequired": false,
    "kioskMode": true
  },
  "policies": {
    "duplicateWindowMinutes": 5,
    "maxRetries": 5,
    "lockoutAfterFailedAttempts": 3
  },
  "ui": {
    "theme": "light",
    "language": "es",
    "showEmployeeList": true
  }
}
```

### POST /incidents

Registrar incidencia.

**Request:**
```json
{
  "employeeId": "emp_001",
  "branchId": "branch_001",
  "deviceId": "device_001",
  "type": "LATE_ARRIVAL",
  "description": "Llegó 15 minutos tarde",
  "authorizedBy": "supervisor_001",
  "timestamp": "2024-01-15T08:15:00-06:00"
}
```

**Response (201):**
```json
{
  "success": true,
  "incidentId": "inc_001"
}
```

### POST /evidence/upload

Subir evidencia facial.

**Request:**
```json
{
  "attendanceRecordId": "rec_001",
  "tenantId": "org_123",
  "employeeId": "emp_001",
  "imageBase64": "data:image/jpeg;base64,...",
  "faceDetected": true,
  "faceCentered": true,
  "eyesVisible": true,
  "livenessScore": 0.95,
  "timestamp": "2024-01-15T08:02:15-06:00"
}
```

**Response (200):**
```json
{
  "success": true,
  "evidenceId": "face_001",
  "url": "https://storage.googleapis.com/.../evidence.jpg"
}
```

## Códigos de Error

| Código | Descripción |
|--------|-------------|
| 400 | Solicitud inválida |
| 401 | No autenticado |
| 403 | Sin permisos |
| 404 | Recurso no encontrado |
| 409 | Conflicto (duplicado) |
| 422 | Error de validación |
| 429 | Rate limit excedido |
| 500 | Error interno del servidor |

## Estrategia de Idempotencia

Cada checada incluye un hash SHA-256 de:
```
employeeId + type + timestamp_truncated_5min + deviceId
```

El servidor usa este hash para:
1. Detectar duplicados
2. Prevenir procesamiento múltiple
3. Garantizar exactly-once delivery

## Reintentos

La app implementa reintentos exponenciales:

```
Intento 1: 1s
Intento 2: 2s
Intento 3: 4s
Intento 4: 8s
Intento 5: 16s
Intento 6+: 30s (max)
```

Máximo de reintentos: 5 antes de marcar como FAILED.
