package com.example.blogapp.ui.Account

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.blogapp.Activity.CommentActivity
import com.example.blogapp.Activity.FollowActivity
import com.example.blogapp.Activity.LoginActivity
import com.example.blogapp.Activity.afterTextChanged
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.*
import com.example.blogapp.R
import com.theartofdev.edmodo.cropper.CropImage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.fragment_account.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File


class AccountFragment : Fragment(), PostUserAdapter.ItemClickListener {

    private lateinit var accountViewModel: AccountViewModel
    val REQUEST_CODE = 100
    val CAMERA = 101
    var flag: Boolean? = false
    var file: File? = null
    private val token = CoreApplication.instance.getUser()?.token
    var tvFileName: TextView? = null
    var avatarUser: CircleImageView? = null
    var imagePost: ImageView? = null
    private var tagResponse: List<Tag> = ArrayList()
    var tagName: ArrayList<String> = ArrayList()
    var status = 0
    var viewUpdate: View? = null
    var swipeContainer: SwipeRefreshLayout? = null
    var layoutAcc: ConstraintLayout? = null

    var listPost: List<Post>? = null
    var postAdapter: PostUserAdapter? = null
    val idUser = CoreApplication.instance.getUser()?.id

    private var mImageUri: Uri? = null

    @SuppressLint("InflateParams")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        accountViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_account, container, false)
        val loaderUser: ProgressBar = root.findViewById(R.id.loaderUser)
        val btnProfile: Button = root.findViewById(R.id.btnProfile)
        val following: LinearLayout = root.findViewById(R.id.followingLayOut)
        val follower: LinearLayout = root.findViewById(R.id.followersLayout)
        val options: ImageView = root.findViewById(R.id.options)

        options.setOnClickListener {
            val popupMenu = PopupMenu(context, options)
            popupMenu.menuInflater.inflate(R.menu.menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.logout -> {
                        val alert = AlertDialog.Builder(activity!!)
                        val layout = LinearLayout(context)
                        layout.orientation = LinearLayout.VERTICAL
                        layout.setPaddingRelative(45, 15, 45, 0)
                        alert.setMessage("Are you sure to sign out ?")
                        alert.setPositiveButton(
                            "Sign out"
                        ) { dialog, which ->
                            run {
                                activity?.finish()
                                startActivity(Intent(context, LoginActivity::class.java))
                            }
                        }
                        alert.setNegativeButton("Cancel") { dialog, which ->
                            run {

                            }
                        }
                        alert.show()
                    }
                    R.id.changePass -> {
                        val alertChangePass = AlertDialog.Builder(activity!!)
                        val alertConfirm = AlertDialog.Builder(activity!!)
                        val alertLogin = AlertDialog.Builder(activity!!)

                        val edCurrentPass = EditText(activity!!)
                        val edNewPass = EditText(activity!!)
                        edNewPass.setBackgroundResource(R.drawable.reg_edittxt_style)
                        edCurrentPass.setBackgroundResource(R.drawable.reg_edittxt_style)
                        val lp = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        lp.setMargins(0, 32, 0, 32)
                        edCurrentPass.layoutParams = lp
                        edNewPass.hint = "New password"
                        edCurrentPass.inputType = InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_PASSWORD
                        edNewPass.inputType = InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_VARIATION_PASSWORD

                        edCurrentPass.hint = "Current password"
                        edCurrentPass.maxLines = 1
                        edCurrentPass.setSelection(edCurrentPass.text.length)

                        val layout = LinearLayout(activity!!)
                        layout.orientation = LinearLayout.VERTICAL

                        //set padding in parent layout
                        layout.setPaddingRelative(45, 15, 45, 0)
                        alertChangePass.setTitle("Change password")
                        layout.addView(edCurrentPass)
                        layout.addView(edNewPass)
                        alertChangePass.setView(layout)
                        alertChangePass.setPositiveButton(
                            "Change"
                        ) { dialog, which ->
                            run {
                                if (edCurrentPass.text.isNotBlank() && edNewPass.text.isNotBlank() && edCurrentPass.text.length >= 6 && edNewPass.text.length >= 6) {
                                    if (edCurrentPass.text.toString() == edNewPass.text.toString()) {
                                        dialogMessenger(
                                            "Warning !",
                                            "The new password must be different from the old !"
                                        )
                                    } else {
                                        alertConfirm.setTitle("Warning!")
                                        alertConfirm.setMessage("Are you sure to change password ?")

                                        alertConfirm.setPositiveButton("Yes") { dialog, which ->
                                            APIClient.instance.updatePass(
                                                CoreApplication.instance.getUser()?.token,
                                                edCurrentPass.text.toString(),
                                                edNewPass.text.toString()
                                            )
                                                .enqueue(object : Callback<UserResponseModel> {

                                                    override fun onFailure(
                                                        call: Call<UserResponseModel>,
                                                        t: Throwable
                                                    ) {
//                                                        Toast.makeText(
//                                                            applicationContext,
//                                                            t.message,
//                                                            Toast.LENGTH_LONG
//                                                        )
//                                                            .show()
                                                    }

                                                    override fun onResponse(
                                                        call: Call<UserResponseModel>,
                                                        response: Response<UserResponseModel>
                                                    ) {
                                                        if (response.body()?.success != null) {

                                                            alertLogin.setTitle("Warning!")
                                                            alertLogin.setMessage("You need to login again.")
                                                            alertLogin.setPositiveButton("OK") { dialog, which ->
                                                                activity!!.finish()
                                                                startActivity(
                                                                    Intent(
                                                                        context,
                                                                        LoginActivity::class.java
                                                                    )
                                                                )
                                                            }
                                                            alertLogin.setOnDismissListener {
                                                                activity!!.finish()
                                                                startActivity(
                                                                    Intent(
                                                                        context,
                                                                        LoginActivity::class.java
                                                                    )
                                                                )
                                                            }
                                                            alertLogin.show()

                                                        } else {
                                                            dialogMessenger(
                                                                "Warning !",
                                                                "Incorrect current password !"
                                                            )
                                                        }
                                                    }

                                                })
                                        }
                                        alertConfirm.setNegativeButton("Cancel") { dialog, which ->

                                        }
                                        alertConfirm.show()
                                    }
                                } else
                                    dialogMessenger(
                                        "Warning !",
                                        "Invalid password, at least 6 chars !"
                                    )
                            }
                        }
                        alertChangePass.setNegativeButton("Cancel") { dialog, which ->

                        }
                        alertChangePass.show()
                        true
                    }
                }
                true
            }
            popupMenu.show()
        }

        following.setOnClickListener {
            val i = Intent(context, FollowActivity::class.java)
            i.putExtra("key", "following")
            context?.startActivity(i)
        }

        follower.setOnClickListener {
            val i = Intent(context, FollowActivity::class.java)
            i.putExtra("key", "follower")
            context?.startActivity(i)
        }
        swipeContainer = root.findViewById(R.id.swipeContainer)

        layoutAcc = root.findViewById(R.id.layoutAcc)
        loaderUser.visibility = View.GONE

        viewUpdate = inflater.inflate(R.layout.dialog_update_post, container, false)

        val view = inflater.inflate(R.layout.dialog_custom_layout, container, false)
        avatarUser = view.findViewById(R.id.avatar)
        val edName: EditText = view.findViewById(R.id.edName)
        val edEmail: EditText = view.findViewById(R.id.edEmail)
        tvFileName = view.findViewById(R.id.tvFileName)

        events(root)

        btnProfile.text = "Edit Profile"

        btnProfile.setOnClickListener {

            val alert = AlertDialog.Builder(activity!!)
            val avatar: CircleImageView = view.findViewById(R.id.avatar)
            val tvFileName: TextView = view.findViewById(R.id.tvFileName)

            tvFileName.visibility = View.GONE

            avatar.setOnClickListener {
                callCropImage(1, 1)
            }

            Glide.with(it).load(CoreApplication.instance.getUser()?.avatar)
                .into(avatar)

            edName.setText(CoreApplication.instance.getUser()?.name)
            edEmail.setText(CoreApplication.instance.getUser()?.email)
            edName.setSelection(edName.text.length)

            alert.setTitle("User Info")
            if (view.parent != null) {
                (view.parent as ViewGroup).removeView(view) // <- fix
            }
            alert.setView(view)

            alert.setPositiveButton("Change") { dialog, which ->

                val filePart: MultipartBody.Part? = if (file == null)
                    null
                else
                    MultipartBody.Part.createFormData(
                        "file",
                        file?.name, RequestBody.create(MediaType.parse("image"), file)
                    )

                val name =
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        edName.text.toString().trim()
                    )
                val email =
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        edEmail.text.toString().trim()
                    )

                val builder = view?.context.let { it1 -> AlertDialog.Builder(it1!!) }
                builder.setTitle("Warning!")
                builder.setMessage("Are you sure to change info ?")

                builder.setPositiveButton("Yes") { dialog, which ->
                    loaderUser.visibility = View.VISIBLE
                    APIClient.instance.updateUser(token, filePart, email, name)
                        .enqueue(object : Callback<UserResponseModel> {
                            override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                                t.message?.let { it1 -> dialogMessenger("Warning !", it1) }
                                loaderUser.visibility = View.GONE
                                file = null
                                Log.e("F", t.message)
                            }

                            override fun onResponse(
                                call: Call<UserResponseModel>,
                                response: Response<UserResponseModel>
                            ) {
                                if (response.body()?.success != null) {
                                    CoreApplication.instance.saveUser(response.body()!!.user)
                                    getInfoUser()
                                } else
                                    dialogMessenger("Warning !", "Email already exists !")
                                loaderUser.visibility = View.GONE
                                tvFileName.visibility = View.GONE
                                file = null
                            }
                        })

                }
                builder.setNegativeButton("Cancel") { dialog, which ->

                }
                builder.show()
            }
            alert.setNegativeButton("Cancel") { dialog, which ->
                run {

                }

            }
            val dialog: AlertDialog = alert.create()
            dialog.show()

            edName.afterTextChanged {
                if (edName.text.isBlank()) {
                    edName.error = "Invalid Name"
                    edName.requestFocus()
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    edName.text.isNotBlank() && isEmailValid(edEmail.text.toString().trim())

            }

            edEmail.afterTextChanged {
                if (!isEmailValid(edEmail.text.toString().trim())) {
                    edEmail.error = "Invalid Email"
                    edEmail.requestFocus()
                }
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    edName.text.isNotBlank() && isEmailValid(edEmail.text.toString().trim())
            }

            dialog.setOnDismissListener {
                edName.error = null
                edEmail.error = null
                file = null
            }

        }

        return root
    }

    private fun callCropImage(width: Int, height: Int) {
        val intent = CropImage.activity()
            .setAspectRatio(width, height)
            .getIntent(context!!)

        startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
    }

    private fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    private fun events(root: View) {
        getPost(root)
        getInfoUser()
        getTagUser()
        swipeContainer?.setOnRefreshListener {
            //layoutAcc?.visibility = View.INVISIBLE
            val handler = Handler()
            handler.postDelayed({
                getPost(root)
                getInfoUser()
            }, 500)
        }
    }

    private fun getInfoUser() {
        APIClient.instance.getInfoUser(idUser)
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

                    username.text = name

                    context?.let {
                        Glide.with(it).load(avatar)
                            .into(imageUser)
                    }

                    tvUserName.text = name
                    tvUserEmail.text = email

                }
            })
    }

    private fun getPost(root: View) {
        APIClient.instance.getPostUser(token)
            .enqueue(object : Callback<PostResponseModel> {

                override fun onFailure(call: Call<PostResponseModel>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                    Log.i("info", t.message)
                }

                @SuppressLint("SetTextI18n")
                override fun onResponse(
                    call: Call<PostResponseModel>,
                    response: Response<PostResponseModel>
                ) {

                    if (response.body()?.success!!) {
                        listPost = response.body()!!.posts as ArrayList<Post>
                        listUserPost.apply {
                            // set a LinearLayoutManager to handle Android
                            // RecyclerView behavior
                            layoutManager = LinearLayoutManager(activity)
                            // set the custom adapter to the RecyclerView
                            //val idUser = CoreApplication.instance.getUser()?._id

                            var likeCount = 0
                            var cmtCount = 0
                            var postCount = (listPost as ArrayList<Post>).size

                            for (i in (listPost as ArrayList<Post>).indices) {
                                likeCount += (listPost as ArrayList<Post>)[i].fan?.size!!
                                cmtCount += (listPost as ArrayList<Post>)[i].cmt?.size!!
                            }
                            tvLikeCount.text = "$likeCount Likes"
                            tvCmtCount.text = "$cmtCount Comments"
                            tvPostCount.text = "$postCount"

                            postAdapter =
                                PostUserAdapter(listPost as ArrayList<Post>, this@AccountFragment)
                            postAdapter!!.view = root
                            adapter = postAdapter

                            swipeContainer!!.isRefreshing = false
                            layoutAcc?.visibility = View.VISIBLE
                        }
                    }
                }
            })
    }

    private fun getTagUser() {
        APIClient.instance.getTag()
            .enqueue(object : Callback<TagResponseModel> {

                override fun onFailure(call: Call<TagResponseModel>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<TagResponseModel>,
                    response: Response<TagResponseModel>
                ) {
                    if (response.body()?.success == true) {
                        tagResponse = response.body()?.tags!!

                        for (i in tagResponse.indices)
                            tagResponse[i].name?.let { tagName.add(it) }
                    }
                }

            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        imagePost = viewUpdate?.findViewById(R.id.imagePostUpdate)

        if (resultCode == Activity.RESULT_CANCELED)
            return
        if (resultCode == Activity.RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            mImageUri = result.uri
            file = File(getRealPathFromURI(mImageUri))
            if (status == 1) {
                imagePost?.setImageURI(mImageUri)
            } else {
                avatarUser?.setImageURI(mImageUri) // handle chosen image
                tvFileName?.visibility = View.VISIBLE
                tvFileName?.text = file!!.name
            }
            status = 0
            flag = true
        }
    }

    private fun getRealPathFromURI(contentURI: Uri?): String? {
        val result: String?
        val resolver = activity!!.contentResolver
        val cursor: Cursor? = resolver?.query(contentURI!!, null, null, null, null)
        if (cursor == null) {
            result = contentURI?.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

    override fun onClick(item: Post, position: Int, view: View) {
        val b = Bundle()
        b.putParcelable("ParcelKey", item)
        val i = Intent(view.context, CommentActivity::class.java)
        i.putExtras(b)
        i.putExtra("key", "key")
        view.context.startActivity(i)
    }

    override fun onLongClick(item: Post, position: Int, v: View) {

        val imagePost: ImageView? = viewUpdate?.findViewById(R.id.imagePostUpdate)
        val titlePost: EditText? = viewUpdate?.findViewById(R.id.edTitleUpdate)
        val contentPost: EditText? = viewUpdate?.findViewById(R.id.edDescriptionUpdate)
        val spTag: Spinner? = viewUpdate?.findViewById(R.id.spinnerTagUpdate)

        var tag: Int? = null

        val adapter = context?.let {
            ArrayAdapter(
                it, // Context
                android.R.layout.simple_spinner_item, // Layout
                tagName // Array
            )
        }

        // Set the drop down view resource
        adapter?.setDropDownViewResource(R.layout.dropdown_item_line)

        // Finally, data bind the spinner object with adapter
        spTag?.adapter = adapter
        for (i in tagName.indices)
            if (tagName[i] == item.tag?.name) {
                spTag?.setSelection(i)
                tag = i
            }

        spTag?.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (adapter != null) {
                        tag = position
                    }
                }

            }

        val alert = AlertDialog.Builder(activity!!)

        imagePost?.setOnClickListener {
            status = 1
            callCropImage(16, 9)
        }

        imagePost?.let {
            Glide.with(this).load(item.image)
                .into(it)
        }

        titlePost?.setText(item.title)
        contentPost?.setText(item.mainContent)
        titlePost?.setSelection(titlePost.text.length)


        alert.setTitle("Post Info")
        if (viewUpdate?.parent != null) {
            (viewUpdate?.parent as ViewGroup).removeView(viewUpdate) // <- fix
        }
        alert.setView(viewUpdate)
        alert.setPositiveButton("Change") { dialog, which ->

            if (titlePost?.text?.trim()?.isNotEmpty()!! && contentPost?.text?.trim()?.isNotEmpty()!!) {
                val filePart: MultipartBody.Part? = if (file == null)
                    null
                else
                    MultipartBody.Part.createFormData(
                        "file",
                        file?.name, RequestBody.create(MediaType.parse("image"), file)
                    )

                val title =
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        titlePost.text.toString().trim()
                    )
                val content =
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        contentPost.text.toString().trim()
                    )
                val idTag =
                    RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        tag?.let { it1 -> tagResponse.get(it1).id })

                val date =
                    RequestBody.create(MediaType.parse("multipart/form-data"), item.date)

                val builder = viewUpdate?.context.let { it1 -> AlertDialog.Builder(it1!!) }
                builder.setTitle("Warning!")
                builder.setMessage("Are you sure to change this post ?")

                builder.setPositiveButton("Yes") { dialog, which ->
                    loaderUser.visibility = View.VISIBLE
                    APIClient.instance.updatePost(
                        token,
                        filePart,
                        title,
                        content,
                        date,
                        idTag,
                        content,
                        item.id
                    )
                        .enqueue(object : Callback<PostOneResponseModel> {
                            override fun onFailure(call: Call<PostOneResponseModel>, t: Throwable) {
                                t.message?.let { it1 -> dialogMessenger("Warning !", it1) }
                                Log.e("F", t.message)
                                file = null
                            }

                            override fun onResponse(
                                call: Call<PostOneResponseModel>,
                                response: Response<PostOneResponseModel>
                            ) {
//                            Toast.makeText(
//                                viewUpdate?.context,
//                                response.body()?.success.toString(),
//                                Toast.LENGTH_LONG
//                            ).show()
                                loaderUser.visibility = View.GONE
                                Log.i("info", response.body()?.success.toString())
                                //tagName.removeAll(tagName)
                                response.body()?.post?.let { it1 ->
                                    (listPost as ArrayList<Post>).set(
                                        position,
                                        it1
                                    )
                                }
                                postAdapter?.notifyItemChanged(position)
                            }
                        })

                }
                builder.setNegativeButton("Cancel") { dialog, which ->

                }
                builder.show()
            }

        }
        alert.setNegativeButton("Cancel") { dialog, which ->
            run {

            }

        }

        alert.setNeutralButton("Delete") { dialog, which ->
            run {

                val builder = viewUpdate?.context.let { it1 -> AlertDialog.Builder(it1!!) }
                builder.setTitle("Warning!")
                builder.setMessage("Are you sure to remove this post ?")

                builder.setPositiveButton("Yes") { dialog, which ->

                    APIClient.instance.deletePost(token, item.id)
                        .enqueue(object : Callback<PostDeleteResponseModel> {
                            override fun onFailure(
                                call: Call<PostDeleteResponseModel>,
                                t: Throwable
                            ) {
                                Toast.makeText(viewUpdate?.context, t.message, Toast.LENGTH_LONG)
                                    .show()
                                Log.e("F", t.message)
                            }

                            override fun onResponse(
                                call: Call<PostDeleteResponseModel>,
                                response: Response<PostDeleteResponseModel>
                            ) {
                                (listPost as ArrayList<Comment>).removeAt(position)
                                postAdapter?.notifyItemRemoved(position)
                                postAdapter?.notifyItemChanged(position)
//                                val ft: FragmentTransaction = fragmentManager!!.beginTransaction()
//                                if (Build.VERSION.SDK_INT >= 26) {
//                                    ft.setReorderingAllowed(false)
//                                }
//                                ft.detach(this@AccountFragment).attach(this@AccountFragment)
//                                    .commit()
                            }
                        })

                }
                builder.setNegativeButton("Cancel") { dialog, which ->

                }
                builder.show()

            }

        }
        val dialog: AlertDialog = alert.create()
        dialog.show()

        titlePost?.afterTextChanged {
            if (titlePost.text.trim().isEmpty()) {
                titlePost.error = "Invalid Title"
                titlePost.requestFocus()
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                titlePost.text.isNotBlank() && contentPost?.text!!.isNotBlank()
        }

        contentPost?.afterTextChanged {
            if (contentPost.text.trim().isEmpty()) {
                contentPost.error = "Invalid Content"
                contentPost.requestFocus()
            }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                titlePost?.text!!.isNotBlank() && contentPost.text.isNotBlank()
        }

        dialog.setOnDismissListener {
            titlePost?.error = null
            contentPost?.error = null
            file = null
        }

    }

    fun dialogMessenger(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(activity!!)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton("OK") { dialog, which -> }
        alertDialog.show()
    }
}
