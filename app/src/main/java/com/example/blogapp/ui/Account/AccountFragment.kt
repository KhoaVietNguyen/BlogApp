package com.example.blogapp.ui.Account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.blogapp.R
import com.example.blogapp.ui.login.LoginActivity


class AccountFragment : Fragment() {

    private lateinit var accountViewModel: AccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        accountViewModel = ViewModelProvider(this).get(AccountViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_account, container, false)
        val textView: TextView = root.findViewById(R.id.text_account)
        accountViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        val btLogout: Button = root.findViewById((R.id.logout))
        btLogout.setOnClickListener {
            activity?.finish()
            startActivity(Intent(activity, LoginActivity::class.java))

        }
        return root
    }
}
