@file:Suppress("UnstableApiUsage")

plugins {
  kotlin("multiplatform") version "2.1.0"
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
        implementation("com.github.ajalt.clikt:clikt:5.0.2")
        implementation("co.touchlab:kermit:2.0.0")
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
