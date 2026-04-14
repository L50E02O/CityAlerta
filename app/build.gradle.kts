import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "man.tap"
    compileSdk = 36

    defaultConfig {
        applicationId = "man.tap"
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

        if (supabaseUrl.isEmpty() || supabaseKey.isEmpty()) {
            project.logger.warn("WARNING: Missing Supabase config. Define SUPABASE_URL and SUPABASE_ANON_KEY in local.properties or gradle.properties. Build tasks that require Supabase will fail.")
        }

        buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseKey\"")
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
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
dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.play.services.auth)
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.mockito:mockito-inline:5.2.0")
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
//    liberias implemenntadas
    implementation("androidx.core:core-splashscreen:1.0.0")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:2.0.0")
    implementation("io.github.jan-tennert.supabase:supabase-kt:2.0.0")
    implementation("io.ktor:ktor-client-android:2.3.7")

//    Room
    implementation("androidx.room:room-runtime:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")

}