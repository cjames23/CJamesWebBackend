package com.cjameshawkins.infra

import software.amazon.awscdk.CfnOutput
import software.amazon.awscdk.Duration
import software.amazon.awscdk.Stack
import software.amazon.awscdk.StackProps
import software.amazon.awscdk.services.apigatewayv2.AddRoutesOptions
import software.amazon.awscdk.services.apigatewayv2.HttpApi
import software.amazon.awscdk.services.apigatewayv2.HttpMethod
import software.amazon.awscdk.services.apigatewayv2.PayloadFormatVersion
import software.amazon.awscdk.aws_apigatewayv2_integrations.HttpLambdaIntegration
import software.amazon.awscdk.services.dynamodb.Attribute
import software.amazon.awscdk.services.dynamodb.AttributeType
import software.amazon.awscdk.services.dynamodb.BillingMode
import software.amazon.awscdk.services.dynamodb.Table
import software.amazon.awscdk.services.lambda.*
import software.amazon.awscdk.services.lambda.Function
import software.amazon.awscdk.services.logs.RetentionDays
import software.constructs.Construct

class InfrastructureStack constructor(scope: Construct, id: String, props: StackProps) : Stack(scope, id, props) {
    companion object {
        private const val MEMORY_SIZE = 2048
    }

    val functions = mutableListOf<Function>()
    private val codePath = "../software/build/libs/software-1.0-SNAPSHOT-all.jar"
    private val optimizationLayer: LayerVersion = LayerVersion.Builder.create(this, "OptimizationLayer")
        .layerVersionName("OptimizationLayer")
        .description("Enable tiered compilation")
        .compatibleRuntimes(listOf(Runtime.JAVA_11, Runtime.JAVA_8_CORRETTO))
        .code(Code.fromAsset("../software/OptimizationLayer/layer.zip"))
        .build()

    init {
        val blogTable = Table.Builder.create(this, "Cjames-PersonalBlog")
            .tableName("Cjames-PersonalBlog")
            .partitionKey(
                Attribute.builder()
                    .type(AttributeType.STRING)
                    .name("PK")
                    .build()
            )
            .billingMode(BillingMode.PAY_PER_REQUEST)
            .build()
        val environmentVariables = mapOf(
            "BLOG_TABLE" to blogTable.tableName,
            "AWS_LAMBDA_EXEC_WRAPPER" to "/opt/java-exec-wrapper",
        )

        val putBlogPostFunction = buildLambdaFunction("PutBlogPost", environmentVariables)
        val getBlogPostFunction = buildLambdaFunction("GetBlogPost", environmentVariables)
        val getAllBlogPostsFunction = buildLambdaFunction("GetAllBlogPosts", environmentVariables)
        val deleteBlogPostFunction = buildLambdaFunction("DeleteBlogPost", environmentVariables)
        blogTable.grantReadData(getBlogPostFunction)
        blogTable.grantReadData(getAllBlogPostsFunction)
        blogTable.grantWriteData(putBlogPostFunction)
        blogTable.grantWriteData(deleteBlogPostFunction)
        functions.add(getBlogPostFunction)
        functions.add(putBlogPostFunction)
        functions.add(deleteBlogPostFunction)
        functions.add(getAllBlogPostsFunction)

        val httpApi = HttpApi.Builder.create(this, "BlogPostsApi")
            .apiName("BlogPostsApi")
            .build()
        httpApi.addRoutes(
            AddRoutesOptions.builder()
                .path("/{id}")
                .methods(listOf(HttpMethod.PUT))
                .integration(
                    HttpLambdaIntegration.Builder.create("putBlogPost", putBlogPostFunction).apply{
                        payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                    }.build()
                )
                .build()
        )
        httpApi.addRoutes(
            AddRoutesOptions.builder()
                .path("/{id}")
                .methods(listOf(HttpMethod.GET))
                .integration(
                    HttpLambdaIntegration.Builder.create("getBlogPost", getBlogPostFunction).apply{
                        payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                    }.build()
                )
                .build()
        )
        httpApi.addRoutes(
            AddRoutesOptions.builder()
                .path("/")
                .methods(listOf(HttpMethod.GET))
                .integration(
                    HttpLambdaIntegration.Builder.create("getAllBlogPosts", getAllBlogPostsFunction).apply{
                        payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                    }.build()
                )
                .build()
        )
        httpApi.addRoutes(
            AddRoutesOptions.builder()
                .path("/{id}")
                .methods(listOf(HttpMethod.DELETE))
                .integration(
                    HttpLambdaIntegration.Builder.create("deleteBlogPost", deleteBlogPostFunction).apply{
                        payloadFormatVersion(PayloadFormatVersion.VERSION_2_0)
                    }.build()
                )
                .build()
        )

        CfnOutput.Builder.create(this, "KotlinApiUrl")
            .exportName("KotlinApiUrl")
            .value(httpApi.apiEndpoint)
            .build()
    }

    private fun buildLambdaFunction(
        name: String,
        environmentVariables: Map<String, String>,
    ) = Function.Builder.create(this, name)
        .functionName(name)
        .handler("com.cjameshawkins.blog.${name}Handler")
        .runtime(Runtime.JAVA_17)
        .code(Code.fromAsset(codePath))
        .architecture(Architecture.ARM_64)
        .logRetention(RetentionDays.ONE_WEEK)
        .memorySize(MEMORY_SIZE)
        .timeout(Duration.seconds(20))
        .environment(environmentVariables)
        .layers(listOf(optimizationLayer))
        .build()
}