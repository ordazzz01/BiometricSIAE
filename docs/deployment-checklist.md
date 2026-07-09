# Checklist de Despliegue

## Pre-despliegue

### Android

- [ ] google-services.json configurado
- [ ] Keystore de release generado
- [ ] ProGuard rules optimizados
- [ ] Versión de app incrementada
- [ ] Permissions manifest correctos
- [ ] Network security config actualizado
- [ ] URL de API configurada

### Web Admin

- [ ] Variables de entorno configuradas
- [ ] Firebase Auth dominios autorizados
- [ ] Build exitoso localmente
- [ ] TypeScript sin errores
- [ ] ESLint sin warnings críticos

### Firebase

- [ ] Firestore rules desplegadas
- [ ] Storage rules desplegadas
- [ ] Cloud Functions desplegadas
- [ ] Índices creados
- [ ] Authentication habilitado

### Backend

- [ ] API desplegada en Cloud Run
- [ ] Endpoints documentados
- [ ] CORS configurado
- [ ] Rate limiting habilitado
- [ ] Monitoreo activo

## Despliegue

### Android

```bash
# Build release
cd android
./gradlew assembleRelease

# Verificar APK
ls -la app/build/outputs/apk/release/

# Subir a Google Play Console
# O distribuir directamente
```

### Web Admin (Vercel)

```bash
# Despliegue automático desde GitHub
git push origin main

# O despliegue manual
cd web-admin
vercel --prod
```

### Firebase

```bash
# Desplegar todo
firebase deploy

# O por componentes
firebase deploy --only firestore:rules
firebase deploy --only storage
firebase deploy --only functions
```

## Post-despliegue

### Verificación

- [ ] Android: App instala y ejecuta correctamente
- [ ] Android: Login funciona
- [ ] Android: Checada biométrica funciona
- [ ] Android: Sync offline funciona
- [ ] Web: Login funciona
- [ ] Web: Dashboard muestra métricas
- [ ] Web: Tabla de asistencias carga
- [ ] Firebase: Reglas aplicadas correctamente
- [ ] API: Endpoints responden correctamente

### Monitoreo

- [ ] Crashlytics recibiendo errores
- [ ] Analytics registrando eventos
- [ ] Firebase logs revisados
- [ ] Vercel logs revisados
- [ ] Cloud Run logs revisados

### Documentación

- [ ] README actualizado
- [ ] API docs actualizados
- [ ] CHANGELOG actualizado
- [ ] Release notes creados

## Rollback

### Android

1. Mantener versión anterior en Google Play
2. Usar rollbacks de APK si es necesario

### Web Admin

1. Vercel permite rollback a versiones anteriores
2. Seleccionar versión en dashboard de Vercel

### Firebase

1. Mantener backup de rules anteriores
2. Rollback manual de Cloud Functions si es necesario

## Post-mortem

Si hay issues después del despliegue:

1. Documentar el problema
2. Identificar causa raíz
3. Implementar fix
4. Actualizar checklist
5. Compartir learnings
