// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
  id("org.sonarqube") version "7.2.3.7755"
}

sonar {
  properties {
    property("sonar.projectKey", "L50E02O_ManTap")
    property("sonar.organization", "l50e02o")
  }
}