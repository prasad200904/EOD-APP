/**
 * Simple Employee Import Script
 * No external dependencies except firebase-admin
 */

const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Initialize Firebase
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Employee data from CSV
const employees = [
  {
    name: "AADEPU SUSHMA",
    email: "aadepu.sushma@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",
    managerId: "ADMIN",
    designation: "Cyber Security & Network Analyst",
    joiningDate: "2026-01-01"
  },
  {
    name: "VUYYALA SYAM GOPAL",
    email: "vuyyala.gopal@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",
    managerId: "ADMIN",
    designation: "Cyber Security Analyst",
    joiningDate: "2026-01-01"
  },
  {
    name: "KALLA GOWTHAM",
    email: "kalla.gowtham@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",
    managerId: "ADMIN",
    designation: "Cyber Security Analyst",
    joiningDate: "2026-01-01"
  },
  {
    name: "V SIREESHA",
    email: "v.sireesha@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",
    managerId: "ADMIN",
    designation: "Cyber Security Analyst",
    joiningDate: "2026-01-01"
  },
  {
    name: "SEKHARAPALLI KARTHIK RAM KUMAR",
    email: "sekharapalli.kumar@sample.com",
    department: "Cyber",
    departmentId: "DEPT_CYBER",
    team: "Cyber Security",
    managerId: "ADMIN",
    designation: "Cyber Security Forensic Engineer",
    joiningDate: "2026-01-01"
  },
  {
    name: "PEETHA SRI HARSHINI",
    email: "peetha.harshini@sample.com",
    department: "DB",
    departmentId: "DEPT_DB",
    team: "DB Team",
    managerId: "EMP009",
    designation: "Data Analyst",
    joiningDate: "2026-01-01"
  },
  {
    name: "PAMULAPATI SUPRIYA",
    email: "pamulapati.supriya@sample.com",
    department: "DB",
    departmentId: "DEPT_DB",
    team: "DB Team",
    managerId: "EMP009",
    designation: "Data Analyst",
    joiningDate: "2026-01-01"
  },
  {
    name: "KONDA VASUNDHARA REDDY",
    email: "konda.reddy@sample.com",
    department: "DB",
    departmentId: "DEPT_DB",
    team: "DB Team",
    managerId: "EMP009",
    designation: "Data Analyst",
    joiningDate: "2026-01-01"
  },
  {
    name: "MOHAMMED ASHAR REHAN",
    email: "mohammed.rehan@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist",
    joiningDate: "2026-01-01"
  },
  {
    name: "SAYYAD SAMEER",
    email: "sayyad.sameer@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist",
    joiningDate: "2026-01-01"
  },
  {
    name: "REKHAPALLI NAGALAKSHMI",
    email: "rekhapalli.nagalakshmi@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist",
    joiningDate: "2026-01-01"
  },
  {
    name: "GADE BALA ABHILASH REDDY",
    email: "gade.reddy@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist",
    joiningDate: "2026-01-01"
  },
  {
    name: "MALLIPEDDI LOHITHA",
    email: "mallipeddi.lohitha@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist",
    joiningDate: "2026-01-01"
  },
  {
    name: "KESANA NAGA MANIKANTA BABU",
    email: "kesana.babu@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist",
    joiningDate: "2026-01-01"
  },
  {
    name: "MATTA VINAY AKHIL",
    email: "matta.akhil@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist",
    joiningDate: "2026-01-01"
  },
  {
    name: "PEDDI SAI KIRAN",
    email: "peddi.kiran@sample.com",
    department: "ML",
    departmentId: "DEPT_ML",
    team: "ML Team",
    managerId: "EMP009",
    designation: "Data Scientist",
    joiningDate: "2026-01-01"
  },
  {
    name: "MEDISETTI CHANDRA MOULI",
    email: "medisetti.mouli@sample.com",
    department: "GT",
    departmentId: "DEPT_GT",
    team: "GT Team",
    managerId: "EMP008",
    designation: "Full-Stack Developer",
    joiningDate: "2026-01-01"
  },
  {
    name: "EEDPUGANTI ESWARI",
    email: "eedpuganti.eswari@sample.com",
    department: "Writing",
    departmentId: "DEPT_WRITING",
    team: "Writing Team",
    managerId: "EMP005",
    designation: "Research Analyst",
    joiningDate: "2026-01-01"
  },
  {
    name: "KAKANI JAGADEESH",
    email: "kakani.jagadeesh@sample.com",
    department: "Writing",
    departmentId: "DEPT_WRITING",
    team: "Writing Team",
    managerId: "EMP005",
    designation: "Research Analyst",
    joiningDate: "2026-01-01"
  }
];

// Generate employee ID
function generateEmployeeId(name, department, index) {
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
  console.log('🚀 WorkCore Simple Employee Import\n');
  console.log(`📝 Importing ${employees.length} employees to Firebase...\n`);
  
  let successCount = 0;
  let errorCount = 0;
  
  for (let i = 0; i < employees.length; i++) {
    const emp = employees[i];
    
    try {
      const employeeId = generateEmployeeId(emp.name, emp.department, i);
      
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
        joiningDate: emp.joiningDate,
        role: 'EMPLOYEE',
        password: 'password123',
        isActive: true,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      };
      
      await db.collection('employees').doc(employeeId).set(employeeData);
      
      console.log(`✅ [${i + 1}/${employees.length}] ${emp.name} (${employeeId})`);
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
  
  console.log('\n🎉 Import complete!');
  console.log('\n📱 Next steps:');
  console.log('   1. Open WorkCore app');
  console.log('   2. Login to ML Team (password: ml123)');
  console.log('   3. You should see 8 ML Team members!');
  console.log('\n💡 Default employee password: password123\n');
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
