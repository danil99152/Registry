package com.example.registry

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Post {
    @SerializedName("userId")
    @Expose
    var userId: Int = 0
    @SerializedName("id")
    @Expose
    var id: Int = 0
    @SerializedName("title")
    @Expose
    var title: String? = null
    @SerializedName("body")
    @Expose
    var body: String? = null

    internal fun getUserId(): Int {
        return userId
    }

    internal fun setUserId(userId: Int) {
        this.userId = userId
    }

    internal fun getId(): Int {
        return id
    }

    internal fun setId(id: Int) {
        this.id = id
    }

    internal fun getTitle(): String? {
        return title
    }

    internal fun setTitle(title: String) {
        this.title = title
    }

    internal fun getBody(): String? {
        return body
    }

    internal fun setBody(body: String) {
        this.body = body
    }
}