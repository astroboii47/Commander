import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val releaseKeystorePropertiesFile = rootProject.file("keystore.properties")
val releaseKeystoreProperties = Properties().apply {
    if (releaseKeystorePropertiesFile.exists()) {
        releaseKeystorePropertiesFile.inputStream().use(::load)
    }
}
val releaseStoreFile = providers.environmentVariable("COMMANDER_STORE_FILE").orNull
    ?: releaseKeystoreProperties.getProperty("storeFile")
val releaseStorePassword = providers.environmentVariable("COMMANDER_STORE_PASSWORD").orNull
    ?: releaseKeystoreProperties.getProperty("storePassword")
val releaseKeyAlias = providers.environmentVariable("COMMANDER_KEY_ALIAS").orNull
    ?: releaseKeystoreProperties.getProperty("keyAlias")
val releaseKeyPassword = providers.environmentVariable("COMMANDER_KEY_PASSWORD").orNull
    ?: releaseKeystoreProperties.getProperty("keyPassword")
val hasReleaseSigning = listOf(releaseStoreFile, releaseStorePassword, releaseKeyAlias, releaseKeyPassword)
    .all { !it.isNullOrBlank() }

android {
    namespace = "com.astroboii47.commander"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.astroboii47.commander"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "0.1.0-alpha.3.1"
    }

    signingConfigs {
        create("release") {
            if (hasReleaseSigning) {
                storeFile = rootProject.file(releaseStoreFile!!)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions { jvmTarget = "11" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
