"use client";

export const dynamic = "force-dynamic";

import { useState } from "react";

export default function SettingsPage() {
  const [settings, setSettings] = useState({
    duplicateWindowMinutes: 5,
    faceRequired: false,
    geofenceEnabled: false,
    kioskMode: true,
    locationRequired: false,
    maxRetries: 5,
    lockoutAfterFailedAttempts: 3,
  });

  const handleSave = () => {
    // Save settings to Firestore or API
    console.log("Saving settings:", settings);
    alert("Configuración guardada");
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Configuración</h1>
      
      <div className="bg-white p-6 rounded-lg shadow max-w-2xl">
        <h2 className="text-lg font-semibold mb-4">Políticas del Dispositivo</h2>
        
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Ventana de duplicados (minutos)
            </label>
            <input
              type="number"
              value={settings.duplicateWindowMinutes}
              onChange={(e) =>
                setSettings({
                  ...settings,
                  duplicateWindowMinutes: parseInt(e.target.value),
                })
              }
              className="w-full border rounded px-3 py-2"
              min="1"
              max="30"
            />
            <p className="text-sm text-gray-500 mt-1">
              Tiempo mínimo entre registros del mismo tipo para evitar duplicados
            </p>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Intentos máximos antes de bloqueo
            </label>
            <input
              type="number"
              value={settings.lockoutAfterFailedAttempts}
              onChange={(e) =>
                setSettings({
                  ...settings,
                  lockoutAfterFailedAttempts: parseInt(e.target.value),
                })
              }
              className="w-full border rounded px-3 py-2"
              min="1"
              max="10"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Reintentos máximos de sincronización
            </label>
            <input
              type="number"
              value={settings.maxRetries}
              onChange={(e) =>
                setSettings({
                  ...settings,
                  maxRetries: parseInt(e.target.value),
                })
              }
              className="w-full border rounded px-3 py-2"
              min="1"
              max="20"
            />
          </div>

          <div className="border-t pt-4 mt-4">
            <h3 className="text-md font-medium text-gray-700 mb-3">Habilitar funcionalidades</h3>
            
            <div className="space-y-3">
              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={settings.faceRequired}
                  onChange={(e) =>
                    setSettings({
                      ...settings,
                      faceRequired: e.target.checked,
                    })
                  }
                  className="mr-2"
                />
                <span className="text-sm">Requerir evidencia facial</span>
              </label>

              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={settings.geofenceEnabled}
                  onChange={(e) =>
                    setSettings({
                      ...settings,
                      geofenceEnabled: e.target.checked,
                    })
                  }
                  className="mr-2"
                />
                <span className="text-sm">Habilitar geocerca</span>
              </label>

              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={settings.kioskMode}
                  onChange={(e) =>
                    setSettings({
                      ...settings,
                      kioskMode: e.target.checked,
                    })
                  }
                  className="mr-2"
                />
                <span className="text-sm">Modo kiosk (pantalla completa)</span>
              </label>

              <label className="flex items-center">
                <input
                  type="checkbox"
                  checked={settings.locationRequired}
                  onChange={(e) =>
                    setSettings({
                      ...settings,
                      locationRequired: e.target.checked,
                    })
                  }
                  className="mr-2"
                />
                <span className="text-sm">Requerir ubicación</span>
              </label>
            </div>
          </div>

          <div className="pt-4">
            <button
              onClick={handleSave}
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700"
            >
              Guardar Configuración
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
