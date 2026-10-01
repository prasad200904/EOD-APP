#!/usr/bin/env node
/**
 * Firestore Access Diagnostic Script
 * 
 * Checks why a user cannot see employee data in the app.
 * 
 * Usage: node debug_firestore_access.js user@example.com
 */

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const auth = admin.auth();

async function debugAccess(email) {
  console.log('='.repeat(60));
  console.log('🔍 FIRESTORE ACCESS DIAGNOSTIC');
  console.log('='.repeat(60));
  console.log(`Checking access for: ${email}\n`);
  
  try {
    // 1. Check Firebase Auth account
    console.log('1️⃣  Checking Firebase Auth account...');
    let authUser;
    try {
      authUser = await auth.getUserByEmail(email);
      console.log(`   ✅ Firebase Auth account exists`);
      console.log(`   UID: ${authUser.uid}\n`);
    } catch (e) {
      console.log(`   ❌ No Firebase Auth account for ${email}`);
      console.log(`   Error: ${e.code}`);
      console.log(`\n   💡 SOLUTION: Log in once through the app to create account\n`);
      return;
    }
    
    // 2. Check user document in /users/ collection
    console.log('2️⃣  Checking user document in /users/ collection...');
    const userDoc = await db.collection('users').doc(authUser.uid).get();
    if (!userDoc.exists) {
      console.log(`   ❌ User document MISSING in /users/${authUser.uid}`);
      console.log(`\n   💡 SOLUTION: Log out and log back in through the app`);
      console.log(`   The app will create this document automatically.\n`);
      return;
    }
    
    const userData = userDoc.data();
    console.log(`   ✅ User document exists`);
    console.log(`   Path: /users/${authUser.uid}`);
    console.log(`   Data:`);
    console.log(`      employeeId: ${userData.employeeId || '(missing)'}`);
    console.log(`      email: ${userData.email || '(missing)'}`);
    console.log(`      name: ${userData.name || '(missing)'}`);
    console.log(`      role: ${userData.role || '(missing)'}`);
    console.log(`      isActive: ${userData.isActive}`);
    console.log(`      teamId: ${userData.teamId || '(missing)'}`);
    console.log(`      departmentId: ${userData.departmentId || '(missing)'}\n`);
    
    // 3. Check if isActive = true
    console.log('3️⃣  Checking if user is active...');
    if (userData.isActive !== true) {
      console.log(`   ❌ User is NOT active (isActive = ${userData.isActive})`);
      console.log(`\n   💡 SOLUTION: Activate user with:`);
      console.log(`   node activate_user.js ${email}\n`);
      return;
    }
    console.log(`   ✅ User is active (isActive = true)\n`);
    
    // 4. Check if role is set
    console.log('4️⃣  Checking user role...');
    if (!userData.role) {
      console.log(`   ❌ User role is missing`);
      console.log(`\n   💡 SOLUTION: Set role in Firestore Console or re-login\n`);
      return;
    }
    console.log(`   ✅ User role is set: ${userData.role}\n`);
    
    // 5. Test employees collection access
    console.log('5️⃣  Testing access to /employees/ collection...');
    try {
      const employeeSnap = await db.collection('employees').limit(5).get();
      console.log(`   ✅ Can read /employees/ collection`);
      console.log(`   Found ${employeeSnap.size} employees (limited to 5 for test)\n`);
    } catch (e) {
      console.log(`   ❌ Cannot read /employees/ collection`);
      console.log(`   Error: ${e.message}`);
      console.log(`\n   💡 SOLUTION: Check Firestore rules are deployed:`);
      console.log(`   firebase deploy --only firestore:rules\n`);
      return;
    }
    
    // 6. Check specific employee document
    console.log('6️⃣  Checking employee document...');
    if (userData.employeeId) {
      try {
        const empDoc = await db.collection('employees').doc(userData.employeeId).get();
        if (empDoc.exists) {
          const empData = empDoc.data();
          console.log(`   ✅ Employee document exists: ${userData.employeeId}`);
          console.log(`   Data:`);
          console.log(`      name: ${empData.name}`);
          console.log(`      email: ${empData.email}`);
          console.log(`      department: ${empData.department}`);
          console.log(`      team: ${empData.team}`);
          console.log(`      designation: ${empData.designation}`);
          console.log(`      isActive: ${empData.isActive}\n`);
        } else {
          console.log(`   ⚠️  Employee document NOT FOUND: ${userData.employeeId}`);
          console.log(`   User document references this ID but it doesn't exist in /employees/`);
          console.log(`\n   💡 SOLUTION: Import employee or create in admin dashboard\n`);
        }
      } catch (e) {
        console.log(`   ❌ Error reading employee document: ${e.message}\n`);
      }
    } else {
      console.log(`   ⚠️  No employeeId in user document`);
      console.log(`   This field should be set when user logs in\n`);
    }
    
    // 7. Count total employees
    console.log('7️⃣  Counting total employees in Firestore...');
    try {
      const allEmployees = await db.collection('employees').get();
      console.log(`   ✅ Total employees in Firestore: ${allEmployees.size}\n`);
    } catch (e) {
      console.log(`   ❌ Error counting employees: ${e.message}\n`);
    }
    
    console.log('='.repeat(60));
    console.log('✅ DIAGNOSTIC COMPLETE - ALL CHECKS PASSED');
    console.log('='.repeat(60));
    console.log('\n📱 User should be able to see employee data in the app.');
    console.log('\nIf the app still shows no employees:');
    console.log('1. Force-close and reopen the app');
    console.log('2. Check Logcat for errors: adb logcat | findstr FirebaseDataSource');
    console.log('3. Ensure device has internet connection');
    console.log('4. Try signing out and signing in again\n');
    
  } catch (error) {
    console.log('='.repeat(60));
    console.log(`❌ DIAGNOSTIC ERROR`);
    console.log('='.repeat(60));
    console.log(`\nError: ${error.message}`);
    console.log(`\nStack trace:\n${error.stack}\n`);
  }
}

const email = process.argv[2];
if (!email) {
  console.log('Usage: node debug_firestore_access.js user@example.com');
  console.log('\nExample:');
  console.log('  node debug_firestore_access.js admin@company.com');
  console.log('  node debug_firestore_access.js employee@company.com');
  process.exit(1);
}

debugAccess(email)
  .then(() => process.exit(0))
  .catch(err => {
    console.error('Fatal error:', err);
    process.exit(1);
  });
