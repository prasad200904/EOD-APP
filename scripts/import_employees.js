/**
 * WorkCore - Firestore Employee Importer Script
 * 
 * Usage:
 *   DRY-RUN mode (Default - prints preview without writing):
 *     node import_employees.js
 * 
 *   EXECUTE mode (Writes to Firestore):
 *     node import_employees.js --write
 * 
 *   CSV File:
 *     Place 'employees_full_import (1).csv' in the scripts folder
 *     Expected columns: name, email, department, departmentId, team, managerId, designation, joiningDate
 * 
 *   Employee ID Generation:
 *     Automatically queries Firestore for highest existing EMPnnn ID and continues from there
 */

const fs = require('fs');
const path = require('path');

// 1. Command Line Arguments Parsing
const args = process.argv.slice(2);
const isExecuteMode = args.includes('--write') || args.includes('--execute');
const isDryRun = !isExecuteMode;

console.log('====================================================');
console.log(`🚀 WORKCORE FIRESTORE EMPLOYEE IMPORTER`);
console.log(`MODE      : ${isDryRun ? '🔍 DRY-RUN (Preview Only - No changes written)' : '⚡ WRITE MODE (Writing to Firestore)'}`);
console.log('====================================================\n');

// 2. Locate Service Account Key
const keyFilename = 'serviceAccountKey.json';
const possibleKeyPaths = [
  path.join(__dirname, keyFilename),
  path.join(__dirname, '..', keyFilename)
];

let serviceAccountPath = null;
for (const p of possibleKeyPaths) {
  if (fs.existsSync(p)) {
    serviceAccountPath = p;
    break;
  }
}

// 3. Initialize Firebase Admin SDK
let admin = null;
let db = null;

if (isExecuteMode) {
  if (!serviceAccountPath) {
    console.error(`❌ ERROR: Service account key '${keyFilename}' not found!`);
    console.error(`Please download your Firebase Service Account key from:`);
    console.error(`  Firebase Console -> Project Settings -> Service Accounts -> Generate New Private Key`);
    console.error(`Save key file as: ${path.join(__dirname, keyFilename)}\n`);
    process.exit(1);
  }

  try {
    admin = require('firebase-admin');
    const serviceAccount = require(serviceAccountPath);
    
    admin.initializeApp({
      credential: admin.credential.cert(serviceAccount)
    });
    
    db = admin.firestore();
    console.log(`✅ Connected to Firebase Project: [${serviceAccount.project_id || 'DEV'}]\n`);
  } catch (err) {
    console.error(`❌ Failed to initialize Firebase Admin SDK: ${err.message}`);
    process.exit(1);
  }
}

// 4. Department Default Project Mapping (from SeedData.kt)
function getDefaultProject(departmentId) {
  const projectMap = {
    'DEPT_ML': 'Model Evaluation & Infra',
    'DEPT_DB': 'PostgreSQL Migration',
    'DEPT_GT': 'Operations & Compliance',
    'DEPT_WRITING': 'Technical Documentation',
    'DEPT_CYBER': 'Security Audit & Monitoring'
  };
  return projectMap[departmentId] || 'Core Engineering';
}

// 5. Query Firestore for Highest Existing Employee ID
async function getNextEmployeeId(db) {
  if (isDryRun) {
    console.log('🔍 Dry-run mode: Skipping Firestore query, starting from EMP010\n');
    return 10; // Default starting point for dry-run
  }

  try {
    console.log('🔎 Querying Firestore for existing employees...');
    const snapshot = await db.collection('employees')
      .orderBy('employeeId', 'desc')
      .limit(1)
      .get();

    if (snapshot.empty) {
      console.log('   ℹ️  No existing employees found. Starting from EMP010\n');
      return 10;
    }

    const highestId = snapshot.docs[0].data().employeeId;
    console.log(`   ℹ️  Highest existing ID: ${highestId}`);

    // Extract number from formats like EMP010, ADMIN, ML-001, etc.
    const match = highestId.match(/EMP(\d+)/);
    if (match) {
      const nextNum = parseInt(match[1], 10) + 1;
      console.log(`   ✅ Next available ID: EMP${String(nextNum).padStart(3, '0')}\n`);
      return nextNum;
    } else {
      console.log('   ℹ️  No EMP-format IDs found. Starting from EMP010\n');
      return 10;
    }
  } catch (error) {
    console.warn(`   ⚠️  Error querying Firestore: ${error.message}`);
    console.log('   ℹ️  Defaulting to EMP010\n');
    return 10;
  }
}

