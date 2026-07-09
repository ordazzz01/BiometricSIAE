"use client";

import { useEffect, useState } from "react";
import { collection, query, orderBy, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { Device } from "@/types";

export default function DevicesPage() {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const q = query(
      collection(db, "organizations", "default", "devices"),
      orderBy("name", "asc")
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const data = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      })) as Device[];
      setDevices(data);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const isDeviceOnline = (device: Device) => {
    if (!device.lastSyncAt) return false;
    const lastSync = new Date(device.lastSyncAt);
    const now = new Date();
    const diffMinutes = (now.getTime() - lastSync.getTime()) / (1000 * 60);
    return diffMinutes < 5;
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dispositivos</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {loading ? (
          <div className="col-span-full text-center text-gray-500 py-8">
            Cargando dispositivos...
          </div>
        ) : devices.length === 0 ? (
          <div className="col-span-full text-center text-gray-500 py-8">
            No hay dispositivos registrados
          </div>
        ) : (
          devices.map((device) => (
            <div
              key={device.id}
              className="bg-white p-6 rounded-lg shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold">{device.name}</h3>
                <span
                  className={`px-2 py-1 text-xs font-semibold rounded-full ${
                    isDeviceOnline(device)
                      ? "bg-green-100 text-green-800"
                      : "bg-red-100 text-red-800"
                  }`}
                >
                  {isDeviceOnline(device) ? "En línea" : "Fuera de línea"}
                </span>
              </div>
              
              <div className="space-y-2 text-sm">
                <p>
                  <span className="text-gray-500">ID:</span>{" "}
                  <span className="font-mono">{device.id}</span>
                </p>
                <p>
                  <span className="text-gray-500">Tipo:</span> {device.type}
                </p>
                <p>
                  <span className="text-gray-500">Sucursal:</span>{" "}
                  {device.branchId}
                </p>
                <p>
                  <span className="text-gray-500">Última sync:</span>{" "}
                  {device.lastSyncAt
                    ? new Date(device.lastSyncAt).toLocaleString("es-MX")
                    : "Nunca"}
                </p>
              </div>
              
              {device.config && (
                <div className="mt-4 pt-4 border-t">
                  <p className="text-sm font-medium text-gray-700 mb-2">
                    Configuración:
                  </p>
                  <div className="flex flex-wrap gap-2">
                    {device.config.kioskMode && (
                      <span className="px-2 py-1 text-xs bg-blue-100 text-blue-800 rounded">
                        Kiosk
                      </span>
                    )}
                    {device.config.faceRequired && (
                      <span className="px-2 py-1 text-xs bg-purple-100 text-purple-800 rounded">
                        Facial
                      </span>
                    )}
                    {device.config.geofenceEnabled && (
                      <span className="px-2 py-1 text-xs bg-green-100 text-green-800 rounded">
                        Geocerca
                      </span>
                    )}
                  </div>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  );
}
