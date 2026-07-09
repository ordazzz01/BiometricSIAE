# Guía de Configuración Firebase

## Prerrequisitos

1. Cuenta de Google Cloud
2. Proyecto Firebase creado (BiometricSIAE)
3. Firebase CLI instalado

## 1. Configurar Firebase CLI

```bash
npm install -g firebase-tools
firebase login
firebase init
```

## 2. Firestore Database

### Crear base de datos

1. Ve a Firebase Console → Firestore Database
2. Crea una base de datos en modo de producción
3. Selecciona ubicación: `nam5` (US Central)

### Aplicar reglas

```bash
firebase deploy --only firestore:rules
```

### Crear índices

```bash
firebase deploy --only firestore:indexes
```

## 3. Storage

### Aplicar reglas

```bash
firebase deploy --only storage
```

## 4. Authentication

1. Ve a Firebase Console → Authentication
2. Habilita Email/Password como proveedor
3. Crea un usuario administrador inicial

## 5. Cloud Functions

### Instalar dependencias

```bash
cd firebase/functions
npm install
```

### Desplegar funciones

```bash
firebase deploy --only functions
```

## 6. Variables de Entorno

### Android (google-services.json)

1. Ve a Firebase Console → Project Settings
2. Agrega una app Android con package name: `com.siae.biometricsiae`
3. Descarga `google-services.json`
4. Colócalo en `android/app/`

### Web Admin

Configura las variables en `.env.local`:

```env
NEXT_PUBLIC_FIREBASE_API_KEY=tu_api_key
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=biometricsiae.firebaseapp.com
NEXT_PUBLIC_FIREBASE_PROJECT_ID=biometricsiae
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET=biometricsiae.appspot.com
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID=tu_sender_id
NEXT_PUBLIC_FIREBASE_APP_ID=tu_app_id
```

## 7. Vercel (Web Admin)

1. Importa el repositorio en Vercel
2. Configura las variables de entorno
3. Deploy automático desde GitHub

## 8. Estructura de Colecciones

### organizations

```json
{
  "id": "org_123",
  "name": "Empresa Ejemplo",
  "rfc": "EEE000000000",
  "plan": "enterprise",
  "createdAt": "2024-01-01T00:00:00Z"
}
```

### branches

```json
{
  "id": "branch_001",
  "name": "Sucursal Centro",
  "address": "Av. Principal 123",
  "latitude": 19.4326,
  "longitude": -99.1332,
  "timezone": "America/Mexico_City",
  "active": true
}
```

### employees

```json
{
  "id": "emp_001",
  "code": "EMP001",
  "name": "Juan Pérez",
  "department": "Ventas",
  "branchIds": ["branch_001"],
  "biometricEnrolled": true,
  "active": true
}
```

### attendance

```json
{
  "id": "rec_001",
  "employeeId": "emp_001",
  "branchId": "branch_001",
  "deviceId": "device_001",
  "type": "ENTRY",
  "timestamp": "2024-01-15T08:02:15-06:00",
  "method": "BIOMETRIC",
  "syncStatus": "SYNCED"
}
```

## 9. Troubleshooting

### Error: Permission denied

- Verifica que las Firestore Rules estén desplegadas
- Asegúrate de que el usuario esté autenticado
- Verifica el Custom Claims del usuario

### Error: Storage quota exceeded

- Revisa los límites de Storage en Firebase Console
- Implementa limpieza de evidencias antiguas

### Error: Functions not deployed

- Verifica que Node.js 18+ esté instalado
- Revisa los logs: `firebase functions:log`
