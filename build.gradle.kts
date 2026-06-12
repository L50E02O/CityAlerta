// Top-level build file where you can add configuration options common to all subproject/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    id("org.sonarqube") version "7.3.0.8198"
    id("com.google.gms.google-services") version "4.4.4" apply false
}

sonar {
  properties {
    property("sonar.projectKey", "L50E02O_ManTap")
    property("sonar.organization", "l50e02o")
  }
}

project(":app") {
  sonar {
    properties {
      // Definimos fuentes y tests en el modulo para evitar indexacion duplicada en raiz.
      property("sonar.sources", "src/main/java")
      property("sonar.tests", "src/test/java")
      property("sonar.junit.reportPaths", "build/test-results/testDebugUnitTest")
      property("sonar.java.binaries", "build/tmp/kotlin-classes/debug,build/intermediates/javac/debug/classes")
      property("sonar.androidLint.reportPaths", "build/reports/lint-results-debug.xml")
      property(
        "sonar.coverage.jacoco.xmlReportPaths",
        "build/reports/kover/report.xml"
      )
      property(
        "sonar.coverage.exclusions",
        listOf(
          "src/main/java/ec/cityalerta/app/view/**",
          "src/main/java/ec/cityalerta/app/navigation/**",
          "src/main/java/ec/cityalerta/app/MainActivity.kt",
          "src/main/java/ec/cityalerta/app/model/remote/**",
          "src/main/java/ec/cityalerta/app/model/repository/**",
          "src/main/java/ec/cityalerta/app/viewmodel/ExploreViewModel.kt",
          "src/main/java/ec/cityalerta/app/viewmodel/ReporteViewModel.kt",
          "src/main/java/ec/cityalerta/app/viewmodel/ProfileViewModel.kt",
          "src/main/java/ec/cityalerta/app/theme/Theme.kt",
          "src/main/java/ec/cityalerta/app/CityAlertaApplication.kt",
          "src/main/java/ec/cityalerta/app/theme/AccessibilityManager.kt",
          "src/main/java/ec/cityalerta/app/theme/ThemeManager.kt",
          "src/main/java/ec/cityalerta/app/theme/LocaleManager.kt"
        ).joinToString(",")
      )
    }
  }
}

tasks.named("sonar") {
  dependsOn(":app:lintDebug")
  dependsOn(":app:koverXmlReport")
}