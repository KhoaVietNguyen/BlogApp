package com.example.blogapp.ui.post

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.blogapp.R
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.fragment_post.*

class PostFragment : Fragment() {

    private lateinit var postViewModel: PostViewModel

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

        val imgPost: ImageView = root.findViewById(R.id.imagePost)
        imgPost.setOnClickListener {
            openGalleryForImage()
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
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
            imagePost.setImageURI(data?.data) // handle chosen image
        }
    }
}
