# Arquitectura - Checador Biométrico de Asistencias v1

## Visión General

El proyecto BiometricSIAE es un monorepo que contiene:

1. **App Android** - Checador biométrico de asistencias
2. **Panel Web Admin** - Dashboard administrativo en Next.js
3. **Firebase Config** - Reglas, funciones y configuración
4. **CI/CD** - GitHub Actions para automatización

## Arquitectura Android

### Estructura de Módulos

```
app/
├── data/
│   ├── local/          # Room Database + DAOs
│   ├── remote/         # API Client + Firebase
│   ├── repository/     # Implementaciones
│   └── mapper/         # Mapeadores de datos
├── domain/
│   ├── model/          # Modelos de dominio
│   └── usecase/        # Casos de uso
├── feature/
│   ├── auth/           # Login y autenticación
│   ├── checkin/        # Pantalla principal de checada
│   ├── camera/         # Captura facial
│   ├── settings/       # Configuración
│   └── admin/          # Administración local
├── sync/               # WorkManager + Cola offline
├── security/           # Keystore, Root detection, etc.
└── util/               # Utilidades
```

### Patrón de Arquitectura

**Clean Architecture + MVVM:**

```
UI (Compose) → ViewModel → UseCase → Repository → DataSource
```

- **UI**: Jetpack Compose con Material 3
- **ViewModel**: Manejo de estado con StateFlow
- **UseCase**: Lógica de negocio pura
- **Repository**: Orquestación de datos
- **DataSource**: Room (local) o Retrofit/Firebase (remoto)

### Flujo de Datos

1. **Online**: UI → ViewModel → Repository → API → Server
2. **Offline**: UI → ViewModel → Repository → Room → Cola Sync
3. **Sync**: WorkManager → Repository → Cola → API → Room (update status)

## Arquitectura Firebase

### Colecciones Firestore

```
organizations/{orgId}
├── branches/{branchId}
├── employees/{employeeId}
├── attendance/{recordId}
├── devices/{deviceId}
├── incidents/{incidentId}
├── sync_logs/{logId}
├── audit_logs/{logId}
└── app_settings/{settingId}
```

### Seguridad

- **Firestore Rules**: Validación por tenant y rol
- **Storage Rules**: Validación de tipo y tamaño de archivo
- **App Check**: Protección de endpoints

## Arquitectura Web Admin

### Stack

- **Framework**: Next.js 14 (App Router)
- **UI**: Tailwind CSS + shadcn/ui
- **Auth**: Firebase Auth
- **Database**: Firestore (client-side)
- **Deploy**: Vercel

### Estructura

```
src/
├── app/
│   ├── (auth)/         # Login
│   └── (dashboard)/    # Páginas principales
├── components/         # Componentes reutilizables
├── hooks/              # Custom hooks
├── lib/                # Firebase config, utilidades
└── types/              # Tipos TypeScript
```

## Seguridad

### Capas de Seguridad

1. **Android Keystore**: Almacenamiento cifrado de tokens
2. **Certificate Pinning**: Protección contra MITM
3. **Root Detection**: Detección de dispositivos comprometidos
4. **Secure Logger**: Redacción de datos sensibles en logs
5. **Firebase Auth + Custom Claims**: Autenticación por tenant
6. **Firestore Rules**: Control de acceso a nivel documento

### Flujo de Autenticación

```
1. Login → Backend valida credenciales
2. Backend retorna JWT + refresh token
3. App almacena tokens en Keystore
4. Cada request incluye Bearer token
5. Refresh token automático al expirar
```

## Sincronización Offline

### Algoritmo

1. **Detección**: NetworkUtils monitorea conectividad
2. **Cola**: Registros se guardan en Room con status PENDING
3. **Sync**: WorkManager ejecuta cada 15 minutos
4. **Reintentos**: Backoff exponencial (1s, 2s, 4s... max 30s)
5. **Conflictos**: Resolución por timestamp del servidor

### Estados de Sincronización

- `PENDING`: Esperando sincronización
- `SYNCING`: En proceso de sincronización
- `SYNCED`: Sincronizado exitosamente
- `FAILED`: Error después de max reintentos
- `CONFLICT`: Conflicto detectado, requiere resolución
