package com.latihan.kurirdirectoryapp

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.latihan.kurirdirectoryapp.databinding.ActivityDetailBinding

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var favoriteHelper: FavoriteHelper
    private var currentUser: UserResponse? = null

    companion object {
        const val EXTRA_USER = "extra_user"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        favoriteHelper = FavoriteHelper(this)

        currentUser = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra(EXTRA_USER, UserResponse::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra(EXTRA_USER) as? UserResponse
        }

        val user = currentUser
        if (user == null) {
            Toast.makeText(this, "Data kurir tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupViews(user)
        setupActions(user)
    }

    private fun setupViews(user: UserResponse) {
        // Hero Profile Card
        binding.tvDetailAvatar.text = user.getInitials()
        binding.tvDetailName.text = user.name
        binding.tvDetailUsername.text = "@${user.username}"
        binding.tvDetailId.text = "ID #${user.id}"

        // Contact Information
        binding.tvDetailEmail.text = user.email
        binding.tvDetailPhone.text = user.phone
        binding.tvDetailWebsite.text = user.website

        // Company Information
        binding.tvDetailCompanyName.text = user.company?.name ?: "-"
        binding.tvDetailCatchphrase.text = if (!user.company?.catchPhrase.isNullOrBlank()) {
            "\"${user.company?.catchPhrase}\""
        } else {
            "-"
        }
        binding.tvDetailBs.text = user.company?.bs ?: "-"

        // Address Information
        val streetPart = listOfNotNull(user.address?.street, user.address?.suite).filter { it.isNotBlank() }
        binding.tvDetailStreet.text = if (streetPart.isNotEmpty()) streetPart.joinToString(", ") else "-"

        val cityPart = listOfNotNull(user.address?.city, user.address?.zipcode).filter { it.isNotBlank() }
        binding.tvDetailCityZip.text = if (cityPart.isNotEmpty()) cityPart.joinToString(", ") else "-"

        val lat = user.address?.geo?.lat
        val lng = user.address?.geo?.lng
        binding.tvDetailGeo.text = if (!lat.isNullOrBlank() && !lng.isNullOrBlank()) {
            "Lat: $lat, Lng: $lng"
        } else {
            "-"
        }

        // Initial Favorite State
        updateFavoriteIcon(favoriteHelper.isFavorite(user.id))
    }

    private fun setupActions(user: UserResponse) {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnFavoriteDetail.setOnClickListener {
            val isNowFavorite = favoriteHelper.toggleFavorite(user.id)
            updateFavoriteIcon(isNowFavorite)
            val msg = if (isNowFavorite) {
                getString(R.string.status_favorite_added)
            } else {
                getString(R.string.status_favorite_removed)
            }
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }

        binding.btnCopyEmail.setOnClickListener {
            copyToClipboard("Email Kurir", user.email)
            Toast.makeText(this, getString(R.string.toast_email_copied), Toast.LENGTH_SHORT).show()
        }

        binding.btnCopyPhone.setOnClickListener {
            copyToClipboard("Telepon Kurir", user.phone)
            Toast.makeText(this, getString(R.string.toast_phone_copied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFavoriteIcon(isFavorite: Boolean) {
        if (isFavorite) {
            binding.btnFavoriteDetail.setImageResource(R.drawable.ic_favorite_filled)
        } else {
            binding.btnFavoriteDetail.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun copyToClipboard(label: String, text: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
