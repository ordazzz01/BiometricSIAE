"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/hooks/useAuth";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { user, loading, logout } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && !user) {
      router.push("/auth/login");
    }
  }, [user, loading, router]);

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <p>Cargando...</p>
      </div>
    );
  }

  if (!user) {
    return null;
  }

  return (
    <div className="min-h-screen flex">
      {/* Sidebar */}
      <aside className="w-64 bg-gray-900 text-white">
        <div className="p-4">
          <h1 className="text-xl font-bold">Checador Biométrico</h1>
          <p className="text-sm text-gray-400">Panel Administrativo</p>
        </div>
        <nav className="mt-4">
          <Link
            href="/dashboard"
            className="block px-4 py-2 hover:bg-gray-800"
          >
            Dashboard
          </Link>
          <Link
            href="/dashboard/attendance"
            className="block px-4 py-2 hover:bg-gray-800"
          >
            Asistencias
          </Link>
          <Link
            href="/dashboard/employees"
            className="block px-4 py-2 hover:bg-gray-800"
          >
            Empleados
          </Link>
          <Link
            href="/dashboard/devices"
            className="block px-4 py-2 hover:bg-gray-800"
          >
            Dispositivos
          </Link>
          <Link
            href="/dashboard/incidents"
            className="block px-4 py-2 hover:bg-gray-800"
          >
            Incidencias
          </Link>
          <Link
            href="/dashboard/evidence"
            className="block px-4 py-2 hover:bg-gray-800"
          >
            Evidencias
          </Link>
          <Link
            href="/dashboard/branches"
            className="block px-4 py-2 hover:bg-gray-800"
          >
            Sucursales
          </Link>
        </nav>
        <div className="absolute bottom-0 w-64 p-4">
          <p className="text-sm text-gray-400 mb-2">{user.email}</p>
          <button
            onClick={() => logout()}
            className="w-full py-2 px-4 bg-red-600 hover:bg-red-700 rounded text-sm"
          >
            Cerrar Sesión
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 p-6">{children}</main>
    </div>
  );
}
