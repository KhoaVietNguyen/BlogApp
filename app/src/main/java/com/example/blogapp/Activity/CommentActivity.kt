package com.example.blogapp.Activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.*
import com.example.blogapp.R
import kotlinx.android.synthetic.main.activity_comments.*
import kotlinx.android.synthetic.main.activity_comments.toolbar
import kotlinx.android.synthetic.main.fragment_account.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CommentActivity : AppCompatActivity(), CommentAdapter.ItemClickListener {

    val token = CoreApplication.instance.getUser()?.token
    val idUser = CoreApplication.instance.getUser()?.id
    var idPost: String? = null
    var idAuthorPost: String? = null
    var key: String? = null

    var data: List<Comment>? = null
    var commentAdapter: CommentAdapter? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        val b = intent.extras
        key = intent.getStringExtra("key")
        val obj: Post? = b!!.getParcelable<Parcelable>("ParcelKey") as Post?
        idPost = obj?.id
        idAuthorPost = obj?.author?.id

        tvTagcmt.text = obj?.tag?.name
        tvContent.text = obj?.content
        tvContent.movementMethod = ScrollingMovementMethod()
        tvCmt.text = obj?.cmt!!.size.toString()
        tvLike.text = obj.fan!!.size.toString()
        tvTitle.text = obj.title

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        supportActionBar!!.title

        data = ArrayList()

        avatarUser.setOnClickListener { view ->
            if (!obj.author?.id.equals(idUser)) {
                val i = Intent(applicationContext, UserInfoActivity::class.java)
                i.putExtra("idUser", obj.author?.id)
                i.putExtra("key", "key")
                startActivity(i)
            }
        }

        val urlAvatar = CoreApplication.instance.getUser()?.avatar
        this.let {
            Glide.with(it).load(urlAvatar)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_photo_camera_black_24dp)
                .into(commentProfilePic)
        }



        idPost?.let { loadComment(it) }

        checkLiked(obj.fan!!)

        tvLike.setOnClickListener {
            idPost?.let { it1 -> likePost(it1) }
        }

        commentSubmit.setOnClickListener {
            closeKeyBoard()
            loadeComment.visibility = View.VISIBLE
            val commentText = comment_text.text.toString().trim()


            if (commentText.isEmpty()) {
                comment_text.error = "Invalid text"
                comment_text.requestFocus()
                return@setOnClickListener
            }

            APIClient.instance.createComment(token, commentText, idPost)
                .enqueue(object : Callback<CommentResponseModel> {

                    override fun onFailure(call: Call<CommentResponseModel>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                        Log.e("F", t.message)
                        loadeComment.visibility = View.GONE
                    }

                    override fun onResponse(
                        call: Call<CommentResponseModel>,
                        response: Response<CommentResponseModel>
                    ) {
                        loadeComment.visibility = View.GONE
                        response.body()?.comment?.let { it1 -> (data as ArrayList<Comment>).add(it1) }
                        commentAdapter?.notifyItemChanged((data as ArrayList<Comment>).size - 1)
                        recycler_view_comments.smoothScrollToPosition((data as ArrayList<Comment>).size - 1)
                        tvCmt.text = (data as ArrayList<Comment>).size.toString()
                        //idPost?.let { numCmt?.minus(1)?.let { it1 -> loadComment(it, it1) } }
                    }
                })
            comment_text.setText("")
        }

    }


    fun loadComment(id: String) {
        APIClient.instance.getOnePost(id)
            .enqueue(object : Callback<PostOneResponseModel> {

                override fun onFailure(call: Call<PostOneResponseModel>, t: Throwable) {
                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                    Log.e("F", t.message)
                    loadeComment.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<PostOneResponseModel>,
                    response: Response<PostOneResponseModel>
                ) {

                    Glide.with(applicationContext).load(response.body()?.post?.author?.avatar)
                        .placeholder(R.drawable.placeholder)
                        .into(avatarUser)

                    loadeComment.visibility = View.GONE
                    tvLike.text = response.body()?.post?.fan!!.size.toString()
                    checkLiked(response.body()?.post?.fan!!)
                    data = response.body()?.post?.cmt!!
                    tvCmt.text = data!!.size.toString()
                    addRecyclerView()
                    if (data!!.isNotEmpty())
                        recycler_view_comments.scrollToPosition(data!!.size - 1)
                }
            })
    }

    private fun addRecyclerView() {
        recycler_view_comments.apply {
            // set a LinearLayoutManager to handle Android
            // RecyclerView behavior
            layoutManager = LinearLayoutManager(this@CommentActivity)

            commentAdapter =
                CommentAdapter(
                    data!!, this@CommentActivity,
                    idUser!!
                )
            // set the custom adapter to the RecyclerView
            adapter = commentAdapter

        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        if (key == null) {
//            finish()
//            val i = Intent(this, HomeActivity::class.java)
//            startActivity(i)
//        } else
//            finish()
//        return true
//    }
//
//    override fun onBackPressed() {
//        super.onBackPressed()
//        if (key == null) {
//            finish()
//            val i = Intent(this, HomeActivity::class.java)
//            startActivity(i)
//        } else
//            finish()
//    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm =
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun likePost(idPost: String) {
        loadeComment.visibility = View.VISIBLE
        APIClient.instance.likePost(token, idPost)
            .enqueue(object : Callback<PostOneResponseModel> {

                override fun onFailure(call: Call<PostOneResponseModel>, t: Throwable) {
                    // btnLike.setImageResource(R.drawable.ic_thumb_up_black_liked_24dp)
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<PostOneResponseModel>,
                    response: Response<PostOneResponseModel>
                ) {
                    Log.e("error", "" + response.body()?.success)
                    if (response.body()?.success == null)
                        dislikePost(idPost)
                    else loadComment(idPost)
                }
            })
    }

    fun dislikePost(idPost: String) {
        APIClient.instance.dislikePost(token, idPost)
            .enqueue(object : Callback<PostOneResponseModel> {

                override fun onFailure(call: Call<PostOneResponseModel>, t: Throwable) {
                }

                override fun onResponse(
                    call: Call<PostOneResponseModel>,
                    response: Response<PostOneResponseModel>
                ) {
                    loadComment(idPost)
                }
            })
    }

    private fun likeCmt(idCmt: String, position: Int) {
        APIClient.instance.likeCmt(token, idCmt)
            .enqueue(object : Callback<CommentResponseModel> {

                override fun onFailure(call: Call<CommentResponseModel>, t: Throwable) {
                    // btnLike.setImageResource(R.drawable.ic_thumb_up_black_liked_24dp)
                    Log.i("info", t.message)
                }

                override fun onResponse(
                    call: Call<CommentResponseModel>,
                    response: Response<CommentResponseModel>
                ) {
                    if (response.body()?.success == null)
                        dislikeCmt(idCmt, position)
                    else
                        response.body()?.comment?.let { it1 ->
                            (data as ArrayList<Comment>).set(
                                position,
                                it1
                            )
                        }
                    commentAdapter?.notifyItemChanged(position)
                    //idPost?.let { loadComment(it) }
                }

            })
    }

    fun dislikeCmt(idCmt: String, position: Int) {
        APIClient.instance.dislikeCmt(token, idCmt)
            .enqueue(object : Callback<CommentResponseModel> {

                override fun onFailure(call: Call<CommentResponseModel>, t: Throwable) {
                }

                override fun onResponse(
                    call: Call<CommentResponseModel>,
                    response: Response<CommentResponseModel>
                ) {
                    response.body()?.comment?.let { it1 ->
                        (data as ArrayList<Comment>).set(
                            position,
                            it1
                        )
                    }
                    commentAdapter?.notifyItemChanged(position)
                    //idPost?.let { loadComment(it) }
                }
            })
    }

    private fun checkLiked(listLike: List<String>) {
        if (listLike.contains(idUser)) {
            tvLike.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_thumb_up_black_liked_24dp,
                0,
                0,
                0
            )
        } else
            tvLike.setCompoundDrawablesRelativeWithIntrinsicBounds(
                R.drawable.ic_thumb_up_black_24dp,
                0,
                0,
                0
            )
    }

    override fun onLongClick(item: Comment, position: Int) {
        if (item.author?.id?.equals(idUser)!! || idAuthorPost?.equals(idUser)!!) {
            val alert = AlertDialog.Builder(this)

            val edittext = EditText(this)
            if (!item.author?.id?.equals(idUser)!!)
                edittext.isEnabled = false
            edittext.setText(item.content)
            edittext.hint = "Enter Comment"
            edittext.setBackgroundResource(R.drawable.reg_edittxt_style)
            edittext.setSelection(edittext.text.length)

            val layout = FrameLayout(this)

            //set padding in parent layout
            layout.setPaddingRelative(45, 15, 45, 0)
            alert.setTitle("Comment update")
            layout.addView(edittext)
            alert.setView(layout)
            alert.setPositiveButton(
                "Save"
            ) { dialog, which ->
                run {
                    if (edittext.text.isNotBlank()) {
                        APIClient.instance.updateCmt(token, edittext.text.toString(), item.id)
                            .enqueue(object : Callback<CommentResponseModel> {

                                override fun onFailure(
                                    call: Call<CommentResponseModel>,
                                    t: Throwable
                                ) {
                                    Toast.makeText(
                                        applicationContext,
                                        t.message,
                                        Toast.LENGTH_LONG
                                    )
                                        .show()
                                }

                                override fun onResponse(
                                    call: Call<CommentResponseModel>,
                                    response: Response<CommentResponseModel>
                                ) {
                                    response.body()?.comment?.let { it1 ->
                                        (data as ArrayList<Comment>).set(
                                            position,
                                            it1
                                        )
                                    }
                                    commentAdapter?.notifyItemChanged(position)
                                    // idPost?.let { loadComment(it, position) }
                                }

                            })
                    }
                }
            }
            alert.setNegativeButton("Cancel",
                DialogInterface.OnClickListener {

                        dialog, which ->
                    run {

                    }

                })

            alert.setNeutralButton("Delete") { dialog, which ->
                run {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Warning!")
                    builder.setMessage("Are you sure to remove this comment?")

                    builder.setPositiveButton("Yes") { dialog, which ->
                        APIClient.instance.deleteCmt(token, item.author?.id, item.id)
                            .enqueue(object : Callback<CommentResponseModel> {

                                override fun onFailure(
                                    call: Call<CommentResponseModel>,
                                    t: Throwable
                                ) {
                                    Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG)
                                        .show()
                                }

                                override fun onResponse(
                                    call: Call<CommentResponseModel>,
                                    response: Response<CommentResponseModel>
                                ) {
                                    (data as ArrayList<Comment>).removeAt(position)
                                    commentAdapter?.notifyItemRemoved(position)
                                    tvCmt.text = (data as ArrayList<Comment>).size.toString()
                                    //idPost?.let { loadComment(it, null) }
                                }
                            })
                    }
                    builder.setNegativeButton(android.R.string.no) { dialog, which ->

                    }
                    builder.show()
                }
            }
            alert.show()
        }
    }

    override fun onClick(item: Comment, position: Int) {
        item.id?.let { likeCmt(it, position) }
        //Toast.makeText(applicationContext, "on click", Toast.LENGTH_LONG).show()
    }
}
