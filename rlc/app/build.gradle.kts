@file:Suppress("UnstableApiUsage")

plugins {
  kotlin("multiplatform") version "2.0.0"
}

repositories {
  mavenCentral()
}

dependencies {
}

kotlin {
  mingwX64(name = "native") {
    binaries {
      executable(namePrefix = "rlc") {
        entryPoint = "org.example.rlc.application.main"
      }
    }
  }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation(kotlin(simpleModuleName = "stdlib-common"))
      }
    }
    val nativeMain by getting {
      dependencies {
        implementation(kotlin(simpleModuleName = "stdlib"))
      }
    }

    val nativeTest by getting {
      dependencies {
        implementation(kotlin(simpleModuleName = "test-common"))
        implementation(kotlin(simpleModuleName = "test-annotations-common"))
      }
    }
  }
}
