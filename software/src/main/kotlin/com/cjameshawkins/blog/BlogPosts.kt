package com.cjameshawkins.blog

import kotlinx.serialization.Serializable

@Serializable
data class BlogPosts(val blogPost: List<BlogPost>)
