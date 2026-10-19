package com.hamric.feature.users

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.hamric.domain.model.User
import com.hamric.feature.users.databinding.ItemUserBinding

class UsersAdapter (
    private val onClick:(User) ->Unit
): ListAdapter<User, UsersAdapter.UserViewHolder>(DIFF){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UsersAdapter.UserViewHolder {
        val binding = ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
       holder.bind(getItem(position))
    }

    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(user: User){
            binding.textLogin.text = user.login
            Glide.with(binding.imageAvatar)
                .load(user.avatarUrl)
                .placeholder(android.R.color.darker_gray)
                .circleCrop()
                .into(binding.imageAvatar)

            binding.root.setOnClickListener{
                onClick(user)
            }
        }
    }

    companion object{
        private val DIFF = object: DiffUtil.ItemCallback<User>(){
            override fun areItemsTheSame(
                oldItem: User,
                newItem: User
            )= oldItem.login == newItem.login

            override fun areContentsTheSame(
                oldItem: User,
                newItem: User
            )= oldItem == newItem

        }
    }
}