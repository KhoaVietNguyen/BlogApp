package com.example.blogapp.Model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Post() : Parcelable {
    @SerializedName("fans")
    var fan: List<String>? = null

    @SerializedName("comments")
    var cmt: List<Comment>? = null

    @SerializedName("_id")
    var id: String? = null

    @SerializedName("content")
    var content: String? = null

    @SerializedName("mainContent")
    var mainContent: String? = null

    @SerializedName("author")
    var author: Author? = null

    @SerializedName("title")
    var title: String? = null

    @SerializedName("date")
    var date: String? = null

    @SerializedName("tag")
    var tag: Tag? = null

    @SerializedName("image")
    var image: String? = null

    constructor(parcel: Parcel) : this() {
        fan = parcel.createStringArrayList()
        cmt = parcel.createTypedArrayList(Comment)
        id = parcel.readString()
        content = parcel.readString()
        mainContent = parcel.readString()
        author = parcel.readParcelable(Author::class.java.classLoader)
        title = parcel.readString()
        date = parcel.readString()
        tag = parcel.readParcelable(Tag::class.java.classLoader)
        image = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(fan)
        parcel.writeTypedList(cmt)
        parcel.writeString(id)
        parcel.writeString(content)
        parcel.writeString(mainContent)
        parcel.writeParcelable(author, flags)
        parcel.writeString(title)
        parcel.writeString(date)
        parcel.writeParcelable(tag, flags)
        parcel.writeString(image)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }

}