export interface Organization {
  id: string;
  name: string;
  rfc?: string;
  plan: string;
  createdAt: string;
}

export interface Branch {
  id: string;
  tenantId: string;
  name: string;
  address?: string;
  latitude?: number;
  longitude?: number;
  timezone: string;
  active: boolean;
  createdAt: string;
}

export interface Employee {
  id: string;
  tenantId: string;
  code: string;
  name: string;
  department?: string;
  position?: string;
  photoUrl?: string;
  branchIds: string[];
  scheduleId?: string;
  biometricEnrolled: boolean;
  faceRegistered: boolean;
  active: boolean;
  lastCheckin?: string;
  createdAt: string;
  updatedAt: string;
}

export interface AttendanceRecord {
  id: string;
  tenantId: string;
  employeeId: string;
  employeeName?: string;
  branchId: string;
  branchName?: string;
  deviceId: string;
  type: AttendanceType;
  timestamp: string;
  timezone: string;
  latitude?: number;
  longitude?: number;
  method: AuthMethod;
  faceEvidenceUrl?: string;
  syncStatus: SyncStatus;
  hash: string;
  createdAt: string;
}

export type AttendanceType = "ENTRY" | "EXIT" | "BREAK" | "BREAK_RETURN";

export type AuthMethod = "BIOMETRIC" | "FACE" | "PIN_FALLBACK" | "QR" | "MANUAL";

export type SyncStatus = "PENDING" | "SYNCED" | "CONFLICT" | "FAILED";

export interface Device {
  id: string;
  tenantId: string;
  name: string;
  branchId: string;
  type: "KIOSK" | "MOBILE" | "TABLET";
  lastSyncAt?: string;
  active: boolean;
  config?: DeviceConfig;
}

export interface DeviceConfig {
  faceRequired: boolean;
  geofenceEnabled: boolean;
  locationRequired: boolean;
  kioskMode: boolean;
}

export interface Incident {
  id: string;
  tenantId: string;
  employeeId: string;
  branchId: string;
  deviceId: string;
  type: IncidentType;
  description: string;
  authorizedBy?: string;
  status: IncidentStatus;
  createdAt: string;
}

export type IncidentType = "MANUAL" | "LATE_ARRIVAL" | "EARLY_EXIT" | "MISSING_CHECKOUT";

export type IncidentStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface FaceEvidence {
  id: string;
  tenantId: string;
  attendanceRecordId: string;
  url: string;
  faceDetected: boolean;
  faceCentered: boolean;
  eyesVisible: boolean;
  createdAt: string;
}

export interface DashboardMetrics {
  totalCheckins: number;
  checkinsByType: {
    ENTRY: number;
    EXIT: number;
    BREAK: number;
    BREAK_RETURN: number;
  };
  totalEmployees: number;
  pendingIncidents: number;
  totalDevices: number;
}
