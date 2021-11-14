import java.util.Properties

plugins {
    id("com.android.application")
    id("com.adarshr.test-logger") version "3.0.0" // https://github.com/radarsh/gradle-test-logger-plugin
    id("kotlin-android")
}

testlogger {
    theme = com.adarshr.gradle.testlogger.theme.ThemeType.STANDARD
    showExceptions = true
    showStackTraces = true
    showFullStackTraces = false
    showCauses = true
    slowThreshold = 2000
    showSummary = true
    showSimpleNames = false
    showPassed = true
    showSkipped = true
    showFailed = true
    showStandardStreams = true
    showPassedStandardStreams = true
    showSkippedStandardStreams = true
    showFailedStandardStreams = true
}

fun File.readProperty(propertyName: String): String = bufferedReader().use { reader ->
    Properties().apply { load(reader) }.getProperty(propertyName)
}

android {
    compileSdk = 31

    defaultConfig {
        applicationId = "bobko.todomail"
        minSdk = 22
        targetSdk = 31
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    val secretsDir = rootDir.resolve("todomail-secrets")
    val releaseSigningConfig = if (secretsDir.isDirectory) {
        logger.lifecycle("Release signing is enabled")
        signingConfigs.create("release") {
            keyAlias = secretsDir.resolve("jks.properties").readProperty("keyAlias")
            keyPassword = secretsDir.resolve("jks.properties").readProperty("keyPassword")
            storeFile = secretsDir.resolve("todomail.jks")
            storePassword = secretsDir.resolve("jks.properties").readProperty("storePassword")
        }
    } else {
        logger.lifecycle("Release signing is disabled")
        null
    }
    buildTypes {
        release {
            signingConfig = releaseSigningConfig
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            signingConfig = releaseSigningConfig
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_1_8)
        targetCompatibility(JavaVersion.VERSION_1_8)
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = rootProject.extra["compose_version"] as String
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/NOTICE.md"
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/DEPENDENCIES"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.31")

    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.compose.ui:ui:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.material:material:${rootProject.extra["compose_version"]}")
    implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["compose_version"]}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.activity:activity-compose:1.4.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.1.0-beta02")
    implementation("androidx.preference:preference-ktx:1.1.1")

    // Gmail API
//    implementation("com.google.api-client:google-api-client:1.25.0")
//    implementation("com.google.oauth-client:google-oauth-client-jetty:1.23.0")
//    implementation("com.google.apis:google-api-services-gmail:v1-rev83-1.23.0")

    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
    implementation("com.github.kittinunf.fuel:fuel-jackson:2.3.1")

    // Login with Google
    implementation("com.google.android.gms:play-services-auth:19.2.0")

    // https://stackoverflow.com/a/60492942
//    implementation("com.google.guava:guava:27.0.1-android")
//    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")

    // JetPack navigation
    implementation("androidx.navigation:navigation-fragment-ktx:${rootProject.extra["nav_version"]}")
    implementation("androidx.navigation:navigation-ui-ktx:${rootProject.extra["nav_version"]}")

    implementation("com.google.accompanist:accompanist-systemuicontroller:0.18.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core-ktx:1.4.1-alpha03")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}")
    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")

    // https://mvnrepository.com/artifact/com.sun.mail/android-mail/1.6.7
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")
}
