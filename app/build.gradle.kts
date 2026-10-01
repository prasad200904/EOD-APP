import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy
import java.util.Properties
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

// Load version properties safely
val versionPropsFile = file("${rootProject.projectDir}/version.properties")
val versionProps = Properties()

if (versionPropsFile.exists()) {
  versionProps.load(FileInputStream(versionPropsFile))
}

// Read version with safe defaults - renamed to avoid shadowing
val appVersionMajor = versionProps.getProperty("VERSION_MAJOR", "1").toIntOrNull() ?: 1
val appVersionMinor = versionProps.getProperty("VERSION_MINOR", "2").toIntOrNull() ?: 2
val appVersionPatch = versionProps.getProperty("VERSION_PATCH", "1").toIntOrNull() ?: 1
val appVersionCode = versionProps.getProperty("VERSION_CODE", "4").toIntOrNull() ?: 4
val appVersionName = "$appVersionMajor.$appVersionMinor.$appVersionPatch"
val appBuildTime = versionProps.getProperty("LAST_BUILD_TIME") 
  ?: SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(Date())

android {
  namespace = "com.example"
  compileSdk = 35  // Using stable SDK 35 instead of 36

  defaultConfig {
    applicationId = "com.aistudio.workcore.kpmz"
    minSdk = 24
    targetSdk = 35  // Match compileSdk
    versionCode = appVersionCode
    versionName = appVersionName

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    
    // Build time available in BuildConfig (VERSION_NAME and VERSION_CODE are auto-generated)
    buildConfigField("String", "BUILD_TIME", "\"$appBuildTime\"")
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      val keystore = file(keystorePath)
      
      // Only set signing config if file exists
      if (keystore.exists()) {
        storeFile = keystore
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      }
    }
    create("debugConfig") {
      val debugKeystore = file("${rootDir}/debug.keystore")
      if (debugKeystore.exists()) {
        storeFile = debugKeystore
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      
      // Only set signing config if keystore exists
      val releaseConfig = signingConfigs.getByName("release")
      if (releaseConfig.storeFile?.exists() == true) {
        signingConfig = releaseConfig
      } else {
        // Allow build without signing for local development
        println("⚠️  WARNING: Release keystore not found. APK will not be signed.")
      }
    }
    debug { 
      val debugConfig = signingConfigs.getByName("debugConfig")
      if (debugConfig.storeFile?.exists() == true) {
        signingConfig = debugConfig
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.auth)
  implementation(libs.firebase.appcheck.recaptcha)
  implementation(libs.firebase.appcheck.debug)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
