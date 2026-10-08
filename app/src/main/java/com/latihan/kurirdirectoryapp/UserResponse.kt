package com.latihan.kurirdirectoryapp

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UserResponse(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("address")
    val address: Address? = null,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("website")
    val website: String,
    @SerializedName("company")
    val company: Company? = null
) : Serializable {

    fun getInitials(): String {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return "?"
        val parts = trimmed.split("\\s+".toRegex()).filter { it.isNotEmpty() }
        return if (parts.size >= 2) {
            val first = parts[0].firstOrNull()?.uppercaseChar() ?: ""
            val second = parts[1].firstOrNull()?.uppercaseChar() ?: ""
            "$first$second"
        } else {
            trimmed.take(2).uppercase()
        }
    }
}

data class Address(
    @SerializedName("street")
    val street: String? = null,
    @SerializedName("suite")
    val suite: String? = null,
    @SerializedName("city")
    val city: String? = null,
    @SerializedName("zipcode")
    val zipcode: String? = null,
    @SerializedName("geo")
    val geo: Geo? = null
) : Serializable {
    fun getFullAddress(): String {
        val parts = listOfNotNull(street, suite, city, zipcode).filter { it.isNotBlank() }
        return if (parts.isNotEmpty()) parts.joinToString(", ") else "-"
    }
}

data class Geo(
    @SerializedName("lat")
    val lat: String? = null,
    @SerializedName("lng")
    val lng: String? = null
) : Serializable

data class Company(
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("catchPhrase")
    val catchPhrase: String? = null,
    @SerializedName("bs")
    val bs: String? = null
) : Serializable
