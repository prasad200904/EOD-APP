/**
 * WorkCore - Publish Version to Firestore
 * 
 * This script publishes the current app version info to Firestore
 * so the app can check for updates.
 * 
 * Usage:
 *   node publish_version.js --download-url "https://your-url.com/app.apk" [--mandatory]
 * 
 * Options:
 *   --download-url <url>  : URL where users can download the APK (required)
 *   --mandatory           : Mark this update as mandatory
 *   --min-version <code>  : Minimum version code required (forces update for older versions)
 *   --notes "<text>"      : Custom release notes
 */

const fs = require('fs');
const path = require('path');

// Parse command line arguments
const args = process.argv.slice(2);
function getArg(name) {
  const index = args.indexOf(name);
  return index !== -1 && index + 1 < args.length ? args[index + 1] : null;
}
const hasFlag = (name) => args.includes(name);

const downloadUrl = getArg('--download-url') || getArg('--url');
const isMandatory = hasFlag('--mandatory');
const minVersion = getArg('--min-version') || null;
const customNotes = getArg('--notes') || null;

console.log('====================================================');
console.log('📦 WORKCORE VERSION PUBLISHER');
console.log('====================================================\n');

// Validate required arguments
if (!downloadUrl) {
  console.error('❌ ERROR: Download URL is required!');
  console.error('Usage: node publish_version.js --download-url "https://example.com/app.apk"');
  console.error('\nOptions:');
  console.error('  --download-url <url>  : URL where APK can be downloaded (REQUIRED)');
  console.error('  --mandatory           : Mark update as mandatory');
  console.error('  --min-version <code>  : Minimum version code required');
  console.error('  --notes "<text>"      : Custom release notes\n');
  process.exit(1);
}

// Read version from version.properties
const versionPropsPath = path.join(__dirname, '..', 'version.properties');
if (!fs.existsSync(versionPropsPath)) {
  console.error('❌ ERROR: version.properties not found!');
  console.error(`Expected location: ${versionPropsPath}\n`);
  process.exit(1);
}

// Parse properties file
const versionProps = {};
const propsContent = fs.readFileSync(versionPropsPath, 'utf8');
propsContent.split('\n').forEach(line => {
  line = line.trim();
  if (line && !line.startsWith('#')) {
    const [key, ...valueParts] = line.split('=');
    versionProps[key.trim()] = valueParts.join('=').trim();
  }
});

const versionMajor = versionProps['VERSION_MAJOR'] || '1';
const versionMinor = versionProps['VERSION_MINOR'] || '2';
const versionPatch = versionProps['VERSION_PATCH'] || '0';
const versionCode = parseInt(versionProps['VERSION_CODE'] || '3', 10);
const versionName = `${versionMajor}.${versionMinor}.${versionPatch}`;
const releaseNotes = customNotes || versionProps['RELEASE_NOTES'] || 'Bug fixes and performance improvements.';
const buildTime = versionProps['LAST_BUILD_TIME'] || new Date().toISOString();

console.log('📋 Version Information:');
console.log(`   Version Name    : ${versionName}`);
console.log(`   Version Code    : ${versionCode}`);
console.log(`   Download URL    : ${downloadUrl}`);
console.log(`   Mandatory       : ${isMandatory ? 'YES' : 'NO'}`);
console.log(`   Min Required    : ${minVersion || versionCode}`);
console.log(`   Release Notes   : ${releaseNotes}`);
console.log(`   Build Time      : ${buildTime}\n`);

// Locate Service Account Key
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

if (!serviceAccountPath) {
  console.error(`❌ ERROR: Service account key '${keyFilename}' not found!`);
  console.error(`Please place your Firebase service account key in:`);
  console.error(`  ${path.join(__dirname, keyFilename)}\n`);
  process.exit(1);
}

// Initialize Firebase Admin SDK
let admin, db;
try {
  admin = require('firebase-admin');
  const serviceAccount = require(serviceAccountPath);
  
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });
  
  db = admin.firestore();
  console.log(`✅ Connected to Firebase Project: [${serviceAccount.project_id}]\n`);
} catch (err) {
  console.error(`❌ Failed to initialize Firebase: ${err.message}`);
  process.exit(1);
}

// Publish version to Firestore
async function publishVersion() {
  try {
    const versionData = {
      versionCode: versionCode,
      versionName: versionName,
      downloadUrl: downloadUrl,
      releaseNotes: releaseNotes,
      isMandatory: isMandatory,
      minRequiredVersion: minVersion ? parseInt(minVersion, 10) : versionCode,
      publishedAt: admin.firestore.Timestamp.now(),
      buildTime: buildTime,
      platform: 'android'
    };

    // Update the main version document
    await db.collection('app_config').doc('version_info').set(versionData, { merge: false });
    
    console.log('✅ Version published to Firestore successfully!');
    console.log('   Collection: app_config');
    console.log('   Document  : version_info\n');

    // Also store in version history
    await db.collection('version_history').add({
      ...versionData,
      archivedAt: admin.firestore.Timestamp.now()
    });
    
    console.log('✅ Version archived in version_history collection\n');

    console.log('====================================================');
    console.log('🎉 PUBLISH COMPLETE');
    console.log('====================================================');
    console.log(`\nUsers with version < ${versionCode} will be notified of the update.`);
    
    if (isMandatory) {
      console.log('⚠️  This is a MANDATORY update - users must install it.');
    }
    
    console.log('\n💡 Next steps:');
    console.log('   1. Upload your APK to the download URL');
    console.log('   2. Test the update flow on a device');
    console.log('   3. Monitor Firestore for update analytics\n');

    process.exit(0);
  } catch (error) {
    console.error('❌ Error publishing version:', error.message);
    process.exit(1);
  }
}

// Run the publisher
publishVersion();
