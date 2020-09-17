package com.example.blogapp.ui

import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.blogapp.Activity.afterTextChanged
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.*
import com.example.blogapp.R
import kotlinx.android.synthetic.main.fragment_search_info.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchUserFragment : Fragment(), UserAdapter.ItemClickListener {

    private val idUser = CoreApplication.instance.getUser()?.id
    val token = CoreApplication.instance.getUser()?.token
    var listUser: List<User>? = null
    var userAdapter: UserAdapter? = null

    var swipeContainer: SwipeRefreshLayout? = null

    var recyclerView: RecyclerView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_search_info, container, false)
        swipeContainer = root.findViewById(R.id.swipeContainer)
        recyclerView = root.findViewById(R.id.recycler_view)

        var searchBar: EditText = root.findViewById(R.id.search_bar)
        searchBar.hint = "Search user..."

        events(root)

        searchBar.afterTextChanged {
            searchfriend(searchBar.text.toString(), root)
        }
        return root
    }

    private fun events(root: View) {
        getUser(root)
        swipeContainer?.setOnRefreshListener {
            recycler_view.visibility = View.INVISIBLE
            val handler = Handler()
            handler.postDelayed({ getUser(root) }, 500)
        }
    }

    fun searchfriend(s: CharSequence, root: View) {
        var list = listUser?.toMutableList()
        (listUser as ArrayList<User>).clear()
        if (s.isNotEmpty()) {
            if (list != null) {
                for (i in list.indices) {
                    // Adapt the if for your usage
                    if (list[i].name?.toLowerCase()?.contains(
                            s.toString().toLowerCase()
                        )!!
                    ) {
                        (listUser as ArrayList<User>).add(list[i])
                    }
                }
            }
        } else
            getUser(root)
        userAdapter?.notifyDataSetChanged()
    }

    fun getUser(root: View) {

        APIClient.instance.getAllUser()
            .enqueue(object : Callback<UsersResponseModel> {

                override fun onFailure(call: Call<UsersResponseModel>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
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
                    var index = 0
                    for (i in listUser!!.indices)
                        if (listUser!![i].id.equals(idUser))
                            index = i
                    (listUser as ArrayList<User>).removeAt(index)
                    recycler_view.apply {
                        // set a LinearLayoutManager to handle Android
                        // RecyclerView behavior
                        layoutManager = LinearLayoutManager(activity)
                        // set the custom adapter to the RecyclerView
                        userAdapter = UserAdapter(listUser!!, this@SearchUserFragment)
                        userAdapter!!.view = root
                        adapter = userAdapter
                    }
                    swipeContainer!!.isRefreshing = false
                    recyclerView!!.visibility = View.VISIBLE
                }
            })
    }

    fun follow(id: String) {
        APIClient.instance.follow(token, id)
            .enqueue(object : Callback<UserResponseModel> {

                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {

                }
            })
    }

    fun followed(id: String, position: Int) {
        APIClient.instance.followed(idUser!!, id)
            .enqueue(object : Callback<UserResponseModel> {

                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
                    response.body()?.user?.let { (listUser as ArrayList<User>).set(position, it) }
                    userAdapter?.notifyItemChanged(position)
                }
            })
    }

    fun getInfoUser(id: String, position: Int) {
        APIClient.instance.getInfoUser(id)
            .enqueue(object : Callback<UserResponseModel> {
                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.e("error", t.message)
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
                    if (response.body()?.user?.followed?.contains(idUser)!!) {
                        unfollowed(id, position)
                        unfollow(id)

                    } else {
                        followed(id, position)
                        follow(id)
                    }
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

                }
            })
    }

    fun unfollowed(id: String, position: Int) {
        APIClient.instance.unfollowed(idUser!!, id)
            .enqueue(object : Callback<UserResponseModel> {

                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
                    response.body()?.user?.let { (listUser as ArrayList<User>).set(position, it) }
                    userAdapter?.notifyItemChanged(position)
                }
            })
    }

    override fun onClick(item: User, position: Int, view: View) {
        item.id?.let { getInfoUser(it, position) }
    }

}