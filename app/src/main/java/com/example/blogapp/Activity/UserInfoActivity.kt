package com.example.blogapp.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.*
import com.example.blogapp.R
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_account.toolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UserInfoActivity : AppCompatActivity(), PostUserAdapter.ItemClickListener {
    val token = CoreApplication.instance.getUser()?.token
    val idUser = CoreApplication.instance.getUser()?._id

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_account)
        val id = intent.getStringExtra("idUser")
        val rootView = window.decorView.rootView
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar!!.title
        getPostInfoUser(id, rootView)
        events(id, rootView)
    }

    private fun events(id: String, v: View) {
        swipeContainer?.setOnRefreshListener {
            layoutAcc?.visibility = View.INVISIBLE
            val handler = Handler()
            handler.postDelayed({ getPostInfoUser(id, v) }, 500)
        }

        btnProfile.setOnClickListener {
            loaderUser.visibility = View.VISIBLE
            APIClient.instance.getInfoUser(idUser)
                .enqueue(object : Callback<UserResponseModel> {
                    override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                        Log.e("error", t.message)
                    }

                    override fun onResponse(
                        call: Call<UserResponseModel>,
                        response: Response<UserResponseModel>
                    ) {
                        var listFollowed = response.body()?.user?.follow
                        if (listFollowed!!.contains(id)) {
                            unfollow(id)
                            unfollowed(id)
                        } else {
                            follow(id)
                            followed(id)
                        }
                        loaderUser.visibility = View.GONE
                    }
                })
        }
    }

    private fun getInfoUser(id: String) {
        APIClient.instance.getInfoUser(id)
            .enqueue(object : Callback<UserResponseModel> {
                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.e("error", t.message)
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
                    var listFollowed = response.body()?.user?.followed
                    var listFollow = response.body()?.user?.follow
                    var name = response.body()?.user?.name
                    var email = response.body()?.user?.email
                    var avatar = response.body()?.user?.avatar

                    followers.text = "${listFollowed?.size}"
                    following.text = "${listFollow?.size}"
                    Glide.with(applicationContext).load(avatar)
                        .into(imageUser)

                    tvUserName.text = name
                    tvUserEmail.text = email

                    checkUserFollow(id)
                }
            })
    }

    private fun getPostInfoUser(id: String, v: View) {
        getInfoUser(id)
        APIClient.instance.getPostByIdUser(id)
            .enqueue(object : Callback<PostResponseModel> {

                override fun onFailure(call: Call<PostResponseModel>, t: Throwable) {
                    Log.i("info", t.message)
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<PostResponseModel>,
                    response: Response<PostResponseModel>
                ) {
                    val listPost: ArrayList<Post>

                    if (response.body()?.success!!) {
                        listPost = response.body()!!.posts as ArrayList<Post>
                        var likeCount = 0
                        var cmtCount = 0
                        var postCount = listPost.size

                        for (i in listPost.indices) {
                            likeCount += listPost[i].fan?.size!!
                            cmtCount += listPost[i].cmt?.size!!
                        }
                        tvLikeCount.text = "$likeCount Likes"
                        tvCmtCount.text = "$cmtCount Comments"
                        tvPostCount.text = "$postCount"

                        listUserPost.apply {
                            // set a LinearLayoutManager to handle Android
                            // RecyclerView behavior
                            layoutManager = LinearLayoutManager(this@UserInfoActivity)
                            // set the custom adapter to the RecyclerView
                            //val idUser = CoreApplication.instance.getUser()?._id

                            val postAdapter = PostUserAdapter(listPost, this@UserInfoActivity)
                            postAdapter.view = v
                            adapter = postAdapter
                        }

                        swipeContainer!!.isRefreshing = false
                        layoutAcc?.visibility = View.VISIBLE
                        loaderUser.visibility = View.GONE
                    }
                }
            })
    }

    private fun follow(id: String) {
        APIClient.instance.follow(token, id)
            .enqueue(object : Callback<UserResponseModel> {

                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
                    Toast.makeText(
                        applicationContext,
                        "You followed ${tvUserName.text}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    Log.e("loi", "" + response.body()?.success)
                    checkUserFollow(id)
                }
            })
    }

    fun unfollow(id: String) {
        APIClient.instance.unfollow(token, id)
            .enqueue(object : Callback<UserResponseModel> {

                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
                    Toast.makeText(
                        applicationContext,
                        " You unfollowed ${tvUserName.text}",
                        Toast.LENGTH_LONG
                    )
                        .show()
                    checkUserFollow(id)
                }
            })
    }

    private fun followed(id: String) {
        APIClient.instance.followed(idUser!!, id)
            .enqueue(object : Callback<UserResponseModel> {
                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
//                    Toast.makeText(applicationContext, "1", Toast.LENGTH_LONG)
//                        .show()
                    Log.e("error", "" + response.body()?.success)
                    checkUserFollow(id)
                }
            })
    }

    fun unfollowed(id: String) {
        APIClient.instance.unfollowed(idUser!!, id)
            .enqueue(object : Callback<UserResponseModel> {

                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
//                    Toast.makeText(applicationContext, "2", Toast.LENGTH_LONG)
//                        .show()
//                    checkUserFollow(id)
                }
            })
    }

    private fun checkUserFollow(id: String) {
        APIClient.instance.getInfoUser(idUser)
            .enqueue(object : Callback<UserResponseModel> {
                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.e("error", t.message)
                }

                @SuppressLint("ResourceAsColor")
                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
                    var listFollowed = response.body()?.user?.follow
                    if (listFollowed!!.contains(id)) {
                        btnProfile.text = "FOLLOWING"
                        btnProfile.setBackgroundResource(R.drawable.button_background_conf)
                        btnProfile.setTextColor(Color.WHITE)

                    } else {
                        btnProfile.text = "+ FOLLOW"

                    }
                    getInfoUser(id)
                }
            })
    }

    override fun onSupportNavigateUp(): Boolean {
        val key = intent.getStringExtra("key")
        if (key == null) {
            finish()
            val i = Intent(this, HomeActivity::class.java)
            startActivity(i)
        } else
            finish()
        return true
    }

    override fun onClick(item: Post, position: Int, view: View) {
        val b = Bundle()
        b.putParcelable("ParcelKey", item)
        val i = Intent(view.context, CommentActivity::class.java)
        i.putExtras(b)
        i.putExtra("key", "key")
        view.context.startActivity(i)
    }

    override fun onLongClick(view: Post, position: Int, v: View) {
        TODO("Not yet implemented")
    }
}