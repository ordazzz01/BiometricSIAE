"use client";

import { useEffect, useState } from "react";
import { collection, query, orderBy, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { Device } from "@/types";

interface UseDevicesProps {
  orgId: string;
}

export function useDevices({ orgId }: UseDevicesProps) {
  const [devices, setDevices] = useState<Device[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!orgId) return;

    setLoading(true);
    setError(null);

    const q = query(
      collection(db, "organizations", orgId, "devices"),
      orderBy("name", "asc")
    );

    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const data = snapshot.docs.map((doc) => ({
          id: doc.id,
          ...doc.data(),
        })) as Device[];
        setDevices(data);
        setLoading(false);
      },
      (err) => {
        setError(err.message);
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, [orgId]);

  const isDeviceOnline = (device: Device) => {
    if (!device.lastSyncAt) return false;
    const lastSync = new Date(device.lastSyncAt);
    const now = new Date();
    const diffMinutes = (now.getTime() - lastSync.getTime()) / (1000 * 60);
    return diffMinutes < 5; // Online if synced in last 5 minutes
  };

  return { devices, loading, error, isDeviceOnline };
}
