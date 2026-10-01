/**
 * WorkCore - Create/Seed app_config/version_info in Firestore
 * 
 * Usage:
 *   Dry-Run: node create_update_config.js
 *   Execute: node create_update_config.js --execute
 */

const fs = require('fs');
const path = require('path');

const args = process.argv.slice(2);
const isExecuteMode = args.includes('--execute') || args.includes('--live');

console.log('====================================================');
console.log(`🚀 WORKCORE FIRESTORE VERSION CONFIG SEEDER`);
console.log(`MODE: ${isExecuteMode ? '⚡ EXECUTE (Writing to Firestore)' : '🔍 DRY-RUN (Preview)'}`);
console.log('====================================================\n');

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

const updateData = {
  versionCode: 3, // Must be > 2 to trigger update popup on current build (v2)
  versionName: "1.2.0",
  downloadUrl: "https://example.com/workcore-v1.2.0.apk",
  releaseNotes: "• Added automatic in-app update system\n• Fixed EOD History dashboard permissions\n• Performance & UI enhancements",
  isMandatory: false,
  minRequiredVersion: 1,
  updatedAt: new Date().toISOString()
};

console.log('📄 Document to be written: app_config/version_info');
console.log(JSON.stringify(updateData, null, 2));
console.log('');

if (!isExecuteMode) {
  console.log('💡 TIP: To write this document to your DEV Firestore database, run:');
  console.log('   node create_update_config.js --execute\n');
  process.exit(0);
}

if (!serviceAccountPath) {
  console.error(`❌ Service account key '${keyFilename}' not found in scripts/ folder.`);
  console.error(`Please place your Firebase serviceAccountKey.json file in the scripts/ folder.`);
  process.exit(1);
}

const admin = require('firebase-admin');
const serviceAccount = require(serviceAccountPath);

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function seedUpdateConfig() {
  await db.collection('app_config').doc('version_info').set(updateData, { merge: true });
  console.log('✅ Successfully created app_config/version_info document in Firestore!');
}

seedUpdateConfig().catch(err => {
  console.error('❌ Error writing to Firestore:', err);
  process.exit(1);
});
