"use client";

export const dynamic = "force-dynamic";

import { useEffect, useState } from "react";
import { collection, query, onSnapshot, where, getDocs } from "firebase/firestore";
import { db } from "@/lib/firebase";

export default function DashboardPage() {
  const [totalCheckins, setTotalCheckins] = useState(0);
  const [totalEmployees, setTotalEmployees] = useState(0);
  const [totalDevices, setTotalDevices] = useState(0);
  const [pendingIncidents, setPendingIncidents] = useState(0);
  const [checkinsToday, setCheckinsToday] = useState(0);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        // Get total employees
        const employeesSnap = await getDocs(
          collection(db, "organizations", "default", "employees")
        );
        setTotalEmployees(employeesSnap.size);

        // Get total devices
        const devicesSnap = await getDocs(
          collection(db, "organizations", "default", "devices")
        );
        setTotalDevices(devicesSnap.size);

        // Get total attendance records
        const attendanceSnap = await getDocs(
          collection(db, "organizations", "default", "attendance_records")
        );
        setTotalCheckins(attendanceSnap.size);

        // Get today's checkins
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        const todayQuery = query(
          collection(db, "organizations", "default", "attendance_records"),
          where("timestamp", ">=", today.toISOString())
        );
        const todaySnap = await getDocs(todayQuery);
        setCheckinsToday(todaySnap.size);

        // Get pending incidents
        const incidentsQuery = query(
          collection(db, "organizations", "default", "incidents"),
          where("status", "==", "PENDING")
        );
        const incidentsSnap = await getDocs(incidentsQuery);
        setPendingIncidents(incidentsSnap.size);
      } catch (error) {
        console.error("Error fetching data:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
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
          <p className="text-3xl font-bold text-green-600">{checkinsToday}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Total Registros</h3>
          <p className="text-3xl font-bold text-blue-600">{totalCheckins}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Empleados</h3>
          <p className="text-3xl font-bold text-purple-600">{totalEmployees}</p>
        </div>
        <div className="bg-white p-6 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Dispositivos</h3>
          <p className="text-3xl font-bold text-orange-600">{totalDevices}</p>
        </div>
      </div>

      <div className="bg-white p-6 rounded-lg shadow">
        <h2 className="text-lg font-semibold mb-4">Resumen</h2>
        <div className="grid grid-cols-2 gap-4">
          <div className="text-center p-4 bg-gray-50 rounded">
            <p className="text-2xl font-bold text-green-600">{checkinsToday}</p>
            <p className="text-sm text-gray-500">Checadas registradas hoy</p>
          </div>
          <div className="text-center p-4 bg-gray-50 rounded">
            <p className="text-2xl font-bold text-orange-600">{pendingIncidents}</p>
            <p className="text-sm text-gray-500">Incidencias pendientes</p>
          </div>
        </div>
      </div>
    </div>
  );
}
