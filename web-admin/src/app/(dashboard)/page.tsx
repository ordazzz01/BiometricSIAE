"use client";

import { useEffect, useState } from "react";
import { collection, query, where, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { DashboardMetrics } from "@/types";

export default function DashboardPage() {
  const [metrics, setMetrics] = useState<DashboardMetrics>({
    totalCheckins: 0,
    checkinsByType: {
      ENTRY: 0,
      EXIT: 0,
      BREAK: 0,
      BREAK_RETURN: 0,
    },
    totalEmployees: 0,
    pendingIncidents: 0,
    totalDevices: 0,
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // This is a simplified version - in production, use Cloud Functions or API
    const fetchMetrics = async () => {
      try {
        // For demo purposes, using mock data
        // In production, fetch from Firestore or API
        setMetrics({
          totalCheckins: 47,
          checkinsByType: {
            ENTRY: 23,
            EXIT: 18,
            BREAK: 3,
            BREAK_RETURN: 3,
          },
          totalEmployees: 35,
          pendingIncidents: 2,
          totalDevices: 4,
        });
      } catch (error) {
        console.error("Error fetching metrics:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchMetrics();
  }, []);

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <p>Cargando métricas...</p>
      </div>
    );
  }

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Dashboard</h1>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Checadas Hoy</h3>
          <p className="text-3xl font-bold text-green-600">{metrics.totalCheckins}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Empleados Activos</h3>
          <p className="text-3xl font-bold text-blue-600">{metrics.totalEmployees}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Dispositivos</h3>
          <p className="text-3xl font-bold text-purple-600">{metrics.totalDevices}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Incidencias Pendientes</h3>
          <p className="text-3xl font-bold text-orange-600">{metrics.pendingIncidents}</p>
        </div>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <h2 className="text-lg font-semibold mb-4">Resumen de Checadas</h2>
        <div className="grid grid-cols-4 gap-4">
          <div className="text-center">
            <p className="text-2xl font-bold text-green-600">{metrics.checkinsByType.ENTRY}</p>
            <p className="text-sm text-gray-500">Entradas</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-red-600">{metrics.checkinsByType.EXIT}</p>
            <p className="text-sm text-gray-500">Salidas</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-yellow-600">{metrics.checkinsByType.BREAK}</p>
            <p className="text-sm text-gray-500">Descansos</p>
          </div>
          <div className="text-center">
            <p className="text-2xl font-bold text-blue-600">{metrics.checkinsByType.BREAK_RETURN}</p>
            <p className="text-sm text-gray-500">Regresos</p>
          </div>
        </div>
      </div>
    </div>
  );
}
