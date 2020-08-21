package com.example.blogapp.ui

import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.blogapp.Activity.afterTextChanged
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.*
import com.example.blogapp.R
import kotlinx.android.synthetic.main.fragment_friend.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class FriendFragment : Fragment(), UserAdapter.ItemClickListener {
    private val idUser = CoreApplication.instance.getUser()?._id
    val token = CoreApplication.instance.getUser()?.token
    var listUser: List<User>? = null
    var userAdapter: UserAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_friend, container, false)
        var searchBar: EditText = root.findViewById(R.id.search_bar)
        var swipeContainer: SwipeRefreshLayout = root.findViewById(R.id.swipeContainer)

        swipeContainer.isEnabled = false
        getUser(root)

        searchBar.afterTextChanged {
            searchfriend(searchBar.text.toString(), root)
        }

        return root
    }

    private fun searchfriend(s: CharSequence, root: View) {
        var list = listUser?.toMutableList()
        (listUser as ArrayList<User>).clear()
        if (s.isNotEmpty()) {
            if (list != null) {
                for (i in list.indices) {
                    // Adapt the if for your usage
                    if (list[i].name?.toLowerCase(Locale.ROOT)?.contains(
                            s.toString().toLowerCase(Locale.ROOT)
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

        APIClient.instance.getUserFollowing(idUser)
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
                    recycler_view.apply {
                        // set a LinearLayoutManager to handle Android
                        // RecyclerView behavior
                        layoutManager = LinearLayoutManager(activity)
                        // set the custom adapter to the RecyclerView
                        userAdapter = UserAdapter(listUser!!, this@FriendFragment)
                        userAdapter!!.view = root
                        adapter = userAdapter
                    }
                    //swipeContainer!!.isRefreshing = false
                    //recyclerView!!.visibility = View.VISIBLE
                }
            })
    }

    fun unfollow(id: String, position: Int) {
        APIClient.instance.unfollow(token, id)
            .enqueue(object : Callback<UserResponseModel> {

                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
                    (listUser as ArrayList<User>).removeAt(position)
                    userAdapter?.notifyItemRemoved(position)
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

                }
            })
    }

    override fun onClick(item: User, position: Int, view: View) {
        val alert = AlertDialog.Builder(activity!!)
        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPaddingRelative(45, 15, 45, 0)
        //alert.setTitle("Unfollow")
        alert.setMessage("Unfollow ${item.name}?")
        alert.setPositiveButton(
            "Unfollow"
        ) { dialog, which ->
            run {
                item._id?.let { unfollow(it, position) }
                item._id?.let { unfollowed(it) }
            }
        }
        alert.setNegativeButton("Cancel") { dialog, which ->
            run {

            }
        }
        alert.show()

    }

}