import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.plugin.serialization)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "de.mm20.launcher2.ai"
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }
}

dependencies {
    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.ktor)
    implementation(libs.koin.android)
    implementation(libs.androidx.securitycrypto)
    implementation(project(":core:base"))
    implementation(project(":core:preferences"))
    implementation(project(":core:ktx"))
    implementation(project(":core:crashreporter"))
}
