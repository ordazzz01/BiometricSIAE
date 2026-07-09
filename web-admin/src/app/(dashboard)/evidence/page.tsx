"use client";

import { useEffect, useState } from "react";
import { collection, query, orderBy, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { FaceEvidence } from "@/types";

export default function EvidencePage() {
  const [evidence, setEvidence] = useState<FaceEvidence[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const q = query(
      collection(db, "organizations", "default", "face_evidence"),
      orderBy("createdAt", "desc")
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const data = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      })) as FaceEvidence[];
      setEvidence(data);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Evidencias Faciales</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {loading ? (
          <div className="col-span-full text-center text-gray-500 py-8">
            Cargando evidencias...
          </div>
        ) : evidence.length === 0 ? (
          <div className="col-span-full text-center text-gray-500 py-8">
            No hay evidencias registradas
          </div>
        ) : (
          evidence.map((item) => (
            <div
              key={item.id}
              className="bg-white rounded-lg shadow overflow-hidden"
            >
              <div className="aspect-square bg-gray-100">
                {item.url ? (
                  <img
                    src={item.url}
                    alt="Evidencia facial"
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-gray-400">
                    Sin imagen
                  </div>
                )}
              </div>
              <div className="p-4">
                <div className="flex justify-between items-start mb-2">
                  <span className="text-sm font-medium text-gray-900">
                    {item.employeeId}
                  </span>
                  <span className="text-xs text-gray-500">
                    {new Date(item.createdAt).toLocaleString("es-MX")}
                  </span>
                </div>
                <div className="flex flex-wrap gap-2">
                  <span
                    className={`px-2 py-1 text-xs rounded ${
                      item.faceDetected
                        ? "bg-green-100 text-green-800"
                        : "bg-red-100 text-red-800"
                    }`}
                  >
                    Rostro: {item.faceDetected ? "Detectado" : "No detectado"}
                  </span>
                  <span
                    className={`px-2 py-1 text-xs rounded ${
                      item.faceCentered
                        ? "bg-green-100 text-green-800"
                        : "bg-yellow-100 text-yellow-800"
                    }`}
                  >
                    Centrado: {item.faceCentered ? "Sí" : "No"}
                  </span>
                  <span
                    className={`px-2 py-1 text-xs rounded ${
                      item.eyesVisible
                        ? "bg-green-100 text-green-800"
                        : "bg-red-100 text-red-800"
                    }`}
                  >
                    Ojos: {item.eyesVisible ? "Visibles" : "No visibles"}
                  </span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
