package com.example.blogapp.ui.home

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.blogapp.Activity.SearchActivity
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.*
import com.example.blogapp.R
import kotlinx.android.synthetic.main.fragment_home.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    var follow: List<String>? = null

    var swipeContainer: SwipeRefreshLayout? = null

    var recyclerView: RecyclerView? = null

    val idUser = CoreApplication.instance.getUser()?.id

    var tvThongBao: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val search : ImageView = root.findViewById(R.id.search)

        val imagePost: ImageView = root.findViewById(R.id.post)
        imagePost.setOnClickListener {
            findNavController().navigate(R.id.navigation_post, null, null)
        }
        swipeContainer = root.findViewById(R.id.swipeContainer)
        recyclerView = root.findViewById(R.id.listPost)
        tvThongBao = root.findViewById(R.id.tvThongBao)

        search.setOnClickListener {
            val i = Intent(context, SearchActivity::class.java)
            context?.startActivity(i)
        }
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
                        if (follow != null) {
                            for (i in postResponse.indices)
                                if (follow!!.contains(postResponse[i].author?.id) ||
                                    postResponse[i].author?.id?.equals(idUser)!!
                                )
                                    ltPost.add(postResponse[i])
                            tvThongBao?.text = ""
                        }
                    }
                    if (ltPost?.isEmpty()!!)
                        tvThongBao?.text = "No posts available !"
                    else
                        tvThongBao?.text = ""
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

