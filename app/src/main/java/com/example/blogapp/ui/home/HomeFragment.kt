package com.example.blogapp.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.*
import com.example.blogapp.R
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.fragment_account.*
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    var follow: List<String>? = null

    var swipeContainer: SwipeRefreshLayout? = null

    var recyclerView: RecyclerView? = null

    val idUser = CoreApplication.instance.getUser()?._id

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
//        val textView: TextView = root.findViewById(R.id.text_home)
//        homeViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        swipeContainer = root.findViewById(R.id.swipeContainer)
        recyclerView = root.findViewById(R.id.listPost)
        events(root)

        return root
    }

    fun events(root: View) {
        getPost(root)
        swipeContainer?.setOnRefreshListener {
            recyclerView?.visibility = View.INVISIBLE
            val handler = Handler()
            handler.postDelayed({ getPost(root) }, 500)
        }
    }

    fun getPost(root: View) {
        if (idUser != null) {
            getInfoUser(idUser)
        }
        APIClient.instance.getPost()
            .enqueue(object : Callback<PostResponseModel> {

                override fun onFailure(call: Call<PostResponseModel>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<PostResponseModel>,
                    response: Response<PostResponseModel>
                ) {
                    var ltPost: List<Post>? = null
                    if (response.body()?.success!!) {
                        loader.visibility = View.GONE
                        ltPost = response.body()?.posts!!
                        val postResponse = ltPost.toMutableList()
                        (ltPost as ArrayList<Post>).clear()
                        if (follow != null)
                            for (i in postResponse.indices)
                                if (follow!!.contains(postResponse[i].author?.id) || postResponse[i].author?.id?.equals(
                                        idUser
                                    )!!
                                )
                                    ltPost.add(postResponse[i])

                    }
                    listPost.apply {
                        // set a LinearLayoutManager to handle Android
                        // RecyclerView behavior
                        layoutManager = LinearLayoutManager(activity)
                        // set the custom adapter to the RecyclerView
                        var postAdapter = ltPost?.let { PostAdapter(it) }
                        if (postAdapter != null) {
                            postAdapter.view = root
                        }
                        adapter = postAdapter
                    }
                    swipeContainer!!.isRefreshing = false
                    recyclerView!!.visibility = View.VISIBLE
                }
            })
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
                    follow = response.body()?.user?.follow
                }
            })
    }

}

