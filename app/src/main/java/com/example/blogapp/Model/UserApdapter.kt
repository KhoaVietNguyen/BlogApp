package com.example.blogapp.Model

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Activity.UserInfoActivity
import com.example.blogapp.CoreApplication
import com.example.blogapp.R

class UserAdapter(
    private val dataArrayList: List<User>,
    private var itemClickListener: ItemClickListener?
) :
    RecyclerView.Adapter<UserAdapter.BaseViewHolder<*>>() {
    var view: View? = null

    interface ItemClickListener {
        fun onClick(item: User, position: Int, view: View)
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
                    .inflate(R.layout.user_items, parent, false)
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

    internal inner class ItemViewHolder(view: View) : BaseViewHolder<User>(view) {
        val username: TextView = view.findViewById(R.id.username)
        val email: TextView = view.findViewById(R.id.email)
        val avatar: ImageView = view.findViewById(R.id.image_profile)
        val follow: Button = view.findViewById(R.id.btn_follow)

        fun setItemClickListener(itemClickListener: ItemClickListener?) {
            this.itemClickListener = itemClickListener
        }

        private var itemClickListener: ItemClickListener? = null

//

        fun initialize(item: User, action: ItemClickListener) {
            follow.setOnClickListener {
                view?.let { it1 ->
                    action.onClick(
                        item, adapterPosition,
                        it1
                    )
                }
            }
//            avatar.setOnClickListener { view?.let { it1 ->
//                action.onLongClick(item, adapterPosition,
//                    it1
//                )
//            } }
        }

        init {
            // view.setOnClickListener(this)
        }

        override fun bind(item: User) {
            username.text = item.name
            email.text = item.email
            follow.visibility = View.VISIBLE
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = dataArrayList[position]
        when (holder) {
            is ItemViewHolder -> {
                holder.bind(element)
                holder.avatar.setOnClickListener {
                    val i = Intent(view?.context, UserInfoActivity::class.java)
                    i.putExtra("idUser", element.id)
                    i.putExtra("key", "key")
                    view?.context?.startActivity(i)
                }
                val idUser = CoreApplication.instance.getUser()?.id
                if (element.followed?.contains(idUser)!!) {
                    holder.follow.text = "Following"
                    holder.follow.setBackgroundResource(R.drawable.button_background_conf)
                    holder.follow.setTextColor(Color.WHITE)
                } else {
                    holder.follow.setBackgroundResource(R.drawable.button_background)
                    view?.context?.let { ContextCompat.getColor(it, R.color.colorPrimary) }?.let {
                        holder.follow.setTextColor(
                            it
                        )
                    }
                    holder.follow.text = "+ Follow"
                }
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
                    Glide.with(it).load(element.avatar)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.placeholder)
                        .into(holder.avatar)
                }

            }
            is LoadingViewHolder -> holder.bind(element as Any)
            else -> throw IllegalArgumentException()
        }
    }
}