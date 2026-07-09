"use client";

export const dynamic = "force-dynamic";

import { useEffect, useState } from "react";
import { collection, query, orderBy, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { Branch } from "@/types";

export default function BranchesPage() {
  const [branches, setBranches] = useState<Branch[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const q = query(
      collection(db, "organizations", "default", "branches"),
      orderBy("name", "asc")
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const data = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      })) as Branch[];
      setBranches(data);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Sucursales</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {loading ? (
          <div className="col-span-full text-center text-gray-500 py-8">
            Cargando sucursales...
          </div>
        ) : branches.length === 0 ? (
          <div className="col-span-full text-center text-gray-500 py-8">
            No hay sucursales registradas
          </div>
        ) : (
          branches.map((branch) => (
            <div
              key={branch.id}
              className="bg-white p-6 rounded-lg shadow"
            >
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-lg font-semibold">{branch.name}</h3>
                <span
                  className={`px-2 py-1 text-xs font-semibold rounded-full ${
                    branch.active
                      ? "bg-green-100 text-green-800"
                      : "bg-red-100 text-red-800"
                  }`}
                >
                  {branch.active ? "Activa" : "Inactiva"}
                </span>
              </div>
              
              <div className="space-y-2 text-sm">
                {branch.address && (
                  <p>
                    <span className="text-gray-500">Dirección:</span>{" "}
                    {branch.address}
                  </p>
                )}
                <p>
                  <span className="text-gray-500">Zona horaria:</span>{" "}
                  {branch.timezone}
                </p>
                {branch.latitude && branch.longitude && (
                  <p>
                    <span className="text-gray-500">Coordenadas:</span>{" "}
                    {branch.latitude.toFixed(4)}, {branch.longitude.toFixed(4)}
                  </p>
                )}
                {branch.geofenceRadiusMeters && (
                  <p>
                    <span className="text-gray-500">Geocerca:</span>{" "}
                    {branch.geofenceRadiusMeters}m
                  </p>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
