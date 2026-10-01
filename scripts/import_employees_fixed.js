/**
 * Import Employees with CORRECT Team Names
 * This fixes the team name mismatches
 */

const admin = require('firebase-admin');

// Initialize Firebase
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Employee data with FIXED team names
const employees = [
  // CYBER SECURITY TEAM (5 members)
  {
    name: "AADEPU SUSHMA",
    email: "aadepu.sushma@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",  // ✅ FIXED
    managerId: "ADMIN",
    designation: "Cyber Security & Network Analyst"
  },
  {
    name: "VUYYALA SYAM GOPAL",
    email: "vuyyala.gopal@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",
    managerId: "ADMIN",
    designation: "Cyber Security Analyst"
  },
  {
    name: "KALLA GOWTHAM",
    email: "kalla.gowtham@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",
    managerId: "ADMIN",
    designation: "Cyber Security Analyst"
  },
  {
    name: "V SIREESHA",
    email: "v.sireesha@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",
    managerId: "ADMIN",
    designation: "Cyber Security Analyst"
  },
  {
    name: "SEKHARAPALLI KARTHIK RAM KUMAR",
    email: "sekharapalli.kumar@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",
    managerId: "ADMIN",
    designation: "Cyber Security Forensic Engineer"
  },
  
  // DB TEAM (3 members)
  {
    name: "PEETHA SRI HARSHINI",
    email: "peetha.harshini@sample.com",
    department: "DB",
    departmentId: "DEPT_DB",
    team: "DB Team",
    managerId: "EMP009",
    designation: "Data Analyst"
  },
  {
    name: "PAMULAPATI SUPRIYA",
    email: "pamulapati.supriya@sample.com",
    department: "DB",
    departmentId: "DEPT_DB",
    team: "DB Team",
    managerId: "EMP009",
    designation: "Data Analyst"
  },
  {
    name: "KONDA VASUNDHARA REDDY",
    email: "konda.reddy@sample.com",
    department: "DB",
    departmentId: "DEPT_DB",
    team: "DB Team",
    managerId: "EMP009",
    designation: "Data Analyst"
  },
  
  // ML TEAM (8 members)
  {
    name: "MOHAMMED ASHAR REHAN",
    email: "mohammed.rehan@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist"
  },
  {
    name: "SAYYAD SAMEER",
    email: "sayyad.sameer@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist"
  },
  {
    name: "REKHAPALLI NAGALAKSHMI",
    email: "rekhapalli.nagalakshmi@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist"
  },
  {
    name: "GADE BALA ABHILASH REDDY",
    email: "gade.reddy@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist"
  },
  {
    name: "MALLIPEDDI LOHITHA",
    email: "mallipeddi.lohitha@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist"
  },
  {
    name: "KESANA NAGA MANIKANTA BABU",
    email: "kesana.babu@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist"
  },
  {
    name: "MATTA VINAY AKHIL",
    email: "matta.akhil@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist"
  },
  {
    name: "PEDDI SAI KIRAN",
    email: "peddi.kiran@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist"
  },
  
  // GT TEAM (1 member)
  {
    name: "MEDISETTI CHANDRA MOULI",
    email: "medisetti.mouli@sample.com",
    department: "GT",
    departmentId: "DEPT_GT",
    team: "GT Team",  // ✅ FIXED (was "General Team")
    managerId: "EMP008",
    designation: "Full-Stack Developer"
  },
  
  // WRITING TEAM (2 members)
  {
    name: "EEDPUGANTI ESWARI",
    email: "eedpuganti.eswari@sample.com",
    department: "Writing",
    departmentId: "DEPT_WRITING",
    team: "Writing Team",
    managerId: "EMP005",
    designation: "Research Analyst"
  },
  {
    name: "KAKANI JAGADEESH",
    email: "kakani.jagadeesh@sample.com",
    department: "Writing",
    departmentId: "DEPT_WRITING",
    team: "Writing Team",
    managerId: "EMP005",
    designation: "Research Analyst"
  }
];

// Generate employee ID
function generateEmployeeId(department, index) {
  const prefix = department === 'ML' ? 'ML' : 
                 department === 'DB' ? 'DB' : 
                 department === 'GT' ? 'GT' : 
                 department === 'Cyber' ? 'CYB' : 
                 department === 'Writing' ? 'WR' : 'EMP';
  
  const num = String(index + 1).padStart(3, '0');
  return `${prefix}-${num}`;
}

// Generate phone number
function generatePhone(index) {
  const num = String(5550001 + index).padStart(7, '0');
  return `+1 (555) ${num.substring(0, 3)}-${num.substring(3)}`;
}

// Import function
async function importEmployees() {
  console.log('🚀 WorkCore Employee Import (FIXED Team Names)\n');
  console.log('⚠️  WARNING: This will DELETE all existing employees!\n');
  console.log('Press Ctrl+C now if you want to cancel...\n');
  
  // Wait 3 seconds
  await new Promise(resolve => setTimeout(resolve, 3000));
  
  console.log('🗑️  Deleting old employees...\n');
  
  // Delete all existing employees
  const existingSnapshot = await db.collection('employees').get();
  let deleteCount = 0;
  for (const doc of existingSnapshot.docs) {
    await doc.ref.delete();
    deleteCount++;
  }
  
  console.log(`✅ Deleted ${deleteCount} old employees\n`);
  console.log(`📝 Importing ${employees.length} employees with correct team names...\n`);
  
  let successCount = 0;
  let errorCount = 0;
  
  for (let i = 0; i < employees.length; i++) {
    const emp = employees[i];
    
    try {
      const employeeId = generateEmployeeId(emp.department, i);
      
      const employeeData = {
        employeeId: employeeId,
        name: emp.name,
        email: emp.email,
        phone: generatePhone(i),
        department: emp.department,
        departmentId: emp.departmentId,
        team: emp.team,
        managerId: emp.managerId,
        designation: emp.designation,
        defaultProject: 'General Project',
        joiningDate: '2026-01-01',
        role: 'EMPLOYEE',
        password: 'password123',
        isActive: true,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      };
      
      await db.collection('employees').doc(employeeId).set(employeeData);
      
      console.log(`✅ [${i + 1}/${employees.length}] ${emp.name} (${employeeId}) → ${emp.team}`);
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
  console.log('='.repeat(60));
  
  console.log('\n📋 Employees by Team:');
  console.log('   Cyber Security: 5 members');
  console.log('   DB Team: 3 members');
  console.log('   ML Team: 8 members');
  console.log('   GT Team: 1 member  ← FIXED (was "General Team")');
  console.log('   Writing Team: 2 members');
  
  console.log('\n🎉 Import complete with CORRECT team names!');
  console.log('\n📱 Test in app:');
  console.log('   1. Rebuild and reinstall app');
  console.log('   2. Login to ML Team (password: ml123)');
  console.log('   3. You should see 8 ML Team members!');
  console.log('\n💡 All employees have password: password123\n');
}

// Run
importEmployees()
  .then(() => {
    console.log('✅ Script completed successfully');
    process.exit(0);
  })
  .catch((error) => {
    console.error('❌ Script failed:', error);
    process.exit(1);
  });
