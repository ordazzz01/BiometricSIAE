# Checador Biométrico de Asistencias v1

Sistema completo de registro de asistencia biométrica para empresas e instituciones.

## Características Principales

- **Autenticación Biométrica**: Huella dactilar o reconocimiento facial mediante Android BiometricPrompt
- **Evidencia Facial**: Captura opcional con CameraX y ML Kit para validación de presencia
- **Modo Offline**: Funcionamiento sin internet con sincronización posterior
- **Multi-sucursal**: Soporte para múltiples ubicaciones
- **Panel Administrativo**: Dashboard web en Next.js para monitoreo en tiempo real
- **Firebase Integration**: Auth, Firestore, Storage, FCM, Crashlytics

## Arquitectura

```
BiometricSIAE/
├── android/          # App Android (Kotlin + Jetpack Compose)
├── web-admin/        # Panel Admin (Next.js + Tailwind)
├── firebase/         # Configuración Firebase
├── docs/             # Documentación
├── scripts/          # Scripts de utilidad
└── .github/          # CI/CD workflows
```

## Stack Tecnológico

### Android
- Kotlin 2.0+
- Jetpack Compose
- Hilt (DI)
- Room (offline)
- Retrofit (network)
- CameraX + ML Kit
- WorkManager (sync)
- Firebase SDKs

### Web Admin
- Next.js 14
- TypeScript
- Tailwind CSS
- Firebase JS SDK
- Vercel (deploy)

### Backend
- Node.js/Express
- Google Cloud Run
- Firestore

## Requisitos

### Android
- Android 7.0+ (API 24)
- Google Play Services
- Cuenta de Google

### Web Admin
- Node.js 20+
- npm o yarn
- Cuenta de Vercel

### Firebase
- Proyecto Firebase (BiometricSIAE)
- Firestore Database
- Storage
- Authentication

## Instalación

### 1. Clonar repositorio

```bash
git clone https://github.com/tu-usuario/BiometricSIAE.git
cd BiometricSIAE
```

### 2. Configurar Android

```bash
cd android
cp local.properties.example local.properties
# Editar local.properties con tu SDK path

# Abrir en Android Studio
# O build desde línea de comandos
./gradlew assembleDebug
```

### 3. Configurar Web Admin

```bash
cd web-admin
npm install
cp .env.local.example .env.local
# Editar .env.local con tus credenciales Firebase

npm run dev
```

### 4. Configurar Firebase

```bash
# Instalar Firebase CLI
npm install -g firebase-tools
firebase login

# Desplegar reglas y funciones
firebase deploy
```

## Estructura de Carpetas

### Android

```
app/src/main/java/com/siae/biometricsiae/
├── data/
│   ├── local/          # Room DB, DAOs, Entities
│   ├── remote/         # API, Firebase, DTOs
│   └── repository/     # Repository implementations
├── domain/
│   ├── model/          # Domain models
│   └── usecase/        # Use cases
├── feature/
│   ├── auth/           # Login
│   ├── checkin/        # Main checkin screen
│   ├── camera/         # Face capture
│   └── settings/       # App settings
├── sync/               # WorkManager sync
├── security/           # Keystore, Root detection
└── util/               # Utilities
```

### Web Admin

```
src/
├── app/
│   ├── (auth)/         # Login page
│   └── (dashboard)/    # Dashboard pages
├── components/         # Reusable components
├── hooks/              # Custom hooks
├── lib/                # Firebase config
└── types/              # TypeScript types
```

## Uso

### Modo Kiosk (Tablet fija)

1. Instalar app en tablet
2. Configurar como dispositivo kiosk
3. Seleccionar sucursal
4. Los empleados escanean huella o cara

### Modo Supervisor (Celular personal)

1. Login con credenciales de supervisor
2. Seleccionar sucursal
3. Registrar asistencia manual si es necesario
4. Revisar incidencias

### Panel Web

1. Acceder a la URL de Vercel
2. Login con credenciales de admin
3. Dashboard muestra métricas en tiempo real
4. Exportar reportes a CSV

## Configuración

### Variables de Entorno Android

En `android/local.properties`:

```properties
API_BASE_URL=https://your-api.run.app
APP_ENV=dev
```

### Variables de Entorno Web

En `web-admin/.env.local`:

```env
NEXT_PUBLIC_FIREBASE_API_KEY=your_key
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=your_domain
NEXT_PUBLIC_FIREBASE_PROJECT_ID=your_project
```

## Seguridad

- Tokens cifrados en Android Keystore
- Certificate Pinning para API calls
- Root detection para dispositivos comprometidos
- Firestore Rules por tenant
- Storage Rules con validación de tipo y tamaño
- Secure Logger con redacción de datos sensibles

## Contribuir

1. Fork el repositorio
2. Crear branch feature (`git checkout -b feature/nueva-feature`)
3. Commit cambios (`git commit -m 'Agregar nueva feature'`)
4. Push al branch (`git push origin feature/nueva-feature`)
5. Abrir Pull Request

## Licencia

MIT License - Ver archivo [LICENSE](LICENSE)

## Soporte

- Documentación: [docs/](docs/)
- Issues: GitHub Issues
- Email: tu-email@empresa.com
