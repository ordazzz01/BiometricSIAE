#!/usr/bin/env python3
"""
Script para generar el modelo MobileFaceNet TFLite.
Ejecutar localmente antes de compilar la app Android.

Requisitos:
    pip install deepface tensorflow

Uso:
    python scripts/generate_face_model.py

El modelo se guardará en:
    android/app/src/main/assets/mobile_face_net.tflite
"""

import os
import sys

def main():
    print("=== Generador de Modelo MobileFaceNet ===")
    print()
    
    try:
        from deepface import DeepFace
        from deepface.models.facial_recognition.Facenet import scaling
        import tensorflow as tf
        print("✓ Dependencias instaladas")
    except ImportError as e:
        print(f"Error: {e}")
        print()
        print("Instala las dependencias:")
        print("  pip install deepface tensorflow")
        sys.exit(1)
    
    print()
    print("Construyendo modelo FaceNet...")
    
    try:
        # Construir modelo
        model = DeepFace.build_model("Facenet")
        print("✓ Modelo construido")
        
        # Guardar temporalmente
        temp_path = "facenet_temp.keras"
        model.model.save(temp_path)
        print(f"✓ Modelo guardado temporalmente: {temp_path}")
        
        # Cargar con custom objects
        model = tf.keras.models.load_model(temp_path, custom_objects={"scaling": scaling})
        print("✓ Modelo cargado con custom objects")
        
        # Convertir a TFLite
        print("Convirtiendo a TFLite (esto puede tardar 1-2 minutos)...")
        converter = tf.lite.TFLiteConverter.from_keras_model(model)
        converter.optimizations = [tf.lite.Optimize.DEFAULT]
        tflite_model = converter.convert()
        print("✓ Conversión completada")
        
        # Guardar modelo
        output_path = "android/app/src/main/assets/mobile_face_net.tflite"
        with open(output_path, "wb") as f:
            f.write(tflite_model)
        
        # Verificar tamaño
        size_mb = os.path.getsize(output_path) / (1024 * 1024)
        print(f"✓ Modelo guardado: {output_path}")
        print(f"  Tamaño: {size_mb:.2f} MB")
        
        # Limpiar archivo temporal
        os.remove(temp_path)
        
        print()
        print("=== ¡Modelo generado exitosamente! ===")
        print()
        print("Ahora puedes compilar la app Android:")
        print("  cd android")
        print("  ./gradlew.bat assembleDebug")
        
    except Exception as e:
        print(f"Error al generar modelo: {e}")
        print()
        print("Si el error persiste, intenta:")
        print("  pip install --upgrade deepface tensorflow")
        sys.exit(1)

if __name__ == "__main__":
    main()
