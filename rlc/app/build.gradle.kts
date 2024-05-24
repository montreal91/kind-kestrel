@file:Suppress("UnstableApiUsage")

plugins {
  kotlin("jvm") version "2.0.0"

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
    val test by getting(JvmTestSuite::class) {
      useKotlinTest("2.0.0")
    }
  }
}

kotlin {
  jvmToolchain(jdkVersion = 11)
}

application {
  mainClass = "org.example.rlc.application.RudnyLoxCompilerKt"
}
