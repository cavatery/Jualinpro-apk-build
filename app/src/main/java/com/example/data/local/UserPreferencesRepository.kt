package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserProfile(
    val shopName: String = "",
    val shopContact: String = "",
    val shopLocation: String = "",
    val shopMarketplaceLink: String = "",
    val customApiKey: String = "",
    val isProUser: Boolean = true,
    val generationCount: Int = 0
)

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("jualin_ai_prefs", Context.MODE_PRIVATE)

    private val _userProfile = MutableStateFlow(loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadProfile(): UserProfile {
        return UserProfile(
            shopName = prefs.getString("shop_name", "Toko UMKM Berkah") ?: "Toko UMKM Berkah",
            shopContact = prefs.getString("shop_contact", "0812-3456-7890") ?: "0812-3456-7890",
            shopLocation = prefs.getString("shop_location", "Jakarta, Indonesia") ?: "Jakarta, Indonesia",
            shopMarketplaceLink = prefs.getString("shop_marketplace", "shopee.co.id/toko_berkah") ?: "shopee.co.id/toko_berkah",
            customApiKey = prefs.getString("custom_api_key", "") ?: "",
            isProUser = prefs.getBoolean("is_pro_user", true),
            generationCount = prefs.getInt("gen_count", 0)
        )
    }

    fun updateShopProfile(name: String, contact: String, location: String, marketplace: String) {
        prefs.edit()
            .putString("shop_name", name)
            .putString("shop_contact", contact)
            .putString("shop_location", location)
            .putString("shop_marketplace", marketplace)
            .apply()
        _userProfile.value = loadProfile()
    }

    fun saveCustomApiKey(key: String) {
        prefs.edit().putString("custom_api_key", key).apply()
        _userProfile.value = loadProfile()
    }

    fun setProUser(isPro: Boolean) {
        prefs.edit().putBoolean("is_pro_user", isPro).apply()
        _userProfile.value = loadProfile()
    }

    fun incrementGenerationCount() {
        val next = _userProfile.value.generationCount + 1
        prefs.edit().putInt("gen_count", next).apply()
        _userProfile.value = loadProfile()
    }
}
