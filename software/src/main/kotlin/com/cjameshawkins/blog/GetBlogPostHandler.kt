package com.cjameshawkins.blog

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbMapper
import aws.sdk.kotlin.hll.dynamodbmapper.expressions.KeyFilter
import aws.sdk.kotlin.hll.dynamodbmapper.operations.items
import aws.sdk.kotlin.hll.dynamodbmapper.operations.queryPaginated
import aws.sdk.kotlin.runtime.auth.credentials.EnvironmentCredentialsProvider
import aws.sdk.kotlin.services.dynamodb.DynamoDbClient
import aws.smithy.kotlin.runtime.ExperimentalApi
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import com.cjameshawkins.blog.dynamodbmapper.generatedschemas.getBlogPostTable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

class GetBlogPostHandler:  RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private val blogTable = System.getenv("BLOG_TABLE")
    private val ddbClient = DynamoDbClient {
        region = System.getenv("AWS_REGION")
        credentialsProvider = EnvironmentCredentialsProvider()
    }
    @OptIn(ExperimentalApi::class)
    private val mapper =  DynamoDbMapper(ddbClient)
    @OptIn(ExperimentalApi::class)
    override fun handleRequest(event: APIGatewayV2HTTPEvent, context: Context): APIGatewayV2HTTPResponse {
        val logger = context.logger

        val requestId = event.pathParameters?.get("id") ?: return missingId()

        logger.log("INFO: fetching blog post ${requestId}")

        val blogTable = mapper.getBlogPostTable(blogTable)

        val response = try {
            runBlocking {
                blogTable.queryPaginated {
                    keyCondition = KeyFilter(requestId)
                }.items().first()
            }
        }catch(e: Exception){
            return APIGatewayV2HTTPResponse().apply{
                statusCode = 500
                headers = mapOf("Content-Type" to "application/json")
                body = """{"message": "Blog Post does not exist. ${e.message}"}"""
            }
        }
        return APIGatewayV2HTTPResponse().apply {
            statusCode = 200
            headers = mapOf("Content-Type" to "application/json")
            body = Json.encodeToString(response)
        }

    }
}