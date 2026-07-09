"use client";

import { useEffect, useState } from "react";
import {
  collection,
  query,
  where,
  orderBy,
  onSnapshot,
  Timestamp,
} from "firebase/firestore";
import { db } from "@/lib/firebase";
import { AttendanceRecord } from "@/types";

interface UseAttendanceProps {
  orgId: string;
  branchId?: string;
  startDate?: Date;
  endDate?: Date;
}

export function useAttendance({
  orgId,
  branchId,
  startDate,
  endDate,
}: UseAttendanceProps) {
  const [records, setRecords] = useState<AttendanceRecord[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!orgId) return;

    setLoading(true);
    setError(null);

    let q = query(
      collection(db, "organizations", orgId, "attendance"),
      orderBy("timestamp", "desc")
    );

    if (branchId) {
      q = query(q, where("branchId", "==", branchId));
    }

    if (startDate) {
      q = query(
        q,
        where("timestamp", ">=", startDate.toISOString())
      );
    }

    if (endDate) {
      q = query(
        q,
        where("timestamp", "<=", endDate.toISOString())
      );
    }

    const unsubscribe = onSnapshot(
      q,
      (snapshot) => {
        const data = snapshot.docs.map((doc) => ({
          id: doc.id,
          ...doc.data(),
        })) as AttendanceRecord[];
        setRecords(data);
        setLoading(false);
      },
      (err) => {
        setError(err.message);
        setLoading(false);
      }
    );

    return () => unsubscribe();
  }, [orgId, branchId, startDate, endDate]);

  return { records, loading, error };
}
