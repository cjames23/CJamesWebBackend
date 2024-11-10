plugins {
    application
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.+")
    implementation("software.amazon.awscdk:apigatewayv2:1.+")
    implementation("software.amazon.awscdk:apigatewayv2-integrations:1.+")
    implementation("software.constructs:constructs:10.+")
}

application {
    mainClass.set("com.cjameshawkins.infra.InfrastructureAppKt")
}

task<Exec>("makeOptimizationLayer") {
    workingDir = File("../software/OptimizationLayer/")
    commandLine("./build-layer.sh")
}

tasks.named("run") {
    dependsOn(":infrastructure:makeOptimizationLayer")
    dependsOn(":software:shadowJar")
}