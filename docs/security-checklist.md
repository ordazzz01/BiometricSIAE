# Checklist de Seguridad

## Android

### Almacenamiento Seguro

- [ ] Tokens almacenados en Android Keystore
- [ ] Datos sensibles cifrados con EncryptedSharedPreferences
- [ ] No se almacenan huellas biométricas en la app
- [ ] PINs hasheados con SHA-256

### Red

- [ ] Certificate Pinning habilitado
- [ ] TLS 1.2+ requerido
- [ ] No se permite tráfico HTTP (cleartext)
- [ ] Validación de certificados

### Autenticación

- [ ] BiometricPrompt para autenticación biométrica
- [ ] Refresh token automático
- [ ] Sesión expira después de inactividad
- [ ] Rate limiting en intentos de login

### Detección de Manipulación

- [ ] Root detection habilitado
- [ ] Verificación de integridad del APK
- [ ] Detección de emulador
- [ ] Bloqueo en dispositivos comprometidos

### Logging

- [ ] Datos sensibles redactados en logs
- [ ] No se loguean tokens ni contraseñas
- [ ] Logs estructurados para debugging
- [ ] Crashlytics configurado

## Firebase

### Firestore Rules

- [ ] Acceso por tenant verificado
- [ ] Solo dispositivos autenticados pueden crear registros
- [ ] Solo admins pueden modificar configuración
- [ ] No se permite eliminación de registros

### Storage Rules

- [ ] Solo imágenes permitidas
- [ ] Tamaño máximo: 5MB
- [ ] Acceso por tenant
- [ ] No se permite eliminación por dispositivos

### Authentication

- [ ] Email/Password habilitado
- [ ] Custom claims configurados
- [ ] Session timeout configurado
- [ ] Multi-factor auth opcional

## API Backend

### Autenticación

- [ ] JWT validation en cada endpoint
- [ ] Token refresh automático
- [ ] Rate limiting implementado
- [ ] IP blocking para abuso

### Validación

- [ ] Input validation en todos los endpoints
- [ ] SQL injection prevention
- [ ] XSS prevention
- [ ] CSRF protection

### Auditoría

- [ ] Logs de cada operación
- [ ] Timestamps en registros
- [ ] Device ID en cada request
- [ ] Hash de integridad

## Panel Web

### Autenticación

- [ ] Firebase Auth configurado
- [ ] Sesión segura con cookies
- [ ] CSRF protection
- [ ] Rate limiting

### Datos

- [ ] Solo datos del tenant visible
- [ ] No se exponen datos sensibles
- [ ] Exportación CSV con permisos
- [ ] Logs de acceso

## Infraestructura

### Vercel

- [ ] Variables de entorno configuradas
- [ ] Dominio SSL habilitado
- [ ] Headers de seguridad configurados
- [ ] Rate limiting en API routes

### GitHub Actions

- [ ] Secrets en GitHub (no hardcodeados)
- [ ] Permisos mínimos necesarios
- [ ] Build reproducible
- [ ] Artifact signing opcional

## Datos

### Cifrado

- [ ] Datos en tránsito: TLS 1.2+
- [ ] Datos en reposo: Firebase encryption
- [ ] Backups cifrados
- [ ] Key rotation programado

### Retención

- [ ] Política de retención de datos
- [ ] Eliminación de datos antiguos
- [ ] Backup automático
- [ ] Recovery plan documentado

## Monitoreo

### Alertas

- [ ] Errores críticos notificados
- [ ] Fallos de sync notificados
- [ ] Dispositivos offline detectados
- [ ] Anomalías de uso detectadas

### Métricas

- [ ] Tasa de éxito de checadas
- [ ] Tiempo de respuesta API
- [ ] Uso de storage
- [ ] Errores por tipo
