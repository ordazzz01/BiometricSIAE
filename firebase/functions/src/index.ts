import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();
const auth = admin.auth();

// Firestore triggers
export const onAttendanceCreated = functions.firestore
  .document("organizations/{orgId}/attendance/{recordId}")
  .onCreate(async (snap, context) => {
    const orgId = context.params.orgId;
    const record = snap.data();

    // Update employee's last checkin
    await db
      .collection("organizations")
      .doc(orgId)
      .collection("employees")
      .doc(record.employeeId)
      .update({
        lastCheckin: record.timestamp,
        lastCheckinType: record.type,
      });

    // Create audit log
    await db
      .collection("organizations")
      .doc(orgId)
      .collection("audit_logs")
      .add({
        action: "CHECKIN_CREATED",
        entityId: context.params.recordId,
        employeeId: record.employeeId,
        deviceId: record.deviceId,
        type: record.type,
        method: record.method,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
      });

    return null;
  });

export const onIncidentCreated = functions.firestore
  .document("organizations/{orgId}/incidents/{incidentId}")
  .onCreate(async (snap, context) => {
    const orgId = context.params.orgId;
    const incident = snap.data();

    // Create notification for admins
    await db
      .collection("organizations")
      .doc(orgId)
      .collection("notifications")
      .add({
        type: "NEW_INCIDENT",
        title: "Nueva incidencia reportada",
        body: `Incidencia tipo ${incident.type} para empleado ${incident.employeeId}`,
        read: false,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

    return null;
  });

// Callable functions
export const approveIncident = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Usuario no autenticado"
    );
  }

  const { orgId, incidentId, approved } = data;

  // Verify admin role
  const token = context.auth.token;
  if (token.tenantId !== orgId || !token.admin) {
    throw new functions.https.HttpsError(
      "permission-denied",
      "Sin permisos para esta operación"
    );
  }

  await db
    .collection("organizations")
    .doc(orgId)
    .collection("incidents")
    .doc(incidentId)
    .update({
      status: approved ? "APPROVED" : "REJECTED",
      approvedBy: context.auth.uid,
      approvedAt: admin.firestore.FieldValue.serverTimestamp(),
    });

  return { success: true };
});

export const getDashboardMetrics = functions.https.onCall(async (data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "unauthenticated",
      "Usuario no autenticado"
    );
  }

  const { orgId, branchId, date } = data;

  // Get today's attendance count
  const startOfDay = new Date(date);
  startOfDay.setHours(0, 0, 0, 0);
  const endOfDay = new Date(date);
  endOfDay.setHours(23, 59, 59, 999);

  let attendanceQuery = db
    .collection("organizations")
    .doc(orgId)
    .collection("attendance")
    .where("timestamp", ">=", startOfDay.toISOString())
    .where("timestamp", "<=", endOfDay.toISOString());

  if (branchId) {
    attendanceQuery = attendanceQuery.where("branchId", "==", branchId);
  }

  const attendanceSnapshot = await attendanceQuery.get();
  const totalCheckins = attendanceSnapshot.size;

  // Count by type
  const checkinsByType = {
    ENTRY: 0,
    EXIT: 0,
    BREAK: 0,
    BREAK_RETURN: 0,
  };

  attendanceSnapshot.forEach((doc) => {
    const type = doc.data().type;
    checkinsByType[type as keyof typeof checkinsByType]++;
  });

  // Get active employees count
  const employeesSnapshot = await db
    .collection("organizations")
    .doc(orgId)
    .collection("employees")
    .where("active", "==", true)
    .get();

  const totalEmployees = employeesSnapshot.size;

  // Get pending incidents count
  const incidentsSnapshot = await db
    .collection("organizations")
    .doc(orgId)
    .collection("incidents")
    .where("status", "==", "PENDING")
    .get();

  const pendingIncidents = incidentsSnapshot.size;

  // Get devices count
  const devicesSnapshot = await db
    .collection("organizations")
    .doc(orgId)
    .collection("devices")
    .where("active", "==", true)
    .get();

  const totalDevices = devicesSnapshot.size;

  return {
    totalCheckins,
    checkinsByType,
    totalEmployees,
    pendingIncidents,
    totalDevices,
  };
});
