# Descargar Modelo MobileFaceNet

## Opción 1: Desde el repositorio original

El modelo `mobile_face_net.tflite` NO está incluido en el proyecto descargado.
Necesitas descargarlo por separado.

### Desde TensorFlow Model Zoo:

```bash
# Descargar MobileFaceNet
wget https://storage.googleapis.com/tfhub-app-models/mediapipe%2Fface%2Fmobilenet%2Fface_detection%2F1%2Fmobile_face_net.tflite

# O desde el repositorio de deepface:
# https://github.com/serengil/deepface
```

### Generar desde Python:

```python
from deepface import DeepFace
from deepface.models.facial_recognition.Facenet import scaling
import tensorflow as tf

model = DeepFace.build_model("Facenet")
model.model.save("facenet.keras")

model = tf.keras.models.load_model("facenet.keras", custom_objects={
    "scaling": scaling
})

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.optimizations = [tf.lite.Optimize.DEFAULT]
tflite_model = converter.convert()

with open("mobile_face_net.tflite", "wb") as f:
    f.write(tflite_model)
```

## Ubicación

El archivo debe estar en:
```
android/app/src/main/assets/mobile_face_net.tflite
```

## Verificar

Después de colocar el archivo, verifica que exista:
```bash
ls -la android/app/src/main/assets/mobile_face_net.tflite
```

El tamaño debería ser ~1MB.
