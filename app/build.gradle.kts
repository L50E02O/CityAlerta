import java.util.Properties
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
    jacoco
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
        targetSdk = 36
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

        val googleMapsApiKey = providers.gradleProperty("GOOGLE_MAPS_API_KEY")
            .orElse(properties.getProperty("GOOGLE_MAPS_API_KEY") ?: "")
            .getOrElse("")

        if (supabaseUrl.isEmpty() || supabaseKey.isEmpty()) {
            project.logger.warn("WARNING: Missing Supabase config. Define SUPABASE_URL and SUPABASE_ANON_KEY in local.properties or gradle.properties. Build tasks that require Supabase will fail.")
        }

        if (googleMapsApiKey.isEmpty()) {
            project.logger.warn("WARNING: Missing Google Maps API Key. Define GOOGLE_MAPS_API_KEY in local.properties or gradle.properties. Maps will not display.")
        }

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$googleMapsApiKey\"")
        
        // Inyectar API Key al manifest
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = googleMapsApiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

        require(supabaseUrl.isNotBlank() && supabaseKey.isNotBlank()) {
            "Missing SUPABASE_URL / SUPABASE_ANON_KEY for Release build."
        }
    }
}

kapt{
    correctErrorTypes = true
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

    val kotlinDebugTree = fileTree("${layout.buildDirectory.get().asFile}/tmp/kotlin-classes/debug") {
        exclude(fileFilter)
    }
    val javaDebugTree = fileTree("${layout.buildDirectory.get().asFile}/intermediates/javac/debug/classes") {
        exclude(fileFilter)
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.maps)
    implementation(libs.maps.compose)

    // Network / backend
    implementation(libs.supabase.gotrue)
    implementation(libs.supabase.core)
    implementation(libs.supabase.postgrest)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.android)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.inline)

    // Instrumentation tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.mockito.android)
    androidTestImplementation(libs.mockito.kotlin)

    // Debug only
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Annotation processors
    kapt(libs.androidx.room.compiler)

}