// 6. CSV File Parsing Helper (New Format with 8 columns)
function parseCsvFile(filePath) {
  if (!fs.existsSync(filePath)) {
    console.error(`❌ CSV File not found: ${filePath}`);
    console.error(`   Expected: employees_full_import (1).csv`);
    console.error(`   Location: ${path.dirname(filePath)}\n`);
    process.exit(1);
  }

  const rawText = fs.readFileSync(filePath, 'utf8');
  const lines = rawText.split(/\r?\n/).filter(line => line.trim().length > 0);

  if (lines.length < 2) {
    console.error('❌ CSV file is empty or missing data rows.');
    process.exit(1);
  }

  const headers = lines[0].split(',').map(h => h.trim());
  
  const nameIndex = headers.findIndex(h => h.toLowerCase() === 'name');
  const emailIndex = headers.findIndex(h => h.toLowerCase() === 'email');
  const deptIndex = headers.findIndex(h => h.toLowerCase() === 'department');
  const deptIdIndex = headers.findIndex(h => h.toLowerCase() === 'departmentid');
  const teamIndex = headers.findIndex(h => h.toLowerCase() === 'team');
  const managerIdIndex = headers.findIndex(h => h.toLowerCase() === 'managerid');
  const desigIndex = headers.findIndex(h => h.toLowerCase() === 'designation');
  const joiningDateIndex = headers.findIndex(h => h.toLowerCase() === 'joiningdate');

  const rows = [];
  for (let i = 1; i < lines.length; i++) {
    const rawLine = lines[i];
    const cols = rawLine.split(',').map(c => c.trim());

    const name = nameIndex !== -1 ? cols[nameIndex] : '';
    const email = emailIndex !== -1 ? cols[emailIndex] : '';
    const department = deptIndex !== -1 ? cols[deptIndex] : '';
    const departmentId = deptIdIndex !== -1 ? cols[deptIdIndex] : '';
    const team = teamIndex !== -1 ? cols[teamIndex] : '';
    const managerId = managerIdIndex !== -1 ? cols[managerIdIndex] : '';
    const designation = desigIndex !== -1 ? cols[desigIndex] : '';
    const joiningDate = joiningDateIndex !== -1 ? cols[joiningDateIndex] : '';

    rows.push({
      lineNum: i + 1,
      name,
      email,
      department,
      departmentId,
      team,
      managerId,
      designation,
      joiningDate
    });
  }

  return rows;
}

