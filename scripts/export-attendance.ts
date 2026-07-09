#!/usr/bin/env node

/**
 * Export attendance records to CSV
 * 
 * Usage: npx ts-node scripts/export-attendance.ts <orgId> <startDate> <endDate>
 * 
 * Example: npx ts-node scripts/export-attendance.ts org_123 2024-01-01 2024-01-31
 */

import * as fs from 'fs';
import * as path from 'path';

interface AttendanceRecord {
  id: string;
  employeeId: string;
  branchId: string;
  type: string;
  timestamp: string;
  method: string;
  syncStatus: string;
}

function formatDate(date: string): string {
  return new Date(date).toLocaleString('es-MX');
}

function generateCSV(records: AttendanceRecord[]): string {
  const headers = [
    'ID',
    'Empleado',
    'Sucursal',
    'Tipo',
    'Fecha/Hora',
    'Método',
    'Estado'
  ];

  const rows = records.map(record => [
    record.id,
    record.employeeId,
    record.branchId,
    record.type,
    formatDate(record.timestamp),
    record.method,
    record.syncStatus
  ]);

  return [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n');
}

async function main() {
  const args = process.argv.slice(2);
  
  if (args.length < 3) {
    console.log('Usage: npx ts-node scripts/export-attendance.ts <orgId> <startDate> <endDate>');
    console.log('Example: npx ts-node scripts/export-attendance.ts org_123 2024-01-01 2024-01-31');
    process.exit(1);
  }

  const [orgId, startDate, endDate] = args;

  console.log(`Exporting attendance for org: ${orgId}`);
  console.log(`Date range: ${startDate} to ${endDate}`);

  // In production, fetch from Firestore or API
  // For now, use mock data
  const mockRecords: AttendanceRecord[] = [
    {
      id: 'rec_001',
      employeeId: 'emp_001',
      branchId: 'branch_001',
      type: 'ENTRY',
      timestamp: '2024-01-15T08:02:15-06:00',
      method: 'BIOMETRIC',
      syncStatus: 'SYNCED'
    },
    {
      id: 'rec_002',
      employeeId: 'emp_001',
      branchId: 'branch_001',
      type: 'EXIT',
      timestamp: '2024-01-15T17:05:30-06:00',
      method: 'BIOMETRIC',
      syncStatus: 'SYNCED'
    }
  ];

  const csv = generateCSV(mockRecords);
  
  const filename = `attendance_${orgId}_${startDate}_${endDate}.csv`;
  const filepath = path.join(process.cwd(), filename);
  
  fs.writeFileSync(filepath, csv, 'utf-8');
  
  console.log(`✓ Exported ${mockRecords.length} records to ${filename}`);
}

main().catch(console.error);
