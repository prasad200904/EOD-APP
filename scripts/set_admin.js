/**
 * WorkCore - Set Admin Role Script
 * 
 * Sets a user as ADMIN in Firestore and Firebase Auth custom claims.
 * 
 * Usage:
 *   GOOGLE_APPLICATION_CREDENTIALS=./serviceAccountKey.json node set_admin.js <email>
 * 
 * Or on Windows:
 *   set GOOGLE_APPLICATION_CREDENTIALS=serviceAccountKey.json
 *   node set_admin.js <email>
 * 
 * Example:
 *   node set_admin.js admin@workcore.com
 */

const admin = require('firebase-admin');
const path = require('path');

// Get email from command line
const email = process.argv[2];

if (!email) {
  console.error('❌ ERROR: Email address required');
  console.error('\nUsage: node set_admin.js <email>');
  console.error('Example: node set_admin.js admin@workcore.com\n');
  process.exit(1);
}

// Validate email format
const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
if (!emailRegex.test(email)) {
  console.error(`❌ ERROR: Invalid email format: ${email}\n`);
  process.exit(1);
}

console.log('====================================================');
console.log('👑 WORKCORE SET ADMIN ROLE');
console.log('====================================================\n');
console.log(`Target email: ${email}\n`);

// Initialize Firebase Admin
const keyPath = process.env.GOOGLE_APPLICATION_CREDENTIALS || path.join(__dirname, 'serviceAccountKey.json');

try {
  const serviceAccount = require(keyPath);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
  console.log(`✅ Connected to Firebase project: ${serviceAccount.project_id}\n`);
} catch (err) {
  console.error(`❌ Failed to initialize Firebase Admin:`);
  console.error(`   ${err.message}`);
  console.error(`\nMake sure serviceAccountKey.json exists or set GOOGLE_APPLICATION_CREDENTIALS\n`);
  process.exit(1);
}

const db = admin.firestore();
const auth = admin.auth();

async function setAdmin() {
  try {
    // 1. Find user by email in Firebase Auth
    console.log('🔍 Looking up user in Firebase Auth...');
    let userRecord;
    try {
      userRecord = await auth.getUserByEmail(email);
      console.log(`   ✅ Found user: ${userRecord.uid}\n`);
    } catch (err) {
      if (err.code === 'auth/user-not-found') {
        console.error(`❌ ERROR: No user found with email: ${email}`);
        console.error(`   User must sign up in the app first.\n`);
      } else {
        throw err;
      }
      process.exit(1);
    }

    const uid = userRecord.uid;

    // 2. Update Firestore /users/{uid} document
    console.log('📝 Updating Firestore /users document...');
    const userDocRef = db.collection('users').doc(uid);
    const userDoc = await userDocRef.get();

    if (!userDoc.exists) {
      console.error(`❌ ERROR: User document /users/${uid} does not exist`);
      console.error(`   User must log in to the app at least once first.\n`);
      process.exit(1);
    }

    await userDocRef.update({
      role: 'ADMIN',
      isActive: true,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    });
    console.log(`   ✅ Set role=ADMIN in /users/${uid}\n`);

    // 3. Update custom claims in Firebase Auth
    console.log('🔐 Setting Firebase Auth custom claims...');
    await auth.setCustomUserClaims(uid, { admin: true, role: 'ADMIN' });
    console.log(`   ✅ Set custom claim: admin=true\n`);

    // 4. Update employees collection if employee record exists
    console.log('👤 Checking /employees collection...');
    const userData = userDoc.data();
    if (userData.employeeId) {
      const employeeQuery = await db.collection('employees')
        .where('employeeId', '==', userData.employeeId)
        .limit(1)
        .get();

      if (!employeeQuery.empty) {
        const employeeDoc = employeeQuery.docs[0];
        await employeeDoc.ref.update({
          role: 'ADMIN',
          isActive: true
        });
        console.log(`   ✅ Updated /employees/${employeeDoc.id} (${userData.employeeId})\n`);
      } else {
        console.log(`   ℹ️  No employee record found for ${userData.employeeId}\n`);
      }
    } else {
      console.log(`   ℹ️  No employeeId in user document\n`);
    }

    // Success summary
    console.log('====================================================');
    console.log('✅ SUCCESS - ADMIN ROLE SET');
    console.log('====================================================');
    console.log(`Email         : ${email}`);
    console.log(`UID           : ${uid}`);
    console.log(`Role          : ADMIN`);
    console.log(`Custom Claims : { admin: true, role: "ADMIN" }`);
    console.log('====================================================\n');

    console.log('⚠️  IMPORTANT: User must sign out and sign back in for custom claims to take effect.\n');

  } catch (error) {
    console.error('❌ ERROR:', error.message);
    if (error.code) {
      console.error(`   Code: ${error.code}`);
    }
    console.error('');
    process.exit(1);
  }
}

// Run the script
setAdmin()
  .then(() => process.exit(0))
  .catch(err => {
    console.error('Unexpected error:', err);
    process.exit(1);
  });
