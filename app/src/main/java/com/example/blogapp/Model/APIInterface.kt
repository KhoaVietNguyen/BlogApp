package com.example.blogapp.Model

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface APIInterface {

    @POST("user/signup")
    @Multipart
    fun signup(
        @Part filePart: MultipartBody.Part,
        @Part("email") email: RequestBody?,
        @Part("plainPassword") pass: RequestBody?,
        @Part("name") name: RequestBody?
    ): Call<UserResponseModel>

    @FormUrlEncoded
    @POST("user/updatePass")
    fun updatePass(
        @Header("token") token: String?,
        @Field("oldPassword") oldPass: String,
        @Field("newPassword") newPass: String
    ): Call<UserResponseModel>

    @PUT("user")
    @Multipart
    fun updateUser(
        @Header("token") token: String?,
        @Part filePart: MultipartBody.Part?,
        @Part("email") email: RequestBody?,
        @Part("name") name: RequestBody?
    ): Call<UserResponseModel>

    @POST("user/follow/{id}")
    fun follow(
        @Header("token") token: String?,
        @Path("id") id: String?
    ): Call<UserResponseModel>

    @POST("user/disfollow/{id}")
    fun unfollow(
        @Header("token") token: String?,
        @Path("id") id: String?
    ): Call<UserResponseModel>

    @GET("user/{id}")
    fun getInfoUser(
        @Path("id") id: String?
    ): Call<UserResponseModel>

    @GET("user/listFollowing/{id}")
    fun getUserFollowing(
        @Path("id") id: String?
    ): Call<UsersResponseModel>

    @GET("user/listFollower/{id}")
    fun getUserFollower(
        @Path("id") id: String?
    ): Call<UsersResponseModel>

    @FormUrlEncoded
    @POST("user/followed/{id}")
    fun followed(
        //@Header("token") token: String?,
        @Field("idUser") idUser: String,
        @Path("id") id: String?
    ): Call<UserResponseModel>

    @FormUrlEncoded
    @POST("user/disfollowed/{id}")
    fun unfollowed(
        //@Header("token") token: String?,
        @Field("idUser") idUser: String,
        @Path("id") id: String?
    ): Call<UserResponseModel>

    @FormUrlEncoded
    @POST("user/signin")
    fun signin(
        @Field("email") email: String?,
        @Field("plainPassword") pass: String?
    ): Call<UserResponseModel>

    @GET("user/")
    fun getAllUser(
    ): Call<UsersResponseModel>

    @GET("post")
    fun getPost(
    ): Call<PostResponseModel>

    @GET("post/getPostWithTag/{key}")
    fun getPostWithTag(
        @Header("token") token: String?,
        @Path("key") key: String?
    ): Call<PostResponseModel>

    @GET("post/{id}")
    fun getOnePost(
        @Path("id") id: String?
    ): Call<PostOneResponseModel>

    @GET("post/getByUser/{id}")
    fun getPostByIdUser(
        @Path("id") id: String?
    ): Call<PostResponseModel>

    @POST("post/like/{id}")
    fun likePost(
        @Header("token") token: String?,
        @Path("id") id: String?
    ): Call<PostOneResponseModel>

    @POST("post/dislike/{id}")
    fun dislikePost(
        @Header("token") token: String?,
        @Path("id") id: String?
    ): Call<PostOneResponseModel>

    @Multipart
    @POST("post")
    fun creatPost(
        @Header("token") token: String?,
        @Part filePart: MultipartBody.Part,
        @Part("title") title: RequestBody?,
        @Part("content") content: RequestBody?,
        @Part("date") date: RequestBody?,
        @Part("idTag") idTag: RequestBody?,
        @Part("mainContent") mainContent: RequestBody?
    ): Call<PostResponseModel>

    @Multipart
    @PUT("post/{id}")
    fun updatePost(
        @Header("token") token: String?,
        @Part filePart: MultipartBody.Part?,
        @Part("title") title: RequestBody?,
        @Part("content") content: RequestBody?,
        @Part("date") date: RequestBody?,
        @Part("idTag") idTag: RequestBody?,
        @Part("mainContent") mainContent: RequestBody?,
        @Path("id") id: String?
    ): Call<PostOneResponseModel>

    @PATCH("post")
    fun getPostUser(
        @Header("token") token: String?
    ): Call<PostResponseModel>

    @DELETE("post/{id}")
    fun deletePost(
        @Header("token") token: String?,
        @Path("id") id: String?
    ): Call<PostDeleteResponseModel>


    @GET("tag")
    fun getTag(
    ): Call<TagResponseModel>

    @FormUrlEncoded
    @POST("tag")
    fun creatTag(
        @Field("name") id: String?
    ): Call<TagResponseModel>

    @FormUrlEncoded
    @POST("comment")
    fun createComment(
        @Header("token") token: String?,
        @Field("content") content: String?,
        @Field("idPost") idPost: String?
    ): Call<CommentResponseModel>

    @FormUrlEncoded
    @POST("comment/delete/{id}")
    fun deleteCmt(
        @Header("token") token: String?,
        @Field("idUser") idUser: String?,
        @Path("id") id: String?
    ): Call<CommentResponseModel>

    @FormUrlEncoded
    @PUT("comment/{id}")
    fun updateCmt(
        @Header("token") token: String?,
        @Field("content") content: String?,
        @Path("id") id: String?
    ): Call<CommentResponseModel>

    @POST("comment/like/{id}")
    fun likeCmt(
        @Header("token") token: String?,
        @Path("id") id: String?
    ): Call<CommentResponseModel>

    @POST("comment/dislike/{id}")
    fun dislikeCmt(
        @Header("token") token: String?,
        @Path("id") id: String?
    ): Call<CommentResponseModel>

}