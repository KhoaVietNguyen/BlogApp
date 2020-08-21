package com.example.blogapp.Activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.APIClient
import com.example.blogapp.Model.User
import com.example.blogapp.Model.UserAdapter
import com.example.blogapp.Model.UsersResponseModel
import com.example.blogapp.R
import kotlinx.android.synthetic.main.fragment_friend.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowActivity : AppCompatActivity(), UserAdapter.ItemClickListener {
    private val idUser = CoreApplication.instance.getUser()?._id
    val token = CoreApplication.instance.getUser()?.token
    var listUser: List<User>? = null
    var userAdapter: UserAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)
        val rootView = window.decorView.rootView
        val key = intent.getStringExtra("key")
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        if (key == "following") {
            getUserFollowing(rootView)
            title = "List following"
            supportActionBar!!.title = title
        } else {
            getUserFollower(rootView)
            title = "List followers"
            supportActionBar!!.title = title
        }

    }

    fun getUserFollowing(root: View) {

        APIClient.instance.getUserFollowing(idUser)
            .enqueue(object : Callback<UsersResponseModel> {

                override fun onFailure(call: Call<UsersResponseModel>, t: Throwable) {
                    //Toast.makeText(a, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<UsersResponseModel>,
                    response: Response<UsersResponseModel>
                ) {
                    if (response.body()?.success!!) {
                        //loader.visibility = View.GONE
                        // postResponse= response.body()?.posts!!

                    }
                    listUser = response.body()?.users!!
                    recycler_view.apply {
                        // set a LinearLayoutManager to handle Android
                        // RecyclerView behavior
                        layoutManager = LinearLayoutManager(context)
                        // set the custom adapter to the RecyclerView
                        userAdapter = UserAdapter(listUser!!, this@FollowActivity)
                        userAdapter!!.view = root
                        adapter = userAdapter
                    }
                    //swipeContainer!!.isRefreshing = false
                    //recyclerView!!.visibility = View.VISIBLE
                }
            })
    }

    fun getUserFollower(root: View) {

        APIClient.instance.getUserFollower(idUser)
            .enqueue(object : Callback<UsersResponseModel> {

                override fun onFailure(call: Call<UsersResponseModel>, t: Throwable) {
                    //Toast.makeText(a, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<UsersResponseModel>,
                    response: Response<UsersResponseModel>
                ) {
                    if (response.body()?.success!!) {
                        //loader.visibility = View.GONE
                        // postResponse= response.body()?.posts!!

                    }
                    listUser = response.body()?.users!!
                    recycler_view.apply {
                        // set a LinearLayoutManager to handle Android
                        // RecyclerView behavior
                        layoutManager = LinearLayoutManager(context)
                        // set the custom adapter to the RecyclerView
                        userAdapter = UserAdapter(listUser!!, this@FollowActivity)
                        userAdapter!!.view = root
                        adapter = userAdapter
                    }
                    //swipeContainer!!.isRefreshing = false
                    //recyclerView!!.visibility = View.VISIBLE
                }
            })
    }

    override fun onClick(item: User, position: Int, view: View) {

    }

}