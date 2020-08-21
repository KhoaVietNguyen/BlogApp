package com.example.blogapp.ui.post

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.APIClient
import com.example.blogapp.Model.PostResponseModel
import com.example.blogapp.Model.Tag
import com.example.blogapp.Model.TagResponseModel
import com.example.blogapp.R
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_post.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class PostFragment : Fragment() {

    private lateinit var postViewModel: PostViewModel
    private var tagResponse: List<Tag> = ArrayList()

    var file: File? = null
    private var mImageUri: Uri? = null

    var currentTime: Date = Calendar.getInstance().time

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        postViewModel =
            ViewModelProvider(this).get(PostViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_post, container, false)
//        val textView: TextView = root.findViewById(R.id.text_post)
        //     postViewModel.text.observe(viewLifecycleOwner, Observer {
//            textView.text = it
//        })

        val btnPost: ImageButton = root.findViewById(R.id.btnPost)
        val edTitle: EditText = root.findViewById(R.id.edTitle)
        val edContent: EditText = root.findViewById(R.id.edDescription)
        val spTag: Spinner = root.findViewById(R.id.spinnerTag)
        val loader: ProgressBar = root.findViewById(R.id.loaderPost)
        var tag: Int? = null

//        APIClient.instance.creatTag("Ẩm thực")
//            .enqueue(object : retrofit2.Callback<TagResponseModel> {
//
//                override fun onFailure(call: Call<TagResponseModel>, t: Throwable) {
//                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
//                }
//
//                override fun onResponse(
//                    call: Call<TagResponseModel>,
//                    response: Response<TagResponseModel>
//                ) {
//
//                }
//            })


        APIClient.instance.getTag()
            .enqueue(object : retrofit2.Callback<TagResponseModel> {

                override fun onFailure(call: Call<TagResponseModel>, t: Throwable) {
                    Toast.makeText(context, t.message, Toast.LENGTH_LONG).show()
                }

                override fun onResponse(
                    call: Call<TagResponseModel>,
                    response: Response<TagResponseModel>
                ) {
                    if (response.body()?.success!!) {
                        loader.visibility = View.GONE
                        tagResponse = response.body()?.tags!!

                    }
                    var tagName: ArrayList<String> = ArrayList()
                    for (i in tagResponse.indices)
                        tagResponse.get(i).name?.let { tagName.add(it) }

                    val adapter = context?.let {
                        ArrayAdapter(
                            it, // Context
                            android.R.layout.simple_spinner_item, // Layout
                            tagName // Array
                        )
                    }

                    // Set the drop down view resource
                    adapter?.setDropDownViewResource(R.layout.dropdown_item_line)

                    // Finally, data bind the spinner object with dapter
                    spTag.adapter = adapter

                    spTag.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
                }

            })


        btnPost.setOnClickListener {
            if (edTitle.text.isEmpty()) {
                edTitle.error = "Title required"
                edTitle.requestFocus()
                return@setOnClickListener
            }
            if (edContent.text.isEmpty()) {
                edContent.error = "Description required"
                edContent.requestFocus()
                return@setOnClickListener
            }
            if (file == null) {
                Toast.makeText(context, "Hãy chọn ảnh !", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "file",
                file?.name, RequestBody.create(MediaType.parse("image"), file)
            )
            val content =
                RequestBody.create(
                    MediaType.parse("multipart/form-data"),
                    edContent.text.toString()
                )
            val title =
                RequestBody.create(MediaType.parse("multipart/form-data"), edTitle.text.toString())
            val date =
                RequestBody.create(MediaType.parse("multipart/form-data"), currentTime.toString())
            val idTag =
                RequestBody.create(
                    MediaType.parse("multipart/form-data"),
                    tag?.let { it1 -> tagResponse.get(it1).id })

            loader.visibility = View.VISIBLE
            APIClient.instance.creatPost(
                CoreApplication.instance.getUser()?.token,
                filePart,
                title,
                content,
                date,
                idTag,
                content
            )
                .enqueue(object : retrofit2.Callback<PostResponseModel> {
                    override fun onFailure(call: Call<PostResponseModel>, t: Throwable) {
                        t.message?.let { it1 -> dialogMessenger("Warning !", it1) }
                    }

                    override fun onResponse(
                        call: Call<PostResponseModel>,
                        response: Response<PostResponseModel>
                    ) {
                        if (response.body()?.success!!) {
//                            loader.visibility = View.GONE
//                            Toast.makeText(context, "Thành công", Toast.LENGTH_LONG).show()

                        }
                        findNavController().navigate(R.id.navigation_home, null, null)
                    }

                })

        }

        val imgPost: ImageView = root.findViewById(R.id.imagePost)

        imgPost.setOnClickListener {
            val intent = CropImage.activity()
                .setAspectRatio(16, 9)
                .getIntent(context!!)

            startActivityForResult(intent, CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
//            CropImage.activity()
//                .setAspectRatio(1, 1)
//                .start(this@PostFragment)
            // openGalleryForImage()
        }
        return root
    }

    val REQUEST_CODE = 100

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            mImageUri = result.uri
            imagePost.setImageURI(mImageUri) // handle chosen image
            file = File(getRealPathFromURI(mImageUri))
            //Log.i("Info", getRealPathFromURI(data?.data))
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

    fun dialogMessenger(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(activity!!)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton("OK") { dialog, which -> }
        alertDialog.show()
    }

}

private fun CropImage.ActivityBuilder.start(postFragment: PostFragment) {
    TODO("Not yet implemented")
}
