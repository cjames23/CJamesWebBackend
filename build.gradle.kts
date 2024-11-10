plugins {
    kotlin("jvm") version "2.0.21"
}

group = "org.cjameshawkins"
version = "1.0-SNAPSHOT"

allprojects {
    apply(plugin =  "org.jetbrains.kotlin.jvm")
    repositories {
        mavenCentral()
    }

    kotlin {
        jvmToolchain(17)
    }
}
