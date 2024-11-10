package com.cjameshawkins.blog

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbMapper
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
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GetAllBlogPostsHandler:  RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private val blogTable = System.getenv("BLOG_TABLE")
    private val ddbClient = DynamoDbClient {
        region = System.getenv("AWS_REGION")
        credentialsProvider = EnvironmentCredentialsProvider()
    }
    @OptIn(ExperimentalApi::class)
    private val mapper =  DynamoDbMapper(ddbClient)
    @OptIn(ExperimentalApi::class)
    override fun handleRequest(input: APIGatewayV2HTTPEvent, context: Context): APIGatewayV2HTTPResponse {
        val logger = context.logger

        val blogTable = mapper.getBlogPostTable(blogTable)

        val result = try{
            runBlocking{
                blogTable.queryPaginated{}.items().toList()
            }
        }catch(e: Exception){
            logger.log("ERROR: ${e.message}")
            return APIGatewayV2HTTPResponse().apply{
                statusCode = 500
                headers = mapOf("Content-Type" to "application/json")
                body = """{"message": "Failed to get blog posts"}"""
            }
        }
        val posts = BlogPosts(result)
        return APIGatewayV2HTTPResponse().apply{
            statusCode = 200
            headers = mapOf("Content-Type" to "application/json")
            body = Json.encodeToString(posts)
        }
    }
}