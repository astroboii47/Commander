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
        minSdk = 29
        targetSdk = 36
        versionCode = 10
        versionName = "0.1.0-alpha.7"
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
            // Keep cold Bar startup small. Use -Pcommander.minify=false only
            // when diagnosing an optimizer-specific regression.
            isMinifyEnabled = providers.gradleProperty("commander.minify").orNull != "false"
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
    lint {
        // This reflection runs only inside the explicitly authorised Shizuku
        // shell user service used for system task switching and snapshots.
        disable += "BlockedPrivateApi"
    }
    buildFeatures {
        compose = true
        aidl = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2025.12.01"))
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.browser:browser:1.9.0")
    // Installs the packaged Baseline Profile after sideloaded/local installs,
    // so Commander receives the same ahead-of-time startup optimization it
    // would receive from Play-delivered dex metadata.
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("br.com.devsrsouza.compose.icons:tabler-icons:1.1.1")
    implementation("com.woowla.compose.icon.collections:remix:4.9.0")
    implementation("io.github.ardasoyturk.compose.icons:lucide:2.0.6")
    implementation("com.adamglin:phosphor-icon:1.0.0")
    implementation("dev.rikka.shizuku:api:13.1.5")
    implementation("dev.rikka.shizuku:provider:13.1.5")
    implementation("org.lsposed.hiddenapibypass:hiddenapibypass:4.3")
    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
