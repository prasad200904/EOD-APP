/**
 * WorkCore Cloud Functions
 * Provides server-side employee provisioning and activation management
 * Enforces token custom claims for role, employeeId, teamId, and companyId.
 */

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();
const db = admin.firestore();
const auth = admin.auth();

/**
 * Callable: provisionEmployee
 * Callable by ADMIN only. Creates Firebase Auth user without client password,
 * assigns custom claims, initializes /users/{uid} document, and generates a password set link.
 */
exports.provisionEmployee = functions.https.onCall(async (data, context) => {
  // 1. Verify caller has ADMIN role in token claims
  if (!context.auth || (!context.auth.token.admin && context.auth.token.role !== 'ADMIN')) {
    throw new functions.https.HttpsError(
      'permission-denied',
      'Only administrators can provision new employee accounts.'
    );
  }

  const { email, name, employeeId, teamId, companyId, role = 'EMPLOYEE', department = '' } = data;

  if (!email || !employeeId) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'email and employeeId are required fields.'
    );
  }

  try {
    let userRecord;
    let isNewUser = false;

    try {
      userRecord = await auth.getUserByEmail(email.trim());
    } catch (err) {
      if (err.code === 'auth/user-not-found') {
        // Create user without password
        userRecord = await auth.createUser({
          email: email.trim(),
          displayName: name || '',
          disabled: false
        });
        isNewUser = true;
      } else {
        throw err;
      }
    }

    const uid = userRecord.uid;
    const finalRole = (role || 'EMPLOYEE').toUpperCase();
    const isAdmin = finalRole === 'ADMIN';

    // 2. Set custom user claims (strictly authoritative)
    await auth.setCustomUserClaims(uid, {
      role: finalRole,
      employeeId: employeeId,
      teamId: teamId || '',
      companyId: companyId || '',
      admin: isAdmin
    });

    // 3. Update server-side profile /users/{uid}
    await db.collection('users').doc(uid).set({
      uid: uid,
      employeeId: employeeId,
      name: name || '',
      email: email.trim(),
      role: finalRole,
      teamId: teamId || '',
      companyId: companyId || '',
      department: department || '',
      isActive: true,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    // 4. Update /employees/{employeeId}
    await db.collection('employees').doc(employeeId).set({
      employeeId: employeeId,
      name: name || '',
      email: email.trim(),
      teamId: teamId || '',
      companyId: companyId || '',
      department: department || '',
      role: finalRole,
      isActive: true,
      updatedAt: admin.firestore.FieldValue.serverTimestamp()
    }, { merge: true });

    // 5. Generate password set link
    const resetLink = await auth.generatePasswordResetLink(email.trim());

    return {
      success: true,
      uid: uid,
      employeeId: employeeId,
      isNewUser: isNewUser,
      setupLink: resetLink
    };
  } catch (error) {
    throw new functions.https.HttpsError('internal', error.message || 'Provisioning failed');
  }
});

/**
 * Callable: setEmployeeActive
 * Callable by ADMIN only. Enables or disables the employee's auth account and Firestore records.
 */
exports.setEmployeeActive = functions.https.onCall(async (data, context) => {
  if (!context.auth || (!context.auth.token.admin && context.auth.token.role !== 'ADMIN')) {
    throw new functions.https.HttpsError(
      'permission-denied',
      'Only administrators can toggle employee active status.'
    );
  }

  const { employeeId, uid, isActive } = data;

  if (typeof isActive !== 'boolean' || (!employeeId && !uid)) {
    throw new functions.https.HttpsError(
      'invalid-argument',
      'isActive (boolean) and either employeeId or uid are required.'
    );
  }

  try {
    let resolvedUid = uid;

    if (!resolvedUid && employeeId) {
      const userSnap = await db.collection('users')
        .where('employeeId', '==', employeeId)
        .limit(1)
        .get();

      if (!userSnap.empty) {
        resolvedUid = userSnap.docs[0].id;
      }
    }

    if (resolvedUid) {
      await auth.updateUser(resolvedUid, { disabled: !isActive });
      await db.collection('users').doc(resolvedUid).set({
        isActive: isActive,
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      }, { merge: true });
    }

    if (employeeId) {
      await db.collection('employees').doc(employeeId).set({
        isActive: isActive,
        updatedAt: admin.firestore.FieldValue.serverTimestamp()
      }, { merge: true });
    }

    return {
      success: true,
      employeeId: employeeId,
      isActive: isActive
    };
  } catch (error) {
    throw new functions.https.HttpsError('internal', error.message || 'Active status update failed');
  }
});
