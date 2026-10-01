#!/usr/bin/env node
/**
 * User Activation Script
 * 
 * Sets isActive = true for a user in the /users/ collection.
 * Required for Firestore rules to allow data access.
 * 
 * Usage: node activate_user.js user@example.com
 */

const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();
const auth = admin.auth();

async function activateUser(email) {
  console.log('='.repeat(60));
  console.log('🔓 USER ACTIVATION TOOL');
  console.log('='.repeat(60));
  console.log(`Target user: ${email}\n`);
  
  try {
    // 1. Find Firebase Auth user
    console.log('1️⃣  Looking up Firebase Auth account...');
    let authUser;
    try {
      authUser = await auth.getUserByEmail(email);
      console.log(`   ✅ Found user with UID: ${authUser.uid}\n`);
    } catch (e) {
      console.log(`   ❌ No Firebase Auth account found for ${email}`);
      console.log(`   Error: ${e.code}`);
      console.log(`\n   💡 User must log in through the app first to create account\n`);
      return;
    }
    
    // 2. Check if user document exists
    console.log('2️⃣  Checking user document...');
    const userRef = db.collection('users').doc(authUser.uid);
    const userDoc = await userRef.get();
    
    if (!userDoc.exists) {
      console.log(`   ⚠️  User document does not exist in /users/${authUser.uid}`);
      console.log(`   Creating new user document...\n`);
      
      await userRef.set({
        uid: authUser.uid,
        email: email,
        isActive: true,
        role: 'EMPLOYEE',
        updatedAt: Date.now(),
        createdAt: Date.now()
      });
      
      console.log(`   ✅ Created user document with isActive = true\n`);
    } else {
      console.log(`   ✅ User document exists\n`);
      
      // 3. Update isActive to true
      console.log('3️⃣  Activating user...');
      const currentData = userDoc.data();
      
      if (currentData.isActive === true) {
        console.log(`   ℹ️  User is already active (no change needed)\n`);
      } else {
        await userRef.update({
          isActive: true,
          updatedAt: Date.now()
        });
        console.log(`   ✅ Updated isActive: ${currentData.isActive} → true\n`);
      }
    }
    
    // 4. Display final state
    console.log('4️⃣  Final user state:');
    const finalDoc = await userRef.get();
    const finalData = finalDoc.data();
    console.log(`   UID: ${authUser.uid}`);
    console.log(`   Email: ${finalData.email || email}`);
    console.log(`   Role: ${finalData.role || '(not set)'}`);
    console.log(`   isActive: ${finalData.isActive}`);
    console.log(`   employeeId: ${finalData.employeeId || '(not set)'}`);
    console.log(`   teamId: ${finalData.teamId || '(not set)'}`);
    console.log(`   departmentId: ${finalData.departmentId || '(not set)'}\n`);
    
    console.log('='.repeat(60));
    console.log('✅ USER ACTIVATION COMPLETE');
    console.log('='.repeat(60));
    console.log('\n📱 User can now access employee data in the app.');
    console.log('   If already logged in, they may need to restart the app.\n');
    
  } catch (error) {
    console.log('='.repeat(60));
    console.log('❌ ACTIVATION FAILED');
    console.log('='.repeat(60));
    console.log(`\nError: ${error.message}`);
    console.log(`\nStack trace:\n${error.stack}\n`);
  }
}

const email = process.argv[2];
if (!email) {
  console.log('Usage: node activate_user.js user@example.com');
  console.log('\nExamples:');
  console.log('  node activate_user.js admin@company.com');
  console.log('  node activate_user.js employee@company.com');
  console.log('\nThis script sets isActive = true in the /users/ collection,');
  console.log('which is required by Firestore rules for data access.');
  process.exit(1);
}

activateUser(email)
  .then(() => process.exit(0))
  .catch(err => {
    console.error('Fatal error:', err);
    process.exit(1);
  });
