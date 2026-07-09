# Guía de Despliegue en Vercel

## Prerrequisitos

1. Cuenta de Vercel
2. Repositorio en GitHub
3. Proyecto Firebase configurado

## 1. Importar Repositorio

1. Ve a [vercel.com/new](https://vercel.com/new)
2. Importa el repositorio de GitHub
3. Selecciona el directorio: `web-admin`
4. Framework: Next.js (detectado automáticamente)

## 2. Configurar Variables de Entorno

En el dashboard de Vercel, ve a Settings → Environment Variables:

### Variables requeridas

```
NEXT_PUBLIC_FIREBASE_API_KEY
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN
NEXT_PUBLIC_FIREBASE_PROJECT_ID
NEXT_PUBLIC_FIREBASE_STORAGE_BUCKET
NEXT_PUBLIC_FIREBASE_MESSAGING_SENDER_ID
NEXT_PUBLIC_FIREBASE_APP_ID
FIREBASE_CLIENT_EMAIL
FIREBASE_PRIVATE_KEY
NEXT_PUBLIC_API_BASE_URL
```

### Configurar por entorno

- **Production**: Variables para producción
- **Preview**: Variables para previews de PR
- **Development**: Variables para desarrollo local

## 3. Configurar Build

### Configuración en Vercel

- **Root Directory**: `web-admin`
- **Build Command**: `npm run build`
- **Output Directory**: `.next`
- **Install Command**: `npm ci`

## 4. Deploy

### Deploy automático

- Cada push a `main` despliega a producción
- Cada PR genera un deploy de preview

### Deploy manual

```bash
cd web-admin
vercel --prod
```

## 5. Dominio Personalizado

1. Ve a Settings → Domains
2. Agrega tu dominio
3. Configura DNS según las instrucciones de Vercel

## 6. Variables de Entorno por Entorno

### Producción

```
NEXT_PUBLIC_FIREBASE_API_KEY=prod_api_key
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=biometricsiae.firebaseapp.com
NEXT_PUBLIC_FIREBASE_PROJECT_ID=biometricsiae
```

### Preview (PRs)

```
NEXT_PUBLIC_FIREBASE_API_KEY=preview_api_key
NEXT_PUBLIC_FIREBASE_AUTH_DOMAIN=biometricsiae.firebaseapp.com
NEXT_PUBLIC_FIREBASE_PROJECT_ID=biometricsiae
```

## 7. Monitoreo

### Vercel Analytics

1. Ve a Analytics en el dashboard
2. Habilita Web Analytics
3. Monitorea métricas de rendimiento

### Logs

1. Ve a Logs en el dashboard
2. Revisa Function Logs para errores
3. Monitorea rendimiento de API routes

## 8. Troubleshooting

### Build falla

- Verifica que las dependencias estén en `package.json`
- Revisa los logs de build en Vercel
- Asegúrate de que TypeScript compila sin errores

### Variables no disponibles

- Verifica que estén configuradas en el entorno correcto
- Los prefijos `NEXT_PUBLIC_` son para cliente
- Las sin prefijo son para servidor/API routes

### Firebase Auth no funciona

- Verifica que el dominio de Vercel esté en Authorized Domains de Firebase
- Ve a Firebase Console → Authentication → Settings → Authorized domains
