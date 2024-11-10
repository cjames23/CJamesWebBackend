package com.cjameshawkins.infra

import software.amazon.awscdk.App
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.Environment

fun main() {
    val app = App()
    val infrastructureStack = InfrastructureStack(
        app, "CjamesBlogBackend", StackProps.builder()
            .env(
                Environment.builder()
                    .account(System.getenv("CDK_DEFAULT_ACCOUNT"))
                    .region(System.getenv("CDK_DEFAULT_REGION"))
                    .build()
            )
            .build()
    )

    app.synth()
}