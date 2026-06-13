import java.util.Properties
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlinx.kover") version "0.9.8"
    jacoco
}

dependencyLocking {
    lockAllConfigurations()
}

jacoco {
    toolVersion = "0.8.11"
}

android {
    namespace = "ec.cityalerta.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "ec.cityalerta.app"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        val properties = Properties()

        val localProperties = rootProject.file("local.properties")
        if (localProperties.exists()){
            localProperties.inputStream().use {properties.load(it)}
        }


        val supabaseUrl = providers.gradleProperty("SUPABASE_URL")
            .orElse(properties.getProperty("SUPABASE_URL") ?: "")
            .getOrElse("")



        val supabaseKey = providers.gradleProperty("SUPABASE_ANON_KEY")
            .orElse(properties.getProperty("SUPABASE_ANON_KEY") ?: "")
            .getOrElse("")

        val storageBaseUrl = providers.gradleProperty("STORAGE_BASE_URL")
            .orElse(properties.getProperty("STORAGE_BASE_URL") ?: "")
            .getOrElse("")

        val googleMapsApiKey = providers.gradleProperty("GOOGLE_MAPS_API_KEY")
            .orElse(properties.getProperty("GOOGLE_MAPS_API_KEY") ?: "")
            .getOrElse("")

        val passwordResetSecret = providers.gradleProperty("PASSWORD_RESET_SECRET")
            .orElse(properties.getProperty("PASSWORD_RESET_SECRET") ?: "")
            .getOrElse("")

        if (supabaseUrl.isEmpty() || supabaseKey.isEmpty()) {
            project.logger.warn("WARNING: Missing Supabase config. Define SUPABASE_URL and SUPABASE_ANON_KEY in local.properties or gradle.properties. Build tasks that require Supabase will fail.")
        }

        if (googleMapsApiKey.isEmpty()) {
            project.logger.warn("WARNING: Missing Google Maps API Key. Define GOOGLE_MAPS_API_KEY in local.properties or gradle.properties. Maps will not display.")
        }

        if (passwordResetSecret.isEmpty()) {
            project.logger.warn("WARNING: Missing PASSWORD_RESET_SECRET. Define PASSWORD_RESET_SECRET in local.properties or gradle.properties to use the password reset edge function.")
        }

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
        buildConfigField("String", "STORAGE_BASE_URL", "\"$storageBaseUrl\"")
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$googleMapsApiKey\"")
        buildConfigField("String", "PASSWORD_RESET_SECRET", "\"$passwordResetSecret\"")

        // Inyectar API Key al manifest
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey
    }

    bundle {
        language {
            enableSplit = false
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

// Runtime validation for tasks that require Supabase credentials
tasks.matching { task ->
    // enforce only when producing release artifacts
    task.name.contains("Release", ignoreCase = true) &&
        (task.name.contains("assemble", ignoreCase = true) ||
         task.name.contains("bundle", ignoreCase = true))
}.configureEach {
    doFirst {
        val properties = Properties()
        val localProperties = rootProject.file("local.properties")
        if (localProperties.exists()) {
            localProperties.inputStream().use { properties.load(it) }
        }

        val supabaseUrl = providers.gradleProperty("SUPABASE_URL")
            .orElse(properties.getProperty("SUPABASE_URL") ?: "")
            .getOrElse("")
        val supabaseKey = providers.gradleProperty("SUPABASE_ANON_KEY")
            .orElse(properties.getProperty("SUPABASE_ANON_KEY") ?: "")
            .getOrElse("")

        val passwordResetSecret = providers.gradleProperty("PASSWORD_RESET_SECRET")
            .orElse(properties.getProperty("PASSWORD_RESET_SECRET") ?: "")
            .getOrElse("")

        require(supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            "Missing SUPABASE_URL / SUPABASE_ANON_KEY for Release build."
        }

        require(passwordResetSecret.isNotBlank()) {
            "Missing PASSWORD_RESET_SECRET for Release build."
        }
    }
}

kapt{
    correctErrorTypes = true
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "**/BuildConfig.*",
                    "**/R.class",
                    "**/*_Factory*",
                    "**/*_HiltModules*",
                    "**/*Hilt_*",
                    "**/di/**",
                    "**/navigation/**"
                )
            }
        }
        verify {
            rule {
                minBound(90)
            }
        }
    }
}

tasks.register<JacocoReport>("jacocoTestReport") {
    dependsOn("testDebugUnitTest")
    group = "verification"
    description = "Genera el reporte de cobertura JaCoCo para tests unitarios debug"

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/jacocoTestReport/jacocoTestReport.xml"))
    }

    val fileFilter = listOf(
        "**/R.class",
        "**/R$*.class",
        "**/BuildConfig.*",
        "**/Manifest*.*",
        "**/*Test*.*",
        "android/**/*.*"
    )

    val coverageExcludes = listOf(
        "**/view/**",
        "**/navigation/**",
        "**/MainActivity*.class",
        "**/model/remote/**",
        "**/model/repository/**",
        "**/ExploreViewModel*.class",
        "**/ReporteViewModel*.class",
        "**/ProfileViewModel*.class",
        "**/ThemeKt*.class",
        "**/CityAlertaApplication*.class",
        "**/AccessibilityManager*.class",
        "**/ThemeManager*.class",
        "**/LocaleManager*.class"
    )

    val kotlinDebugTree = fileTree("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
        exclude(coverageExcludes)
    }
    val javaDebugTree = fileTree("${layout.buildDirectory.get().asFile}/intermediates/javac/debug/classes") {
        exclude(fileFilter)
        exclude(coverageExcludes)
    }

    classDirectories.setFrom(files(kotlinDebugTree, javaDebugTree))
    sourceDirectories.setFrom(files("src/main/java"))
    executionData.setFrom(
        files(
            layout.buildDirectory.file("outputs/unit_test_code_coverage/debugUnitTest/testDebugUnitTest.exec"),
            layout.buildDirectory.file("jacoco/testDebugUnitTest.exec")
        )
    )
}

dependencies {
    // Main implementation dependencies
    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.core:core-splashscreen:1.2.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation(platform("androidx.compose:compose-bom:2026.05.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("com.google.android.gms:play-services-auth:21.6.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.android.gms:play-services-maps:20.0.0")
    implementation("com.google.maps.android:maps-compose:8.3.0")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.camera:camera-camera2:1.6.1")
    implementation("androidx.camera:camera-lifecycle:1.6.1")
    implementation("androidx.camera:camera-view:1.6.1")
    implementation("androidx.compose.material3:material3-window-size-class")

    // Network / backend
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.6.1")
    implementation("io.github.jan-tennert.supabase:postgrest-kt:3.6.0")
    implementation("io.github.jan-tennert.supabase:storage-kt:3.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    implementation("io.ktor:ktor-client-android:3.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    implementation("androidx.concurrent:concurrent-futures-ktx:1.3.0")

    // Room
    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    kapt("androidx.room:room-compiler:2.8.4")



    // Test
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // firebase push notificationes
    implementation(platform("com.google.firebase:firebase-bom:34.14.1"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-analytics")
}