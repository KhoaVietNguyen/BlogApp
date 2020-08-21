package com.example.blogapp.Model

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Activity.CommentActivity
import com.example.blogapp.Activity.UserInfoActivity
import com.example.blogapp.CoreApplication
import com.example.blogapp.R
import com.example.blogapp.ui.home.HomeFragment
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_comments.*
import java.text.ParseException
import java.text.SimpleDateFormat

//private val fragment: View
class PostAdapter(private val dataArrayList: List<Post>) :
    RecyclerView.Adapter<PostAdapter.BaseViewHolder<*>>() {
    var view: View? = null

    val idUser = CoreApplication.instance.getUser()?._id


    interface ItemClickListener {
        fun onClick(view: View, position: Int)
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
                    .inflate(R.layout.blog_list_item, parent, false)
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
            is Post -> VIEW_TYPE_ITEM
            is Any -> VIEW_TYPE_LOADING
            else -> throw IllegalArgumentException("Invalid type of data " + position)
        }
        //return if (dataArrayList[position] == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    //
    override fun getItemCount(): Int {
        return dataArrayList.size
    }

    internal inner class LoadingViewHolder(view: View) : BaseViewHolder<Any>(view) {
        var progressBar: ProgressBar = view.findViewById(R.id.progressBar)

        override fun bind(item: Any) {

        }
    }

    internal inner class ItemViewHolder(view: View) : BaseViewHolder<Post>(view) {
        val title: TextView = view.findViewById(R.id.blogTitle)
        val avatar: CircleImageView = view.findViewById(R.id.commentProfilePic)
        val time: TextView = view.findViewById(R.id.blogDate)
        val name: TextView = view.findViewById(R.id.commentUsername)
        val blogImage: ImageView = view.findViewById(R.id.blogImage)
        var blog_like: ImageView = view.findViewById(R.id.blog_like)
        val blog_like_count: TextView = view.findViewById(R.id.blog_like_count)
        val blog_comment: ImageView = view.findViewById(R.id.blog_comment)
        val blog_comment_count: TextView = view.findViewById(R.id.blog_comment_count)
        val tag: TextView = view.findViewById(R.id.tvTag)
        val postLayout: ConstraintLayout = view.findViewById(R.id.postLayout)
        val userLayout: ConstraintLayout = view.findViewById(R.id.userLayout)

        fun setItemClickListener(itemClickListener: ItemClickListener?) {
            this.itemClickListener = itemClickListener
        }

        private var itemClickListener: ItemClickListener? = null

//        override fun onClick(v: View) {
//            itemClickListener!!.onClick(v, adapterPosition)
//        }


        @SuppressLint("SetTextI18n")
        override fun bind(item: Post) {
            tag.text = item.tag?.name
            title.text = item.title
            name.text = item.author?.name
            blog_comment_count.text = item.cmt?.size.toString() + " Comments"
            blog_like_count.text = item.fan?.size.toString() + " Likes"
        }
    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = dataArrayList[position]
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(element)
                holder.postLayout.setOnClickListener {
                    val b = Bundle()
                    b.putParcelable("ParcelKey", element)
                    val i = Intent(view?.context, CommentActivity::class.java)
                    i.putExtras(b)
                    view?.context?.startActivity(i)
                }

                holder.userLayout.setOnClickListener { view ->
                    if (element.author?.id.equals(idUser))
                        view.findNavController().navigate(R.id.navigation_Account)
                    else {
                        val i = Intent(view?.context, UserInfoActivity::class.java)
                        i.putExtra("idUser", element.author?.id)
                        view?.context?.startActivity(i)
                    }
                }

                view?.let {
                    Glide.with(it).load(element.author?.avatar)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_photo_camera_black_24dp)
                        .into(holder.avatar)
                }

                view?.let {
                    Glide.with(it).load(element.image)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.ic_image_black_24dp)
                        .into(holder.blogImage)
                }

                try {
                    val string = element.date
                    if (string == null)
                        holder.time.text = ""
                    else {
                        val date =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(string)
                        val formattedDate =
                            SimpleDateFormat("EEE dd/MM/yyyy h:mm aaa").format(date)
                        holder.time.text = formattedDate
                    }

                } catch (e: ParseException) {
                    e.printStackTrace()
                }
                element.fan?.let { checkLiked(it, holder.blog_like) }
            }
            is LoadingViewHolder -> holder.bind(element as Any)
            else -> throw IllegalArgumentException()
        }
    }

    fun checkLiked(listLike: List<String>, imageView: ImageView): Boolean {
        var flag: Boolean = false
        if (listLike.contains(idUser)) {
            imageView.setBackgroundResource(R.drawable.ic_thumb_up_black_liked_24dp)
            flag = true
        } else
            imageView.setBackgroundResource(R.drawable.ic_thumb_up_black_24dp)
        return flag
    }
}