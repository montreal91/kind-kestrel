@file:Suppress("UnstableApiUsage")

plugins {
  // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
  alias(libs.plugins.jvm)

  // Apply the application plugin to add support for building a CLI application in Java.
  application
}

repositories {
  mavenCentral()
}

dependencies {
}

testing {
  suites {
    // Configure the built-in test suite
    val test by getting(JvmTestSuite::class) {
      // Use Kotlin Test framework
      useKotlinTest("1.9.22")
    }
  }
}

java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(11)
  }
}

application {
  mainClass = "org.example.rlc.application.RudnyLoxCompilerKt"
}
