package com.example.blogapp.Model

import com.google.gson.annotations.SerializedName

class contents {
    @SerializedName("content")
    var content: String? = null

    @SerializedName("image")
    var image: String? = null
}