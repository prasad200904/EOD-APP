# WorkCore Release Script (PowerShell)
# Run this from the workcore folder: .\release.ps1

$ROOT = $PSScriptRoot

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "   WorkCore Release Script" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# ── Step 1: Read version.properties ─────────────
$versionFile = "$ROOT\version.properties"
$props = @{}
Get-Content $versionFile | Where-Object { $_ -match "^[^#]" -and $_ -match "=" } | ForEach-Object {
    $parts = $_ -split "=", 2
    $props[$parts[0].Trim()] = $parts[1].Trim()
}

$MAJOR = [int]$props["VERSION_MAJOR"]
$MINOR = [int]$props["VERSION_MINOR"]
$PATCH = [int]$props["VERSION_PATCH"]
$CODE  = [int]$props["VERSION_CODE"]

Write-Host "Current version: $MAJOR.$MINOR.$PATCH (code $CODE)" -ForegroundColor Yellow
Write-Host ""

# ── Step 2: Ask what changed ─────────────────────
$NOTES = Read-Host "What changed in this release?"

# ── Step 3: Bump version ─────────────────────────
Write-Host ""
Write-Host "Bump version?" -ForegroundColor Yellow
Write-Host "  1 = patch  ($MAJOR.$MINOR.$PATCH -> $MAJOR.$MINOR.$($PATCH+1))  <- bug fix / small change"
Write-Host "  2 = minor  ($MAJOR.$MINOR.$PATCH -> $MAJOR.$($MINOR+1).0)       <- new feature"
Write-Host "  3 = major  ($MAJOR.$MINOR.$PATCH -> $($MAJOR+1).0.0)            <- big update"
Write-Host "  ENTER = keep same version"
$BUMP = Read-Host "Choose"

switch ($BUMP) {
    "1" { $PATCH = $PATCH + 1 }
    "2" { $MINOR = $MINOR + 1; $PATCH = 0 }
    "3" { $MAJOR = $MAJOR + 1; $MINOR = 0; $PATCH = 0 }
}
$CODE = $CODE + 1
$VERSION = "$MAJOR.$MINOR.$PATCH"
$TAG = "v$VERSION"

Write-Host ""
Write-Host "New version: $VERSION (code $CODE)" -ForegroundColor Green
Write-Host "Tag: $TAG" -ForegroundColor Green
Write-Host ""

# ── Step 4: Update version.properties ────────────
$newContent = @"
# WorkCore Version Management
VERSION_MAJOR=$MAJOR
VERSION_MINOR=$MINOR
VERSION_PATCH=$PATCH
VERSION_CODE=$CODE
RELEASE_NOTES=$NOTES
"@
Set-Content -Path $versionFile -Value $newContent -Encoding UTF8
Write-Host "[OK] version.properties updated" -ForegroundColor Green

# ── Step 4b: Update CHANGELOG.md ─────────────────
$changelogFile = "$ROOT\CHANGELOG.md"
if (Test-Path $changelogFile) {
    $today = (Get-Date).ToString("yyyy-MM-dd")
    $changelogContent = Get-Content $changelogFile -Raw -Encoding UTF8
    $entry = "`n## [$VERSION] - $today`n`n**Version Code:** ``$CODE```  `n`n### Changes`n- $NOTES`n"
    if ($changelogContent -match "## \[Unreleased\]") {
        $changelogContent = $changelogContent -replace "## \[Unreleased\]", "## [Unreleased]`n$entry"
        Set-Content -Path $changelogFile -Value $changelogContent -Encoding UTF8
        Write-Host "[OK] CHANGELOG.md updated with version $VERSION" -ForegroundColor Green
    }
}

# ── Step 5: Check APK ────────────────────────────
$APK = "$ROOT\app\release\app-release.apk"
if (-not (Test-Path $APK)) {
    Write-Host ""
    Write-Host "[ERROR] APK not found!" -ForegroundColor Red
    Write-Host ""
    Write-Host "Build it first in Android Studio:" -ForegroundColor Yellow
    Write-Host "  Build > Generate Signed Bundle / APK > APK > release > Finish"
    Write-Host ""
    Write-Host "Then run this script again." -ForegroundColor Yellow
    Read-Host "Press Enter to exit"
    exit 1
}

