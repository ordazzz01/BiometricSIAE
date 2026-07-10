"use client";

export const dynamic = "force-dynamic";

import { useEffect, useState } from "react";
import { collection, query, orderBy, onSnapshot, updateDoc, doc } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { Employee } from "@/types";

export default function FingerprintsPage() {
  const [employees, setEmployees] = useState<Employee[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [filterDepartment, setFilterDepartment] = useState("");
  const [filterShift, setFilterShift] = useState("");

  useEffect(() => {
    const q = query(
      collection(db, "organizations", "default", "employees"),
      orderBy("name", "asc")
    );

    const unsubscribe = onSnapshot(q, (snapshot) => {
      const data = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      })) as Employee[];
      setEmployees(data);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const filteredEmployees = employees.filter((emp) => {
    const matchesSearch =
      emp.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (emp.rfc && emp.rfc.toLowerCase().includes(searchQuery.toLowerCase()));
    const matchesDepartment = !filterDepartment || emp.department === filterDepartment;
    const matchesShift = !filterShift || emp.shift === filterShift;
    return matchesSearch && matchesDepartment && matchesShift;
  });

  const departments = [...new Set(employees.map((e) => e.department).filter(Boolean))];
  const shifts = [...new Set(employees.map((e) => e.shift).filter(Boolean))];

  const handleAssignFingerprint = async (employeeId: string) => {
    const fingerprintId = `fp_${Date.now()}`;
    try {
      await updateDoc(
        doc(db, "organizations", "default", "employees", employeeId),
        {
          fingerprintId: fingerprintId,
          biometricEnrolled: true,
          updatedAt: new Date().toISOString(),
        }
      );
    } catch (error) {
      console.error("Error assigning fingerprint:", error);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Asignación de Huellas</h1>

      {/* Filters */}
      <div className="bg-white p-4 rounded-lg shadow mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <input
            type="text"
            placeholder="Buscar por nombre o RFC..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="border rounded px-3 py-2"
          />
          <select
            value={filterDepartment}
            onChange={(e) => setFilterDepartment(e.target.value)}
            className="border rounded px-3 py-2"
          >
            <option value="">Todos los departamentos</option>
            {departments.map((dept) => (
              <option key={dept} value={dept}>
                {dept}
              </option>
            ))}
          </select>
          <select
            value={filterShift}
            onChange={(e) => setFilterShift(e.target.value)}
            className="border rounded px-3 py-2"
          >
            <option value="">Todos los turnos</option>
            {shifts.map((shift) => (
              <option key={shift} value={shift}>
                {shift}
              </option>
            ))}
          </select>
        </div>
      </div>

      {/* Employee list */}
      <div className="space-y-4">
        {loading ? (
          <div className="text-center py-8 text-gray-500">Cargando...</div>
        ) : filteredEmployees.length === 0 ? (
          <div className="text-center py-8 text-gray-500">
            No se encontraron empleados
          </div>
        ) : (
          filteredEmployees.map((employee) => (
            <div
              key={employee.id}
              className="bg-white p-4 rounded-lg shadow flex items-center justify-between"
            >
              <div className="flex-1">
                <h3 className="font-semibold">{employee.name}</h3>
                <p className="text-sm text-gray-500">
                  RFC: {employee.rfc || "Sin RFC"}
                </p>
                <p className="text-sm text-gray-500">
                  Depto: {employee.department || "-"} | Turno:{" "}
                  {employee.shift || "-"}
                </p>
              </div>

              <div className="flex items-center gap-4">
                {employee.fingerprintId ? (
                  <span className="px-3 py-1 bg-green-100 text-green-800 rounded-full text-sm font-medium">
                    ✅ Huella: {employee.fingerprintId}
                  </span>
                ) : (
                  <button
                    onClick={() => handleAssignFingerprint(employee.id)}
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm"
                  >
                    Asignar Huella
                  </button>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
