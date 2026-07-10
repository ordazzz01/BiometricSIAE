# MobileFaceNet Model

Este directorio debe contener el archivo `mobile_face_net.tflite`.

## Descargar el modelo

Opción 1: Desde el repositorio de referencia
```bash
# Descargar desde GitHub
wget https://github.com/shubham0204/OnDevice-Face-Recognition-Android/raw/master/app/src/main/assets/mobile_face_net.tflite
```

Opción 2: Desde TensorFlow Hub
```bash
# Buscar "MobileFaceNet" en TensorFlow Hub
# Descargar versión TFLite
```

## Especificaciones del modelo

- **Input**: [1, 112, 112, 3] - Imagen RGB normalizada
- **Output**: [1, 192] - Vector de embedding
- **Tamaño**: ~1MB
- **Precisión**: ~99% en LFW (Labeled Faces in the Wild)

## Uso

El modelo se carga automáticamente al iniciar la app.
El archivo debe estar en: `app/src/main/assets/mobile_face_net.tflite`
