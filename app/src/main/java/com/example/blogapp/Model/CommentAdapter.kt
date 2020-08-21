package com.example.blogapp.Model

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Activity.UserInfoActivity
import com.example.blogapp.R
import com.example.blogapp.ui.Account.AccountFragment
import de.hdodenhof.circleimageview.CircleImageView


class CommentAdapter(
    private val dataArrayList: List<Comment>,
    private var itemClickListener: ItemClickListener?,
    private var idUser: String
) :
    RecyclerView.Adapter<CommentAdapter.BaseViewHolder<*>>() {


    interface ItemClickListener {
        fun onClick(item: Comment, position: Int)
        fun onLongClick(item: Comment, position: Int)
    }

    abstract class BaseViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {

        abstract fun bind(item: T)
    }

    companion object {
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            VIEW_TYPE_ITEM -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.comment_list_item, parent, false)
                ItemViewHolder(view)
            }
            VIEW_TYPE_LOADING -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_loading, parent, false)
                LoadingViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val comparable = dataArrayList[position]
        return when (comparable) {
            is Comment -> VIEW_TYPE_ITEM
            is Any -> VIEW_TYPE_LOADING
            else -> throw IllegalArgumentException("Invalid type of data " + position)
        }
    }

    override fun getItemCount(): Int {
        return dataArrayList.size
    }

    internal inner class LoadingViewHolder(view: View) : BaseViewHolder<Any>(view) {
        var progressBar: ProgressBar = view.findViewById(R.id.progressBar)

        override fun bind(item: Any) {

        }
    }

    internal inner class ItemViewHolder(view: View) : BaseViewHolder<Comment>(view) {
        val avatar: CircleImageView = view.findViewById(R.id.commentProfilePic)
        val name: TextView = view.findViewById(R.id.commentUN)
        private val comment: TextView = view.findViewById(R.id.commentTextMessage)
        val likeCmt: TextView = view.findViewById(R.id.tvLikeCmt)

        fun setItemClickListener(itemClickListener: ItemClickListener?) {
            this.itemClickListener = itemClickListener
        }

        private var itemClickListener: ItemClickListener? = null

//        override fun onClick(v: View) {
//            itemClickListener!!.onClick(v, adapterPosition)
//        }

        fun initialize(item: Comment, action: ItemClickListener) {
            comment.setOnLongClickListener { action.onLongClick(item, adapterPosition); false }
            likeCmt.setOnClickListener { action.onClick(item, adapterPosition) }
        }

        override fun bind(item: Comment) {

            comment.text = item.content
            name.text = item.author?.name
            Glide.with(itemView).load(item.author?.avatar)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.ic_photo_camera_black_24dp)
                .into(avatar)
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = dataArrayList[position]
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(element)
                holder.avatar.setOnClickListener { View ->
                    if (!element.author?.id.equals(idUser)) {
                        val i = Intent(View?.context, UserInfoActivity::class.java)
                        i.putExtra("idUser", element.author?.id)
                        i.putExtra("key", "key")
                        View?.context?.startActivity(i)
                    }
                }
                if (element.fans?.contains(idUser)!!) {
                    holder.likeCmt.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_thumb_up_black_liked_24dp,
                        0,
                        0,
                        0
                    )
                } else
                    holder.likeCmt.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_thumb_up_black_24dp,
                        0,
                        0,
                        0
                    )
                holder.likeCmt.text = element.fans!!.size.toString()
                itemClickListener?.let { holder.initialize(element, it) }
            }
            is LoadingViewHolder -> holder.bind(element as Any)
            else -> throw IllegalArgumentException()
        }
    }
}