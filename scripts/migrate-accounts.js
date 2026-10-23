/**
 * WorkCore - Account Migration Script
 * 
 * Migrates all employee accounts to password-free authentication:
 * - Checks or creates Firebase Auth users for employees
 * - Sets strict authoritative custom claims (role, employeeId, teamId, companyId)
 * - Updates /users/{uid} profiles
 * - Cleanses any stored passwords/teamPasswords from Firestore
 * 
 * Safety:
 * - Dry-run mode by default! No changes are applied unless --apply flag is provided.
 * - Credentials, passwords, and private emails are never printed or logged.
 * 
 * Usage:
 *   node scripts/migrate-accounts.js          # Dry run (safe preview)
 *   node scripts/migrate-accounts.js --apply  # Execute changes
 */

const admin = require('firebase-admin');
const path = require('path');

const isApply = process.argv.includes('--apply');

console.log('====================================================');
console.log('🔒 WORKCORE AUTH MIGRATION TOOL');
console.log(`MODE: ${isApply ? '⚠️ APPLYING LIVE CHANGES' : '🛡️ DRY RUN (No changes will be written)'}`);
console.log('====================================================\n');

// Initialize Firebase Admin SDK
const keyPath = process.env.GOOGLE_APPLICATION_CREDENTIALS || path.join(__dirname, 'serviceAccountKey.json');

try {
  const serviceAccount = require(keyPath);
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
  console.log('✅ Connected to Firebase Admin SDK\n');
} catch (err) {
  console.error('❌ Failed to initialize Firebase Admin SDK.');
  console.error('Ensure GOOGLE_APPLICATION_CREDENTIALS points to a valid service account key.\n');
  process.exit(1);
}

const db = admin.firestore();
const auth = admin.auth();

async function runMigration() {
  const stats = {
    totalEmployees: 0,
    authAccountsExisting: 0,
    authAccountsCreated: 0,
    claimsUpdated: 0,
    userDocsUpdated: 0,
    passwordsScrubbed: 0,
    errors: 0
  };

  try {
    console.log('📋 Fetching employees from Firestore...');
    const employeesSnap = await db.collection('employees').get();
    stats.totalEmployees = employeesSnap.size;
    console.log(`Found ${stats.totalEmployees} employee records.\n`);

    for (const doc of employeesSnap.docs) {
      const emp = doc.data();
      const employeeId = emp.employeeId || doc.id;
      const email = emp.email ? emp.email.trim() : null;
      const role = (emp.role || 'EMPLOYEE').toUpperCase();
      const teamId = emp.teamId || emp.team || '';
      const companyId = emp.companyId || '';
      const isAdmin = role === 'ADMIN';

      if (!email) {
        console.warn(`[SKIP] Employee ${employeeId} has no email address.`);
        continue;
      }

      try {
        let userRecord;
        let isNew = false;

        try {
          userRecord = await auth.getUserByEmail(email);
          stats.authAccountsExisting++;
        } catch (err) {
          if (err.code === 'auth/user-not-found') {
            isNew = true;
            stats.authAccountsCreated++;
          } else {
            throw err;
          }
        }

        if (isApply) {
          if (isNew) {
            userRecord = await auth.createUser({
              email: email,
              displayName: emp.name || '',
              disabled: emp.isActive === false
            });
          }

          const uid = userRecord.uid;

          // Set custom claims
          await auth.setCustomUserClaims(uid, {
            role: role,
            employeeId: employeeId,
            teamId: teamId,
            companyId: companyId,
            admin: isAdmin
          });
          stats.claimsUpdated++;

          // Create/update user document (without password)
          await db.collection('users').doc(uid).set({
            uid: uid,
            employeeId: employeeId,
            name: emp.name || '',
            role: role,
            teamId: teamId,
            companyId: companyId,
            isActive: emp.isActive !== false,
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          }, { merge: true });
          stats.userDocsUpdated++;

          // Cleanse passwords in employees collection doc
          if (emp.password !== undefined || emp.teamPassword !== undefined) {
            await doc.ref.update({
              password: admin.firestore.FieldValue.delete(),
              teamPassword: admin.firestore.FieldValue.delete(),
              updatedAt: admin.firestore.FieldValue.serverTimestamp()
            });
            stats.passwordsScrubbed++;
          }
        } else {
          // Dry run count simulations
          stats.claimsUpdated++;
          stats.userDocsUpdated++;
          if (emp.password !== undefined || emp.teamPassword !== undefined) {
            stats.passwordsScrubbed++;
          }
        }
      } catch (err) {
        stats.errors++;
        console.error(`[ERROR] Processing employee ${employeeId}: ${err.message}`);
      }
    }

    // Cleanse teamPassword in teams collection
    console.log('\n📋 Checking teams collection for legacy passwords...');
    const teamsSnap = await db.collection('teams').get();
    for (const teamDoc of teamsSnap.docs) {
      const teamData = teamDoc.data();
      if (teamData.teamPassword !== undefined) {
        if (isApply) {
          await teamDoc.ref.update({
            teamPassword: admin.firestore.FieldValue.delete(),
            updatedAt: admin.firestore.FieldValue.serverTimestamp()
          });
        }
        stats.passwordsScrubbed++;
      }
    }

    console.log('\n====================================================');
    console.log('📊 MIGRATION SUMMARY REPORT');
    console.log('====================================================');
    console.log(`Total employees processed:    ${stats.totalEmployees}`);
    console.log(`Auth accounts existing:      ${stats.authAccountsExisting}`);
    console.log(`Auth accounts to create:     ${stats.authAccountsCreated}`);
    console.log(`Claims updated:              ${stats.claimsUpdated}`);
    console.log(`User profiles configured:    ${stats.userDocsUpdated}`);
    console.log(`Legacy passwords scrubbed:   ${stats.passwordsScrubbed}`);
    console.log(`Errors encountered:          ${stats.errors}`);
    console.log('====================================================\n');

    if (!isApply) {
      console.log('💡 Dry run completed safely. No changes were written.');
      console.log('To apply these changes, run with the --apply flag:');
      console.log('  node scripts/migrate-accounts.js --apply\n');
    } else {
      console.log('✅ Live migration completed successfully.\n');
    }
  } catch (error) {
    console.error('Fatal migration error:', error);
    process.exit(1);
  }
}

runMigration();
