"use client";

export const dynamic = "force-dynamic";

import { useEffect, useState } from "react";
import { doc, getDoc, setDoc } from "firebase/firestore";
import { db } from "@/lib/firebase";

export default function SettingsPage() {
  const [clockOffset, setClockOffset] = useState(0);
  const [activationInterval, setActivationInterval] = useState(5);
  const [screenTimeout, setScreenTimeout] = useState(30);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    const loadSettings = async () => {
      try {
        const docRef = doc(db, "organizations", "default", "device_config", "main");
        const docSnap = await getDoc(docRef);

        if (docSnap.exists()) {
          const data = docSnap.data();
          setClockOffset(data.clockOffset || 0);
          setActivationInterval(data.activationInterval || 5);
          setScreenTimeout(data.screenTimeout || 30);
        }
      } catch (error) {
        console.error("Error loading settings:", error);
      } finally {
        setLoading(false);
      }
    };

    loadSettings();
  }, []);

  const handleSave = async () => {
    setSaving(true);
    setMessage("");

    try {
      await setDoc(
        doc(db, "organizations", "default", "device_config", "main"),
        {
          clockOffset: clockOffset,
          activationInterval: activationInterval,
          screenTimeout: screenTimeout,
          updatedAt: new Date().toISOString(),
        },
        { merge: true }
      );
      setMessage("Configuración guardada exitosamente");
    } catch (error) {
      setMessage("Error al guardar");
      console.error("Error saving settings:", error);
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p>Cargando configuración...</p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Configuración del Dispositivo</h1>

      <div className="bg-white p-6 rounded-lg shadow max-w-2xl">
        {/* Clock Offset */}
        <div className="mb-6">
          <h2 className="text-lg font-semibold mb-4">Reloj del Dispositivo</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Offset del reloj (segundos)
              </label>
              <input
                type="number"
                value={clockOffset}
                onChange={(e) => setClockOffset(parseInt(e.target.value) || 0)}
                className="w-full border rounded px-3 py-2"
                step="1"
              />
              <p className="text-sm text-gray-500 mt-1">
                Ajuste la diferencia entre el reloj del dispositivo y el servidor.
                Use valores positivos para adelantar, negativos para atrasar.
              </p>
            </div>
          </div>
        </div>

        <hr className="my-6" />

        {/* Activation Interval */}
        <div className="mb-6">
          <h2 className="text-lg font-semibold mb-4">Intervalo de Activación</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Intervalo (minutos)
              </label>
              <input
                type="number"
                value={activationInterval}
                onChange={(e) => setActivationInterval(parseInt(e.target.value) || 5)}
                className="w-full border rounded px-3 py-2"
                min="1"
                max="60"
              />
              <p className="text-sm text-gray-500 mt-1">
                Cada cuántos minutos se activa el sensor de huellas automáticamente.
              </p>
            </div>
          </div>
        </div>

        <hr className="my-6" />

        {/* Screen Timeout */}
        <div className="mb-6">
          <h2 className="text-lg font-semibold mb-4">Timeout de Pantalla</h2>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Tiempo de inactividad (segundos)
              </label>
              <input
                type="number"
                value={screenTimeout}
                onChange={(e) => setScreenTimeout(parseInt(e.target.value) || 30)}
                className="w-full border rounded px-3 py-2"
                min="10"
                max="300"
              />
              <p className="text-sm text-gray-500 mt-1">
                Después de este tiempo sin actividad, la pantalla se apagará.
              </p>
            </div>
          </div>
        </div>

        {/* Save button */}
        <div className="flex items-center gap-4">
          <button
            onClick={handleSave}
            disabled={saving}
            className="px-6 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50"
          >
            {saving ? "Guardando..." : "Guardar Configuración"}
          </button>

          {message && (
            <span
              className={`text-sm ${
                message.includes("Error") ? "text-red-600" : "text-green-600"
              }`}
            >
              {message}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}