// 7. Main Import Logic
async function runImporter() {
  const csvPath = path.join(__dirname, 'employees_full_import (1).csv');
  const rawRows = parseCsvFile(csvPath);

  let totalRead = rawRows.length;
  let importedCount = 0;
  let skippedCount = 0;

  const validEntries = [];
  const skippedDetails = [];

  console.log(`📋 Processing ${totalRead} rows from ${path.basename(csvPath)}...\n`);

  // Get next available employee ID
  let nextIdNum = await getNextEmployeeId(db);

  for (const r of rawRows) {
    const name = (r.name || '').trim();
    const email = (r.email || '').trim();
    const designation = (r.designation || '').trim();

    // Skip rows missing required fields
    if (!name || !email || !designation) {
      skippedCount++;
      const missing = [];
      if (!name) missing.push('name');
      if (!email) missing.push('email');
      if (!designation) missing.push('designation');
      skippedDetails.push(`Line ${r.lineNum}: Missing required fields: ${missing.join(', ')}`);
      continue;
    }

    // Generate sequential employee ID
    const employeeId = `EMP${String(nextIdNum).padStart(3, '0')}`;
    nextIdNum++;

    // Build complete EmployeeEntity matching Entities.kt structure
    const docData = {
      employeeId: employeeId,
      name: name,
      email: email.toLowerCase(),
      phone: '',
      department: r.department || 'GT',
      departmentId: r.departmentId || 'DEPT_GT',
      managedDepartments: '',
      team: r.team || 'Core Team',
      managerId: r.managerId || 'ADMIN',
      designation: designation,
      defaultProject: getDefaultProject(r.departmentId || 'DEPT_GT'),
      joiningDate: r.joiningDate || '2026-01-01',
      role: 'EMPLOYEE',
      isActive: true,
      password: 'password123',
      createdAt: isExecuteMode && admin ? admin.firestore.Timestamp.now() : Date.now()
    };

    validEntries.push({ 
      employeeId, 
      data: docData, 
      lineNum: r.lineNum 
    });
  }

  // 8. Preview or Commit
  if (isDryRun) {
    console.log('╔══════════════════════════════════════════════════════════════╗');
    console.log('║          DRY-RUN PREVIEW (No data will be written)           ║');
    console.log('╚══════════════════════════════════════════════════════════════╝\n');
    
    // Print table header
    console.log('─'.repeat(120));
    console.log(
      'ID'.padEnd(10) + 
      'Name'.padEnd(35) + 
      'Email'.padEnd(35) + 
      'Dept'.padEnd(15) + 
      'Designation'.padEnd(25)
    );
    console.log('─'.repeat(120));

    validEntries.forEach((entry) => {
      console.log(
        entry.employeeId.padEnd(10) +
        entry.data.name.substring(0, 33).padEnd(35) +
        entry.data.email.substring(0, 33).padEnd(35) +
        entry.data.department.padEnd(15) +
        entry.data.designation.substring(0, 23).padEnd(25)
      );
    });
    console.log('─'.repeat(120));
    
    importedCount = validEntries.length;
  } else {
    console.log(`⚡ Writing ${validEntries.length} documents to Firestore in batches...\n`);
    const BATCH_SIZE = 500;
    
    for (let i = 0; i < validEntries.length; i += BATCH_SIZE) {
      const chunk = validEntries.slice(i, i + BATCH_SIZE);
      const batch = db.batch();

      for (const item of chunk) {
        const docRef = db.collection('employees').doc(item.employeeId);
        batch.set(docRef, item.data);
      }

      await batch.commit();
      console.log(`   ✅ Batch ${Math.floor(i / BATCH_SIZE) + 1}: Written ${chunk.length} employees (${i + chunk.length}/${validEntries.length})`);
    }

    importedCount = validEntries.length;
  }

  // 9. Summary Output
  console.log('\n╔══════════════════════════════════════════════════════════════╗');
  console.log('║                    IMPORT SUMMARY REPORT                     ║');
  console.log('╚══════════════════════════════════════════════════════════════╝');
  console.log(`Total Rows Read      : ${totalRead}`);
  console.log(`✅ Successfully Imported : ${importedCount} ${isDryRun ? '(Preview Only)' : '(Written to Firestore)'}`);
  console.log(`⚠️  Skipped Rows         : ${skippedCount}`);

  if (skippedDetails.length > 0) {
    console.log('\n⚠️  SKIPPED ROWS DETAILS:');
    skippedDetails.forEach(msg => console.log(`   • ${msg}`));
  }

  console.log('═'.repeat(64));

  if (isDryRun) {
    console.log('\n💡 This was a DRY-RUN. No data was written to Firestore.');
    console.log('   To actually import these employees, run:');
    console.log('   \x1b[1m\x1b[36mnode import_employees.js --write\x1b[0m\n');
  } else {
    console.log('\n✅ Import completed successfully!\n');
  }
}

// Run the script
runImporter().catch(err => {
  console.error('❌ Script execution failed:', err);
  process.exit(1);
});
