package com.cjameshawkins.blog

import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbItem
import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbPartitionKey
import aws.sdk.kotlin.hll.dynamodbmapper.DynamoDbSortKey
import kotlinx.serialization.Serializable

@Serializable
@DynamoDbItem
data class BlogPost(
    @DynamoDbPartitionKey
    val postId: Int,
    val title: String,
    val body: String,
    @DynamoDbSortKey
    val postDate: String
)
