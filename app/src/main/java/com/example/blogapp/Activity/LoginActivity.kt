package com.example.blogapp.Activity

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.IntRange
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.example.blogapp.CoreApplication
import com.example.blogapp.Model.APIClient
import com.example.blogapp.Model.UserResponseModel
import com.example.blogapp.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    var valid_email: String? = null
    val REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)
        val rootView = window.decorView.rootView

        if (getConnectionType() == 0)
            dialogMessenger("Warning !", "Please check the network connection !")

        if (CoreApplication.instance.getUser() != null) {
            val emailSave = CoreApplication.instance.getUser()!!.email
            var passSave = CoreApplication.instance.getUser()!!.plainPassword
            edEmail.setText(emailSave)
            edPassword.setText(passSave)
        }

        Handler().postDelayed({
            loader.isVisible = false
        }, 2500)

        requestPermission()

        register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        login.setOnClickListener {
            closeKeyBoard()
            if (Is_Valid_Email(edEmail) && Is_Valid_Pass(edPassword)) {

                login.isEnabled = false
                containerLogin.isEnabled = false
                loader.isVisible = true

                val email = edEmail.text.toString().trim()
                val password = edPassword.text.toString().trim()

                signIn(email, password, rootView)
            }
        }
    }

    private fun signIn(email: String, password: String, v: View) {
        APIClient.instance.signin(email, password)
            .enqueue(object : Callback<UserResponseModel> {
                override fun onFailure(call: Call<UserResponseModel>, t: Throwable) {
                    loader.visibility = View.GONE
                    login.isEnabled = true
                    containerLogin.isEnabled = true
                    t.message?.let { it1 -> dialogMessenger("Warning !", it1) }
                }

                override fun onResponse(
                    call: Call<UserResponseModel>,
                    response: Response<UserResponseModel>
                ) {
                    login.isEnabled = true
                    containerLogin.isEnabled = true
                    loader.visibility = View.GONE
                    if (response.body()?.success == true) {
                        response.body()!!.user.plainPassword =
                            edPassword.text.toString()
                        CoreApplication.instance.saveUser(response.body()!!.user)
                        val intent =
                            Intent(this@LoginActivity, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else Snackbar.make(
                        v,
                        "Wrong email or password",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            })
    }

    @IntRange(from = 0, to = 3)
    fun getConnectionType(): Int {
        var result = 0 // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = this.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (cm != null) {
                val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
                if (capabilities != null) {
                    if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                        result = 2
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                        result = 1
                    } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                        result = 3
                    }
                }
            }
        } else {
            if (cm != null) {
                val activeNetwork = cm.activeNetworkInfo
                if (activeNetwork != null) {
                    // connected to the internet
                    if (activeNetwork.type == ConnectivityManager.TYPE_WIFI) {
                        result = 2
                    } else if (activeNetwork.type == ConnectivityManager.TYPE_MOBILE) {
                        result = 1
                    } else if (activeNetwork.type == ConnectivityManager.TYPE_VPN) {
                        result = 3
                    }
                }
            }
        }
        return result
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ),
            REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CODE -> if (grantResults.isNotEmpty()) {
                val locationAccepted =
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                val cameraAccepted =
                    grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (locationAccepted && cameraAccepted)
//                    view?.let {
//                        Snackbar.make(
//                            it,
//                            "Permission Granted, Now you can access location data and camera.",
//                            Snackbar.LENGTH_LONG
//                        ).show()
//                    }
                else {
//                    view?.let {
//                        Snackbar.make(
//                            it,
//                            "Permission Denied, You cannot access location data and camera.",
//                            Snackbar.LENGTH_LONG
//                        ).show()
//                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                            showMessageOKCancel("You need to allow access to both the permissions",
                                DialogInterface.OnClickListener { dialog, which ->
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(
                                            arrayOf(
                                                Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.CAMERA
                                            ),
                                            REQUEST_CODE
                                        )
                                    }
                                })
                            return
                        }
                    }
                }
            }
        }
    }


    private fun showMessageOKCancel(
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        android.app.AlertDialog.Builder(this@LoginActivity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun Is_Valid_Email(edt: EditText): Boolean {
        var flag = false
        if (isEmailValid(edt.text.toString()) == false) {
            edt.error = "Invalid Email Address"
            valid_email = null
            edt.requestFocus()
        } else {
            valid_email = edt.text.toString()
            flag = true
        }
        return flag
    }

    private fun isEmailValid(email: CharSequence?): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    private fun Is_Valid_Pass(edt: EditText): Boolean {
        // || edt.text.length < 6
        var flag = false
        if (edt.text.toString().isEmpty()) {
            edt.error = "Invalid Password, at least 6 chars"
            edt.requestFocus()
        } else
            flag = true
        return flag
    }

    private fun closeKeyBoard() {
        val view = this.currentFocus
        if (view != null) {
            val imm =
                (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    fun dialogMessenger(title: String, message: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle(title)
        alertDialog.setMessage(message)
        alertDialog.setPositiveButton("OK") { dialog, which -> finish()}
        alertDialog.show()
    }

}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}


