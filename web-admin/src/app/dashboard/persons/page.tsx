"use client";

export const dynamic = "force-dynamic";

import { useEffect, useState } from "react";
import { collection, query, orderBy, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebase";

interface Person {
  id: string;
  fullName: string;
  rfc: string;
  biometricEnabled: boolean;
  status: string;
  createdAt: any;
  updatedAt: any;
  createdByDeviceId: string;
  lastUpdatedByDeviceId: string;
}

interface EnrollmentEvent {
  id: string;
  personId: string;
  rfc: string;
  fullNameSnapshot: string;
  deviceId: string;
  eventType: string;
  authResult: string;
  timestamp: any;
  appVersion: string;
}

export default function PersonsPage() {
  const [persons, setPersons] = useState<Person[]>([]);
  const [events, setEvents] = useState<EnrollmentEvent[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState("");
  const [selectedPerson, setSelectedPerson] = useState<Person | null>(null);

  useEffect(() => {
    // Load persons
    const personsQuery = query(
      collection(db, "organizations", "default", "persons"),
      orderBy("fullName", "asc")
    );

    const unsubscribePersons = onSnapshot(personsQuery, (snapshot) => {
      const data = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      })) as Person[];
      setPersons(data);
      setLoading(false);
    });

    // Load enrollment events
    const eventsQuery = query(
      collection(db, "organizations", "default", "enrollment_events"),
      orderBy("timestamp", "desc")
    );

    const unsubscribeEvents = onSnapshot(eventsQuery, (snapshot) => {
      const data = snapshot.docs.map((doc) => ({
        id: doc.id,
        ...doc.data(),
      })) as EnrollmentEvent[];
      setEvents(data);
    });

    return () => {
      unsubscribePersons();
      unsubscribeEvents();
    };
  }, []);

  const filteredPersons = persons.filter(
    (p) =>
      p.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.rfc.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const getPersonEvents = (personId: string) => {
    return events.filter((e) => e.personId === personId);
  };

  const formatDate = (timestamp: any) => {
    if (!timestamp) return "-";
    const date = timestamp.toDate ? timestamp.toDate() : new Date(timestamp);
    return date.toLocaleString("es-MX");
  };

  return (
    <div>
      <h1 className="text-2xl font-bold mb-6">Personas Registradas</h1>

      {/* Search */}
      <div className="bg-white p-4 rounded-lg shadow mb-6">
        <input
          type="text"
          placeholder="Buscar por nombre o RFC..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full border rounded px-3 py-2"
        />
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
        <div className="bg-white p-4 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Total Personas</h3>
          <p className="text-2xl font-bold">{persons.length}</p>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Biométricamente Habilitadas</h3>
          <p className="text-2xl font-bold text-green-600">
            {persons.filter((p) => p.biometricEnabled).length}
          </p>
        </div>
        <div className="bg-white p-4 rounded-lg shadow">
          <h3 className="text-gray-500 text-sm">Total Registros</h3>
          <p className="text-2xl font-bold">{events.length}</p>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Persons list */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="p-4 border-b">
            <h2 className="font-semibold">Personas</h2>
          </div>
          <div className="max-h-96 overflow-y-auto">
            {loading ? (
              <div className="p-4 text-center text-gray-500">Cargando...</div>
            ) : filteredPersons.length === 0 ? (
              <div className="p-4 text-center text-gray-500">
                No se encontraron personas
              </div>
            ) : (
              filteredPersons.map((person) => (
                <div
                  key={person.id}
                  className={`p-4 border-b cursor-pointer hover:bg-gray-50 ${
                    selectedPerson?.id === person.id ? "bg-blue-50" : ""
                  }`}
                  onClick={() => setSelectedPerson(person)}
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="font-medium">{person.fullName}</h3>
                      <p className="text-sm text-gray-500">RFC: {person.rfc}</p>
                      <p className="text-sm text-gray-500">
                        Registrado: {formatDate(person.createdAt)}
                      </p>
                    </div>
                    <span
                      className={`px-2 py-1 text-xs rounded-full ${
                        person.biometricEnabled
                          ? "bg-green-100 text-green-800"
                          : "bg-gray-100 text-gray-800"
                      }`}
                    >
                      {person.biometricEnabled ? "Biométrico" : "Pendiente"}
                    </span>
                  </div>
                </div>
              ))
            )}
          </div>
        </div>

        {/* Person details / Events */}
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <div className="p-4 border-b">
            <h2 className="font-semibold">
              {selectedPerson
                ? `Detalle: ${selectedPerson.fullName}`
                : "Selecciona una persona"}
            </h2>
          </div>
          <div className="max-h-96 overflow-y-auto">
            {selectedPerson ? (
              <div className="p-4">
                <div className="space-y-2 mb-4">
                  <p>
                    <span className="font-medium">RFC:</span> {selectedPerson.rfc}
                  </p>
                  <p>
                    <span className="font-medium">Estado:</span>{" "}
                    {selectedPerson.status}
                  </p>
                  <p>
                    <span className="font-medium">Biométrico:</span>{" "}
                    {selectedPerson.biometricEnabled ? "Habilitado" : "Deshabilitado"}
                  </p>
                  <p>
                    <span className="font-medium">Creado por:</span>{" "}
                    {selectedPerson.createdByDeviceId}
                  </p>
                  <p>
                    <span className="font-medium">Último dispositivo:</span>{" "}
                    {selectedPerson.lastUpdatedByDeviceId}
                  </p>
                </div>

                <h3 className="font-medium mb-2">Historial de Registros</h3>
                <div className="space-y-2">
                  {getPersonEvents(selectedPerson.id).length === 0 ? (
                    <p className="text-sm text-gray-500">Sin registros</p>
                  ) : (
                    getPersonEvents(selectedPerson.id).map((event) => (
                      <div
                        key={event.id}
                        className="p-2 bg-gray-50 rounded text-sm"
                      >
                        <p>
                          <span className="font-medium">Tipo:</span>{" "}
                          {event.eventType}
                        </p>
                        <p>
                          <span className="font-medium">Dispositivo:</span>{" "}
                          {event.deviceId}
                        </p>
                        <p>
                          <span className="font-medium">Fecha:</span>{" "}
                          {formatDate(event.timestamp)}
                        </p>
                        <p>
                          <span className="font-medium">Resultado:</span>{" "}
                          {event.authResult}
                        </p>
                      </div>
                    ))
                  )}
                </div>
              </div>
            ) : (
              <div className="p-4 text-center text-gray-500">
                Selecciona una persona para ver detalles
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
