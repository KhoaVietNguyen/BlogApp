package com.example.blogapp.Model

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Activity.CommentActivity

import com.example.blogapp.R
import java.text.ParseException
import java.text.SimpleDateFormat

class PostUserAdapter(
    private val dataArrayList: List<Post>,
    private var itemClickListener: ItemClickListener?
) :
    RecyclerView.Adapter<PostUserAdapter.BaseViewHolder<*>>() {
    var view: View? = null

    interface ItemClickListener {
        fun onClick(item: Post, position: Int, view: View)
        fun onLongClick(item: Post, position: Int, v: View)
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
                    .inflate(R.layout.user_post_items, parent, false)
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
        return when (dataArrayList[position]) {
            else -> VIEW_TYPE_ITEM
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
        val title: TextView = view.findViewById(R.id.blogTitleUser)
        val blogImage: ImageView = view.findViewById(R.id.blogImageUser)
        val tag: TextView = view.findViewById(R.id.tvTagUser)
        val blogDateUser: TextView = view.findViewById(R.id.blogDateUser)

        fun setItemClickListener(itemClickListener: ItemClickListener?) {
            this.itemClickListener = itemClickListener
        }

        private var itemClickListener: ItemClickListener? = null

//

        fun initialize(item: Post, action: ItemClickListener) {
            itemView.setOnClickListener {
                view?.let { it1 ->
                    action.onClick(
                        item, adapterPosition,
                        it1
                    )
                }
            }
            itemView.setOnLongClickListener {
                view?.let { it1 ->
                    action.onLongClick(
                        item, adapterPosition,
                        it1
                    )
                }; false
            }
        }

        init {
            // view.setOnClickListener(this)
        }

        override fun bind(item: Post) {
            tag.text = item.tag?.name
            title.text = item.title
            blogDateUser.text = item.date
        }

    }

    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = dataArrayList[position]
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(element)
                itemClickListener?.let { holder.initialize(element, it) }
//                holder.setItemClickListener(object : ItemClickListener {
//                    override fun onClick(view: View, position: Int) {
//                        val b = Bundle()
//                        b.putParcelable("ParcelKey", element)
//                        val i = Intent(view.context, CommentActivity::class.java)
//                        i.putExtras(b)
//                        i.putExtra("key", key)
//                        view.context.startActivity(i)
//                    }
//
//                    override fun onLongClick(view: Post, position: Int) {
//                    }
//
//                })

                view?.let {
                    Glide.with(it).load(element.image)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.blogImage)
                }

                try {
                    val string = element.date
                    if (string == null)
                        holder.blogDateUser.text = ""
                    else {
                        val date =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(string)
                        val formattedDate =
                            SimpleDateFormat("EEE dd/MM/yyyy h:mm aaa").format(date)
                        holder.blogDateUser.text = formattedDate
                    }

                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
            is LoadingViewHolder -> holder.bind(element as Any)
            else -> throw IllegalArgumentException()
        }
    }


}