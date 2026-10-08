package com.latihan.kurirdirectoryapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.latihan.kurirdirectoryapp.databinding.ItemKurirBinding

class UserAdapter(
    private val favoriteHelper: FavoriteHelper,
    private val onItemClick: (UserResponse) -> Unit,
    private val onFavoriteClick: (UserResponse, Boolean) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    private val userList = mutableListOf<UserResponse>()

    fun submitList(newList: List<UserResponse>) {
        userList.clear()
        userList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemKurirBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(userList[position])
    }

    override fun getItemCount(): Int = userList.size

    inner class UserViewHolder(private val binding: ItemKurirBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserResponse) {
            binding.tvAvatarInitials.text = user.getInitials()
            binding.tvName.text = user.name
            binding.tvUsername.text = "@${user.username}"
            binding.tvCompany.text = user.company?.name ?: "Tanpa Perusahaan"
            binding.tvEmail.text = user.email
            binding.tvPhone.text = user.phone

            val isFav = favoriteHelper.isFavorite(user.id)
            updateFavoriteIcon(isFav)

            binding.cardKurir.setOnClickListener {
                onItemClick(user)
            }

            binding.btnFavorite.setOnClickListener {
                val newStatus = favoriteHelper.toggleFavorite(user.id)
                updateFavoriteIcon(newStatus)
                onFavoriteClick(user, newStatus)
            }
        }

        private fun updateFavoriteIcon(isFavorite: Boolean) {
            if (isFavorite) {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_filled)
            } else {
                binding.btnFavorite.setImageResource(R.drawable.ic_favorite_border)
            }
        }
    }
}
