package com.example.blogapp.Model

import com.google.gson.annotations.SerializedName

class User {

    @SerializedName("email")
    var email: String? = null

    @SerializedName("plainPassword")
    var plainPassword: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("token")
    var token: String? = null

    @SerializedName("_id")
    var id: String? = null

    @SerializedName("follows")
    var follow: List<String>? = null

    @SerializedName("followed")
    var followed: List<String>? = null

    @SerializedName("avatar")
    var avatar: String? = null

}