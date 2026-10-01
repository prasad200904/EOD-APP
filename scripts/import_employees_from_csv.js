/**
 * Import Employees from CSV to Firebase Firestore
 * 
 * This script reads the employees CSV file and imports them into Firebase.
 * It creates both the employee records and user authentication accounts.
 */

const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');
const csv = require('csv-parser');

// Initialize Firebase Admin SDK
const serviceAccountPath = path.join(__dirname, '..', 'service-account-key.json');

if (!fs.existsSync(serviceAccountPath)) {
  console.error('❌ Error: service-account-key.json not found!');
  console.log('📝 Please download your Firebase service account key:');
  console.log('   1. Go to Firebase Console → Project Settings → Service Accounts');
  console.log('   2. Click "Generate New Private Key"');
  console.log('   3. Save it as "service-account-key.json" in the project root');
  process.exit(1);
}

const serviceAccount = require(serviceAccountPath);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// CSV file path
const csvFilePath = path.join(__dirname, '..', 'employees_full_import (1).csv');

// Function to generate employee ID from name
function generateEmployeeId(name, department) {
  const prefix = department === 'ML' ? 'ML' : 
                 department === 'DB' ? 'DB' : 
                 department === 'GT' ? 'GT' : 
                 department === 'Cyber' ? 'CYB' : 
                 department === 'Writing' ? 'WR' : 'EMP';
  
  const timestamp = Date.now().toString().slice(-6);
  const namePart = name.replace(/[^A-Z]/g, '').slice(0, 3);
  return `${prefix}-${namePart}${timestamp}`;
}

// Function to generate default phone number
function generatePhone(index) {
  const base = 5550000;
  return `+1 (555) ${String(base + index).padStart(7, '0').replace(/(\d{3})(\d{4})/, '$1-$2')}`;
}

// Function to import employees from CSV
async function importEmployees() {
  const employees = [];
  
  console.log('📖 Reading CSV file...');
  
  return new Promise((resolve, reject) => {
    fs.createReadStream(csvFilePath)
      .pipe(csv())
      .on('data', (row) => {
        employees.push(row);
      })
      .on('end', async () => {
        console.log(`✅ Found ${employees.length} employees in CSV\n`);
        
        let successCount = 0;
        let errorCount = 0;
        
        for (let i = 0; i < employees.length; i++) {
          const emp = employees[i];
          
          try {
            // Generate employee ID
            const employeeId = generateEmployeeId(emp.name, emp.department);
            
            // Create employee data
            const employeeData = {
              employeeId: employeeId,
              name: emp.name,
              email: emp.email,
              phone: generatePhone(i + 1),
              department: emp.department,
              departmentId: emp.departmentId,
              team: emp.team === 'General Team' ? 'GT Team' : emp.team, // Fix team name mismatch
              managerId: emp.managerId || 'ADMIN',
              designation: emp.designation,
              defaultProject: 'General Project',
              joiningDate: emp.joiningDate || '2026-01-01',
              role: 'EMPLOYEE',
              password: 'password123', // Default password
              isActive: true,
              createdAt: admin.firestore.FieldValue.serverTimestamp(),
              updatedAt: admin.firestore.FieldValue.serverTimestamp()
            };
            
            // Save to Firestore employees collection
            await db.collection('employees').doc(employeeId).set(employeeData);
            
            console.log(`✅ [${i + 1}/${employees.length}] Created: ${emp.name} (${employeeId})`);
            successCount++;
            
          } catch (error) {
            console.error(`❌ [${i + 1}/${employees.length}] Failed: ${emp.name}`);
            console.error(`   Error: ${error.message}`);
            errorCount++;
          }
        }
        
        console.log('\n' + '='.repeat(60));
        console.log('📊 Import Summary:');
        console.log(`   ✅ Success: ${successCount}`);
        console.log(`   ❌ Failed: ${errorCount}`);
        console.log(`   📝 Total: ${employees.length}`);
        console.log('='.repeat(60) + '\n');
        
        console.log('🎉 Import complete!');
        console.log('\n📱 Next steps:');
        console.log('   1. Open the WorkCore app');
        console.log('   2. Login to a team (e.g., ML Team with password: ml123)');
        console.log('   3. You should see all team members!');
        console.log('\n💡 Default employee password: password123');
        
        resolve();
      })
      .on('error', (error) => {
        reject(error);
      });
  });
}

// Run the import
console.log('🚀 WorkCore Employee CSV Import\n');
console.log('📁 CSV File: employees_full_import (1).csv');
console.log('🔥 Target: Firebase Firestore\n');

importEmployees()
  .then(() => {
    console.log('\n✅ Script completed successfully');
    process.exit(0);
  })
  .catch((error) => {
    console.error('\n❌ Script failed:', error);
    process.exit(1);
  });
