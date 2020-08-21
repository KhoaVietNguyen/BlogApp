package com.example.blogapp.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.APIClient
import com.example.blogapp.Model.UserResponseModel
import com.example.blogapp.R
import com.google.android.material.snackbar.Snackbar
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.toolbar
import kotlinx.android.synthetic.main.fragment_friend.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class RegisterActivity : AppCompatActivity() {
    var flag: Boolean? = false
    var file: File? = null
    val REQUEST_CODE = 100
    private var mImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Handler().postDelayed({
            loader.isVisible = false
        }, 1500)

        setContentView(R.layout.activity_register)

        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = title
        toolbar.setNavigationOnClickListener { finish() }

        imageUserPhoto.setOnClickListener {
            CropImage.activity()
                .setAspectRatio(1, 1)
                .start(this@RegisterActivity)
        }

        btnRegister.setOnClickListener {
            closeKeyBoard()

            val email = edEmail.text.toString().trim()
            val password = edPassword.text.toString().trim()
            val name = edUsername.text.toString().trim()


            if (name.isEmpty() || name.length < 6) {
                edUsername.error = "Name required, at least 6 chars"
                edUsername.requestFocus()
                return@setOnClickListener
            }

            if (!isEmailValid(email)) {
                edEmail.error = "Invalid Email"
                edEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty() || password.length < 6) {
                edPassword.error = "Password required, at least 6 chars"
                edPassword.requestFocus()
                return@setOnClickListener
            }

            if (!flag!!) {
                val snack = Snackbar.make(it, "Add an avatar !", Snackbar.LENGTH_LONG)
                snack.show()
                return@setOnClickListener
            }

            val filePart: MultipartBody.Part = MultipartBody.Part.createFormData(
                "file",
                file?.name, RequestBody.create(MediaType.parse("image"), file)
            )
            val userName =
                RequestBody.create(MediaType.parse("multipart/form-data"), name)
            val pass =
                RequestBody.create(MediaType.parse("multipart/form-data"), password)
            val emailSN =
                RequestBody.create(MediaType.parse("multipart/form-data"), email)


            loader.visibility = View.VISIBLE
            // email, password, name
            APIClient.instance.signup(filePart, emailSN, pass, userName)
                .enqueue(object : Callback<UserResponseModel> {
                    override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                        Toast.makeText(applicationContext, t.message, Toast.LENGTH_LONG).show()
                        Log.e("F", t.message)
                        loader.visibility = View.GONE
                    }

                    override fun onResponse(
                        call: Call<UserResponseModel>,
                        response: Response<UserResponseModel>
                    ) {
                        if (response.body()?.success != null) {
                            CoreApplication.instance.saveUser(response.body()!!.user)
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                            finish()
                        } else {
                            dialogMessenger("Warning !", "Email already exists !")
                            edEmail.requestFocus()
                        }
                        loader.visibility = View.GONE
                    }
                })
        }


    }

    fun dialogMessenger(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton("OK") { dialog, which -> }
        alertDialog.show()
    }

    private fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            mImageUri = result.uri
            imageUserPhoto.setImageURI(mImageUri) // handle chosen image

            file = File(getRealPathFromURI(mImageUri))

//            Log.i("INFO", data?.data?.path)
            Log.i("IN", getRealPathFromURI(mImageUri))
//            Log.i("IN", mImageUri?.let { getFileExtension(it) })

            flag = true
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun getRealPathFromURI(contentURI: Uri?): String? {
        val result: String?
        val cursor: Cursor? = this.contentResolver?.query(contentURI!!, null, null, null, null)
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

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm =
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

}
