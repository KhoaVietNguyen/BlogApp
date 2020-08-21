package com.example.blogapp.Model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

class Comment() : Parcelable {

    @SerializedName("fans")
    var fans: List<String>? = null

    @SerializedName("_id")
    var id: String? = null

    @SerializedName("author")
    var author: Author? = null

    @SerializedName("content")
    var content: String? = null

    @SerializedName("post")
    var post: String? = null

    constructor(parcel: Parcel) : this() {
        fans = parcel.createStringArrayList()
        id = parcel.readString()
        author = parcel.readParcelable(Author::class.java.classLoader)
        content = parcel.readString()
        post = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(fans)
        parcel.writeString(id)
        parcel.writeParcelable(author, flags)
        parcel.writeString(content)
        parcel.writeString(post)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }
    }

}