# ── Step 6: Copy APK with version name ───────────
$NAMED_APK = "$ROOT\app\release\WorkCore-$VERSION.apk"
Copy-Item $APK $NAMED_APK -Force
Write-Host "[OK] APK ready: WorkCore-$VERSION.apk" -ForegroundColor Green

# ── Step 7: Git commit and push ──────────────────
Write-Host ""
Write-Host "Pushing to GitHub..." -ForegroundColor Cyan
Set-Location $ROOT

# Always release from main branch
$currentBranch = git rev-parse --abbrev-ref HEAD
if ($currentBranch -ne "main") {
  Write-Host "You are on branch '$currentBranch'." -ForegroundColor Yellow
  Write-Host "Merging dev into main for release..." -ForegroundColor Cyan
  git add version.properties CHANGELOG.md
  git commit -m "release: WorkCore $VERSION (code $CODE) - $NOTES"
  git checkout main
  git merge dev --no-ff -m "release: merge dev into main for WorkCore $VERSION"
  git push origin main
  git checkout dev
  git push origin dev
  Write-Host "[OK] Merged dev → main and pushed both branches" -ForegroundColor Green
} else {
  git add version.properties CHANGELOG.md
  git commit -m "release: WorkCore $VERSION (code $CODE) - $NOTES"
  git push origin main
  Write-Host "[OK] Pushed to main" -ForegroundColor Green
}

# ── Step 8: GitHub Release ────────────────────────
$ghInstalled = Get-Command gh -ErrorAction SilentlyContinue
if ($ghInstalled) {
    Write-Host ""
    Write-Host "Creating GitHub release..." -ForegroundColor Cyan
    $releaseNotes = @"
## WorkCore $VERSION

### Changes
$NOTES

### How to install
1. Download **WorkCore-$VERSION.apk** below
2. On your phone: Settings > Security > Enable Unknown sources
3. Open the APK > Install

### Version Info
- Version: $VERSION
- Build Code: $CODE
"@
    gh release create $TAG $NAMED_APK `
        --title "WorkCore $VERSION" `
        --notes $releaseNotes
    Write-Host ""
    Write-Host "[OK] GitHub Release created!" -ForegroundColor Green
    Write-Host ""
    Write-Host "Download link:" -ForegroundColor Yellow
    Write-Host "https://github.com/prasad200904/EOD-APP/releases/download/$TAG/WorkCore-$VERSION.apk"
    Write-Host ""
    Write-Host "Latest release page (share this):" -ForegroundColor Yellow
    Write-Host "https://github.com/prasad200904/EOD-APP/releases/latest"
} else {
    Write-Host ""
    Write-Host "[!] GitHub CLI not installed - create release manually:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "1. Go to: https://github.com/prasad200904/EOD-APP/releases/new" -ForegroundColor Cyan
    Write-Host "2. Tag:    $TAG"
    Write-Host "3. Title:  WorkCore $VERSION"
    Write-Host "4. Upload: $NAMED_APK"
    Write-Host "5. Click Publish release"
}

# ── Step 9: Firestore reminder ────────────────────
$DOWNLOAD_URL = "https://github.com/prasad200904/EOD-APP/releases/download/$TAG/WorkCore-$VERSION.apk"
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  UPDATE FIRESTORE (for auto-update in app)" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Collection : app_config" -ForegroundColor Yellow
Write-Host "Document   : version_info" -ForegroundColor Yellow
Write-Host ""
Write-Host "Set these fields:" -ForegroundColor White
Write-Host "  versionCode  = $CODE" -ForegroundColor Green
Write-Host "  versionName  = `"$VERSION`"" -ForegroundColor Green
Write-Host "  downloadUrl  = `"$DOWNLOAD_URL`"" -ForegroundColor Green
Write-Host ""
Write-Host "Done! WorkCore $VERSION is ready." -ForegroundColor Green
Write-Host ""
Read-Host "Press Enter to exit"
