package com.example.blogapp.Activity

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.APIClient
import com.example.blogapp.Model.Post
import com.example.blogapp.Model.PostAdapter
import com.example.blogapp.Model.PostResponseModel
import com.example.blogapp.R
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_search_info.*
import kotlinx.android.synthetic.main.fragment_search_info.swipeContainer
import kotlinx.android.synthetic.main.fragment_search_info.toolbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SearchActivity : AppCompatActivity() {

    val idUser = CoreApplication.instance.getUser()?.id
    var ltPost: List<Post>? = null
    var postAdapter: PostAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_search_info)

        setSupportActionBar(toolbar)
        val rootView = window.decorView.rootView
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        search.visibility = View.VISIBLE

        search.setOnClickListener {
            hideKeyboard()
            if (search_bar.text.isEmpty()) {
                search_bar.error = "Invalid text"
                search_bar.requestFocus()
                return@setOnClickListener
            }else
                getPost(rootView, search_bar.text.trim().toString().toLowerCase() )
        }
        search_bar.hint = "Search topic..."
        relativelay.setBackgroundColor(Color.parseColor("#8EA5A5A5"))

        swipeContainer.isEnabled = false
    }

    fun AppCompatActivity.hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        // else {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        // }
    }

    fun getPost(root: View, key: String) {
        loaderSearch.visibility=View.VISIBLE
        APIClient.instance.getPost()
            .enqueue(object : Callback<PostResponseModel> {

                override fun onFailure(call: Call<PostResponseModel>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<PostResponseModel>,
                    response: Response<PostResponseModel>
                ) {

                    if (response.body()?.success!!) {
                        ltPost = response.body()?.posts!!
                        val postResponse = ltPost!!.toMutableList()
                        (ltPost as ArrayList<Post>).clear()

                        for (i in postResponse.indices)
                            if (postResponse[i].tag?.name?.trim()?.toLowerCase()?.contains(key)!! && !postResponse[i].author?.id?.equals(idUser)!!
                            )
                                (ltPost as ArrayList<Post>).add(postResponse[i])
                    }

                    if (ltPost?.isEmpty()!!)
                        tvThongBao?.text = "No post found !"
                    else
                        tvThongBao?.text = ""

                    recycler_view.apply {
                        // set a LinearLayoutManager to handle Android
                        // RecyclerView behavior
                        layoutManager = LinearLayoutManager(this@SearchActivity)
                        // set the custom adapter to the RecyclerView
                        postAdapter = ltPost?.let { PostAdapter(it) }
                        if (postAdapter != null) {
                            postAdapter!!.view = root
                        }
                        adapter = postAdapter
                    }
                    loaderSearch.visibility=View.GONE
                }
            })
    }


}