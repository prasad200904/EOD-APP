/**
 * Sync Employees from Firebase to Local Database
 * 
 * This script helps you understand which employees are in Firebase
 * and provides SQL statements to add them to your local Room database.
 */

const admin = require('firebase-admin');
const fs = require('fs');

// Initialize Firebase
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function generateSyncSQL() {
  console.log('🔍 Fetching employees from Firebase...\n');
  
  try {
    const snapshot = await db.collection('employees').get();
    
    console.log(`✅ Found ${snapshot.size} employees in Firebase\n`);
    console.log('=' .repeat(60));
    console.log('EMPLOYEES IN FIREBASE:');
    console.log('='.repeat(60) + '\n');
    
    const sqlStatements = [];
    
    snapshot.forEach((doc, index) => {
      const data = doc.data();
      
      console.log(`${index + 1}. ${data.name}`);
      console.log(`   ID: ${data.employeeId}`);
      console.log(`   Team: ${data.team}`);
      console.log(`   Department: ${data.department}`);
      console.log(`   Email: ${data.email}`);
      console.log('');
      
      // Generate SQL INSERT statement
      const sql = `
INSERT OR REPLACE INTO employees (
  employeeId, name, email, phone, department, departmentId, 
  team, managerId, designation, defaultProject, joiningDate, 
  role, password, isActive, createdAt
) VALUES (
  '${data.employeeId}',
  '${data.name.replace(/'/g, "''")}',
  '${data.email}',
  '${data.phone || '+1 (555) 555-0000'}',
  '${data.department}',
  '${data.departmentId}',
  '${data.team}',
  '${data.managerId || 'ADMIN'}',
  '${data.designation.replace(/'/g, "''")}',
  '${data.defaultProject || 'General Project'}',
  '${data.joiningDate}',
  'EMPLOYEE',
  'password123',
  1,
  ${Date.now()}
);`;
      
      sqlStatements.push(sql);
    });
    
    // Save SQL to file
    const sqlContent = `-- WorkCore Employee Sync SQL
-- Generated: ${new Date().toISOString()}
-- Total Employees: ${snapshot.size}

${sqlStatements.join('\n\n')}
`;
    
    fs.writeFileSync('employee_sync.sql', sqlContent);
    
    console.log('=' .repeat(60));
    console.log('📝 SQL STATEMENTS SAVED');
    console.log('='.repeat(60));
    console.log(`\n✅ Saved to: employee_sync.sql`);
    console.log(`   Total statements: ${sqlStatements.length}\n`);
    
    console.log('🎯 NEXT STEPS:\n');
    console.log('Option A: Use the App (Easiest)');
    console.log('   1. Login as Admin');
    console.log('   2. Go to Settings (if available)');
    console.log('   3. Click "Sync from Firebase"\n');
    
    console.log('Option B: The employees are already in Firebase!');
    console.log('   The app should load them when you:');
    console.log('   1. Login to a team (e.g., ML Team)');
    console.log('   2. Wait for data to load');
    console.log('   3. Check if employees appear\n');
    
    console.log('Option C: Add sync function to app (requires code change)');
    console.log('   See SYNC_IMPLEMENTATION_GUIDE.md\n');
    
    // Group by team
    const byTeam = {};
    snapshot.forEach(doc => {
      const data = doc.data();
      if (!byTeam[data.team]) {
        byTeam[data.team] = [];
      }
      byTeam[data.team].push(data.name);
    });
    
    console.log('=' .repeat(60));
    console.log('EMPLOYEES BY TEAM:');
    console.log('='.repeat(60) + '\n');
    
    Object.keys(byTeam).sort().forEach(team => {
      console.log(`${team}: ${byTeam[team].length} members`);
      byTeam[team].forEach((name, i) => {
        console.log(`  ${i + 1}. ${name}`);
      });
      console.log('');
    });
    
  } catch (error) {
    console.error('❌ Error:', error.message);
  }
  
  process.exit(0);
}

console.log('🔄 WorkCore Employee Sync Check\n');
generateSyncSQL();
