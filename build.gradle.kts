// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    id("org.sonarqube") version "7.2.3.7755"
}

sonar {
  properties {
    property("sonar.projectKey", "L50E02O_ManTap")
    property("sonar.organization", "l50e02o")
    property("sonar.androidLint.reportPaths", "app/build/reports/lint-results-debug.xml")
    property("sonar.coverage.jacoco.xmlReportPaths", "app/build/reports/jacoco/jacocoTestReport/jacocoTestReport.xml")
    property(
      "sonar.coverage.exclusions",
      "app/src/main/java/man/tap/view/**,app/src/main/java/man/tap/navigation/**,app/src/main/java/man/tap/MainActivity.kt,app/src/main/java/man/tap/model/remote/**,app/src/main/java/man/tap/model/repository/authRepository.kt"
    )
  }
}

tasks.named("sonar") {
  dependsOn(":app:lintDebug")
  dependsOn(":app:jacocoTestReport")
}