/**
 * WorkCore - Version Bump Script
 * 
 * Manually bump version numbers
 * 
 * Usage:
 *   node bump_version.js <type> [--notes "Release notes"]
 * 
 * Types:
 *   major  - Bump major version (1.2.3 -> 2.0.0)
 *   minor  - Bump minor version (1.2.3 -> 1.3.0)
 *   patch  - Bump patch version (1.2.3 -> 1.2.4)
 *   code   - Bump version code only
 */

const fs = require('fs');
const path = require('path');

const args = process.argv.slice(2);
const type = args[0];
const notesIndex = args.indexOf('--notes');
const releaseNotes = notesIndex !== -1 && notesIndex + 1 < args.length 
  ? args[notesIndex + 1] 
  : null;

console.log('====================================================');
console.log('🔢 WORKCORE VERSION BUMP UTILITY');
console.log('====================================================\n');

if (!type || !['major', 'minor', 'patch', 'code'].includes(type)) {
  console.error('❌ ERROR: Invalid or missing bump type!');
  console.error('\nUsage: node bump_version.js <type> [--notes "Release notes"]');
  console.error('\nTypes:');
  console.error('  major  - Bump major version (1.2.3 -> 2.0.0)');
  console.error('  minor  - Bump minor version (1.2.3 -> 1.3.0)');
  console.error('  patch  - Bump patch version (1.2.3 -> 1.2.4)');
  console.error('  code   - Bump version code only\n');
  process.exit(1);
}

// Read version.properties
const versionPropsPath = path.join(__dirname, '..', 'version.properties');
if (!fs.existsSync(versionPropsPath)) {
  console.error('❌ ERROR: version.properties not found!');
  process.exit(1);
}

// Parse properties file
const versionProps = {};
const propsContent = fs.readFileSync(versionPropsPath, 'utf8');
const lines = propsContent.split('\n');

lines.forEach(line => {
  line = line.trim();
  if (line && !line.startsWith('#')) {
    const [key, ...valueParts] = line.split('=');
    versionProps[key.trim()] = valueParts.join('=').trim();
  }
});

let major = parseInt(versionProps['VERSION_MAJOR'] || '1', 10);
let minor = parseInt(versionProps['VERSION_MINOR'] || '2', 10);
let patch = parseInt(versionProps['VERSION_PATCH'] || '0', 10);
let code = parseInt(versionProps['VERSION_CODE'] || '3', 10);

const oldVersion = `${major}.${minor}.${patch} (${code})`;

// Bump version based on type
switch (type) {
  case 'major':
    major++;
    minor = 0;
    patch = 0;
    code++;
    break;
  case 'minor':
    minor++;
    patch = 0;
    code++;
    break;
  case 'patch':
    patch++;
    code++;
    break;
  case 'code':
    code++;
    break;
}

const newVersion = `${major}.${minor}.${patch} (${code})`;

console.log('📊 Version Bump Summary:');
console.log(`   Type        : ${type.toUpperCase()}`);
console.log(`   Old Version : ${oldVersion}`);
console.log(`   New Version : ${newVersion}\n`);

// Update properties
versionProps['VERSION_MAJOR'] = major.toString();
versionProps['VERSION_MINOR'] = minor.toString();
versionProps['VERSION_PATCH'] = patch.toString();
versionProps['VERSION_CODE'] = code.toString();
versionProps['LAST_BUILD_TIME'] = new Date().toISOString();

if (releaseNotes) {
  versionProps['RELEASE_NOTES'] = releaseNotes;
  console.log(`   Release Notes: ${releaseNotes}\n`);
}

// Write back to file
let newContent = '# WorkCore Version Management\n';
newContent += '# This file is automatically updated on each build\n\n';
newContent += '# Major.Minor.Patch version (e.g., 1.2.5)\n';
newContent += `VERSION_MAJOR=${versionProps['VERSION_MAJOR']}\n`;
newContent += `VERSION_MINOR=${versionProps['VERSION_MINOR']}\n`;
newContent += `VERSION_PATCH=${versionProps['VERSION_PATCH']}\n\n`;
newContent += '# Build number (auto-incremented)\n';
newContent += `VERSION_CODE=${versionProps['VERSION_CODE']}\n\n`;
newContent += '# Last build timestamp\n';
newContent += `LAST_BUILD_TIME=${versionProps['LAST_BUILD_TIME']}\n\n`;
newContent += '# Release notes for next version\n';
newContent += `RELEASE_NOTES=${versionProps['RELEASE_NOTES'] || 'Bug fixes and performance improvements.'}\n`;

fs.writeFileSync(versionPropsPath, newContent, 'utf8');

console.log('✅ Version updated successfully!');
console.log(`   File: ${versionPropsPath}\n`);

console.log('====================================================');
console.log('🎉 BUMP COMPLETE');
console.log('====================================================\n');

console.log('💡 Next steps:');
console.log('   1. Build a release APK: ./gradlew assembleRelease');
console.log('   2. Upload APK to your server');
console.log('   3. Publish to Firestore: node publish_version.js --download-url "https://..."');
console.log('');
