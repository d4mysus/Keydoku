plugins {
    id("com.android.application")
}

val signingVariables = listOf("KEYDOKU_KEYSTORE", "KEYDOKU_STORE_PASSWORD", "KEYDOKU_KEY_ALIAS", "KEYDOKU_KEY_PASSWORD")
val signingValues = signingVariables.associateWith { providers.environmentVariable(it).orNull }
val hasSigning = signingValues.values.all { !it.isNullOrBlank() }
require(signingValues.values.all { it.isNullOrBlank() } || hasSigning) {
    "Release signing requires all four KEYDOKU signing environment variables."
}

android {
    namespace = "com.keydoku"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.keydoku"
        minSdk = 23
        targetSdk = 37
        versionCode = 4
        versionName = "0.1.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasSigning) {
            create("distribution") {
                storeFile = file(signingValues.getValue("KEYDOKU_KEYSTORE")!!)
                storePassword = signingValues.getValue("KEYDOKU_STORE_PASSWORD")
                keyAlias = signingValues.getValue("KEYDOKU_KEY_ALIAS")
                keyPassword = signingValues.getValue("KEYDOKU_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasSigning) signingConfig = signingConfigs.getByName("distribution")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

}

dependencies {
    implementation("com.google.zxing:core:3.5.3")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test:core:1.6.1")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
