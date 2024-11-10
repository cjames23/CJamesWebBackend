plugins {
    kotlin("jvm") version "2.0.21"
    id("aws.sdk.kotlin.hll.dynamodbmapper.schema.generator") version "1.3.+"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}

group = "org.cjameshawkins"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.amazonaws:aws-lambda-java-core:1.+")
    implementation("com.amazonaws:aws-lambda-java-events:3.+")
    implementation("com.amazonaws:aws-lambda-java-log4j:1.+")
    implementation("aws.sdk.kotlin:dynamodb:1.3.70")
    implementation("aws.sdk.kotlin:dynamodb-mapper:1.3.70-beta")
    implementation("aws.sdk.kotlin:dynamodb-mapper-annotations:1.3.70-beta")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}