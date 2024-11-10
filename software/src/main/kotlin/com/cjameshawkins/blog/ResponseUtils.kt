package com.cjameshawkins.blog

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
fun missingId() = APIGatewayV2HTTPResponse().apply{
        statusCode = 500
        headers = mapOf("Content-Type" to "application/json")
        body = """{"message": "Missing Blog Post 'id' parameter in path'"}"""
    }
