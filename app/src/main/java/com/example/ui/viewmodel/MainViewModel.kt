package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiApiClient
import com.example.data.local.AppDatabase
import com.example.data.local.UserPreferencesRepository
import com.example.data.repository.PromoRepository
import com.example.model.CopywritingTone
import com.example.model.GeneratedPromo
import com.example.model.PhotoAnalysisResult
import com.example.model.ProductInput
import com.example.model.PromotionPlatform
import com.example.model.PromoType
import com.example.model.TargetAudience
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

data class PromoUiState(
    val isGenerating: Boolean = false,
    val isAnalyzingPhoto: Boolean = false,
    val currentInput: ProductInput = ProductInput(),
    val latestResult: GeneratedPromo? = null,
    val selectedResultDetail: GeneratedPromo? = null,
    val photoAnalysisResult: PhotoAnalysisResult? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val historySearchQuery: String = "",
    val activeHistoryTab: Int = 0
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val userPrefs = UserPreferencesRepository(application)
    val repository = PromoRepository(
        historyDao = db.historyDao(),
        userPrefs = userPrefs,
        geminiClient = GeminiApiClient()
    )

    private val _uiState = MutableStateFlow(PromoUiState())
    val uiState: StateFlow<PromoUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    val userProfileState = repository.userProfileState

    val allHistory: StateFlow<List<GeneratedPromo>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteHistory: StateFlow<List<GeneratedPromo>> = repository.favoriteHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalHistoryCount: StateFlow<Int> = repository.totalHistoryCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        val profile = userProfileState.value
        _uiState.value = _uiState.value.copy(
            currentInput = _uiState.value.currentInput.copy(
                shopName = profile.shopName,
                shopContact = profile.shopContact,
                shopLocation = profile.shopLocation
            )
        )
    }

    fun updateInput(block: (ProductInput) -> ProductInput) {
        _uiState.value = _uiState.value.copy(
            currentInput = block(_uiState.value.currentInput)
        )
    }

    fun setPhotoUri(uri: Uri?, context: Context) {
        if (uri == null) {
            _uiState.value = _uiState.value.copy(
                currentInput = _uiState.value.currentInput.copy(
                    photoUri = null,
                    photoBase64 = null,
                    photoUris = emptyList(),
                    photoBase64List = emptyList()
                )
            )
            return
        }
        addPhotoUris(listOf(uri), context)
    }

    fun addPhotoUris(uris: List<Uri>, context: Context) {
        if (uris.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUris = _uiState.value.currentInput.photoUris.toMutableList()
                val currentBase64s = _uiState.value.currentInput.photoBase64List.toMutableList()
                val targetSize = 800

                val remainingSlots = (10 - currentUris.size).coerceAtLeast(0)
                val urisToAdd = uris.take(remainingSlots)

                for (uri in urisToAdd) {
                    val uriStr = uri.toString()
                    if (currentUris.contains(uriStr)) continue

                    val bitmap: Bitmap? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        val source = ImageDecoder.createSource(context.contentResolver, uri)
                        ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                            val origWidth = info.size.width
                            val origHeight = info.size.height
                            if (origWidth > targetSize || origHeight > targetSize) {
                                val maxDim = maxOf(origWidth, origHeight)
                                val sample = (maxDim / targetSize).coerceAtLeast(1)
                                decoder.setTargetSampleSize(sample)
                            }
                        }
                    } else {
                        var options = BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        var inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                        BitmapFactory.decodeStream(inputStream, null, options)
                        inputStream?.close()

                        val origWidth = options.outWidth
                        val origHeight = options.outHeight
                        var inSampleSize = 1

                        if (origHeight > targetSize || origWidth > targetSize) {
                            val halfHeight = origHeight / 2
                            val halfWidth = origWidth / 2
                            while ((halfHeight / inSampleSize) >= targetSize && (halfWidth / inSampleSize) >= targetSize) {
                                inSampleSize *= 2
                            }
                        }

                        options = BitmapFactory.Options().apply {
                            this.inSampleSize = inSampleSize
                            inPreferredConfig = Bitmap.Config.ARGB_8888
                        }
                        inputStream = context.contentResolver.openInputStream(uri)
                        val decoded = BitmapFactory.decodeStream(inputStream, null, options)
                        inputStream?.close()
                        decoded
                    }

                    if (bitmap != null) {
                        val scaled = resizeBitmap(bitmap, targetSize)
                        val outputStream = ByteArrayOutputStream()
                        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)

                        if (scaled != bitmap) {
                            scaled.recycle()
                        }
                        bitmap.recycle()

                        currentUris.add(uriStr)
                        currentBase64s.add(base64String)
                    }
                }

                withContext(Dispatchers.Main) {
                    val finalUris = currentUris.take(10)
                    val finalB64s = currentBase64s.take(10)
                    _uiState.value = _uiState.value.copy(
                        currentInput = _uiState.value.currentInput.copy(
                            photoUri = finalUris.firstOrNull(),
                            photoBase64 = finalB64s.firstOrNull(),
                            photoUris = finalUris,
                            photoBase64List = finalB64s
                        )
                    )
                    _toastEvent.emit("${finalUris.size}/10 Foto Korsel siap digunakan! 📸")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Failed to process photos: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _toastEvent.emit("Gagal memproses foto: ${e.message}")
                }
            }
        }
    }

    fun removePhotoAtIndex(index: Int) {
        val currentUris = _uiState.value.currentInput.photoUris.toMutableList()
        val currentBase64s = _uiState.value.currentInput.photoBase64List.toMutableList()
        if (index in currentUris.indices) {
            currentUris.removeAt(index)
            if (index in currentBase64s.indices) {
                currentBase64s.removeAt(index)
            }
            _uiState.value = _uiState.value.copy(
                currentInput = _uiState.value.currentInput.copy(
                    photoUri = currentUris.firstOrNull(),
                    photoBase64 = currentBase64s.firstOrNull(),
                    photoUris = currentUris,
                    photoBase64List = currentBase64s
                )
            )
        }
    }

    fun setPrimaryPhoto(index: Int) {
        val currentUris = _uiState.value.currentInput.photoUris.toMutableList()
        val currentBase64s = _uiState.value.currentInput.photoBase64List.toMutableList()
        if (index in currentUris.indices && index != 0) {
            val uri = currentUris.removeAt(index)
            currentUris.add(0, uri)
            if (index in currentBase64s.indices) {
                val b64 = currentBase64s.removeAt(index)
                currentBase64s.add(0, b64)
            }
            _uiState.value = _uiState.value.copy(
                currentInput = _uiState.value.currentInput.copy(
                    photoUri = currentUris.firstOrNull(),
                    photoBase64 = currentBase64s.firstOrNull(),
                    photoUris = currentUris,
                    photoBase64List = currentBase64s
                )
            )
            viewModelScope.launch {
                _toastEvent.emit("Foto utama (#1) diperbarui! ⭐")
            }
        }
    }

    fun clearAllPhotos() {
        _uiState.value = _uiState.value.copy(
            currentInput = _uiState.value.currentInput.copy(
                photoUri = null,
                photoBase64 = null,
                photoUris = emptyList(),
                photoBase64List = emptyList()
            )
        )
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxSize && height <= maxSize) return bitmap

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxSize
            newHeight = (maxSize / ratio).toInt()
        } else {
            newHeight = maxSize
            newWidth = (maxSize * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }

    fun analyzeCurrentPhoto() {
        val input = _uiState.value.currentInput
        if (input.photoBase64.isNullOrBlank()) {
            viewModelScope.launch {
                _toastEvent.emit("Pilih atau upload foto produk terlebih dahulu!")
            }
            return
        }

        _uiState.value = _uiState.value.copy(isAnalyzingPhoto = true, errorMessage = null)
        viewModelScope.launch {
            val result = repository.analyzePhoto(
                productName = input.productName,
                extraInfo = input.uspList,
                photoBase64 = input.photoBase64
            )
            val analysis = result.getOrNull()
            _uiState.value = _uiState.value.copy(
                isAnalyzingPhoto = false,
                photoAnalysisResult = analysis
            )
            if (analysis != null) {
                val updatedUsp = if (input.uspList.isBlank()) {
                    "Karakter ${analysis.productVibe}, kemasan ${analysis.packagingType}, warna ${analysis.detectedColors}"
                } else {
                    input.uspList
                }
                _uiState.value = _uiState.value.copy(
                    currentInput = input.copy(uspList = updatedUsp)
                )
                _toastEvent.emit("Foto berhasil dianalisis oleh AI! ✨")
            } else {
                _toastEvent.emit("Analisis foto selesai!")
            }
        }
    }

    fun generatePromotion(onSuccess: (GeneratedPromo) -> Unit) {
        val input = _uiState.value.currentInput
        if (input.productName.isBlank()) {
            viewModelScope.launch {
                _toastEvent.emit("Harap masukkan Nama Produk terlebih dahulu!")
            }
            return
        }

        _uiState.value = _uiState.value.copy(isGenerating = true, errorMessage = null)

        viewModelScope.launch {
            val result = repository.generatePromotion(input)
            _uiState.value = _uiState.value.copy(isGenerating = false)

            result.onSuccess { promo ->
                _uiState.value = _uiState.value.copy(
                    latestResult = promo,
                    selectedResultDetail = promo,
                    successMessage = "Materi promosi berhasil dibuat!"
                )
                _toastEvent.emit("Materi jualan siap digunakan! 🚀")
                onSuccess(promo)
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Gagal membuat materi: ${err.message}"
                )
                _toastEvent.emit("Terjadi kendala: ${err.message}")
            }
        }
    }

    fun setSelectedResultDetail(promo: GeneratedPromo) {
        _uiState.value = _uiState.value.copy(selectedResultDetail = promo)
    }

    fun updateResultDetail(updatedPromo: GeneratedPromo) {
        _uiState.value = _uiState.value.copy(
            selectedResultDetail = updatedPromo,
            latestResult = if (_uiState.value.latestResult?.id == updatedPromo.id) updatedPromo else _uiState.value.latestResult
        )
    }

    fun toggleFavorite(promo: GeneratedPromo) {
        viewModelScope.launch {
            repository.toggleFavorite(promo.id, promo.isFavorite)
            val updated = promo.copy(isFavorite = !promo.isFavorite)
            _uiState.value = _uiState.value.copy(
                selectedResultDetail = if (_uiState.value.selectedResultDetail?.id == promo.id) updated else _uiState.value.selectedResultDetail
            )
            _toastEvent.emit(if (!promo.isFavorite) "Disimpan ke Favorit ❤️" else "Dihapus dari Favorit")
        }
    }

    fun deleteHistory(promoId: Long) {
        viewModelScope.launch {
            repository.deleteHistory(promoId)
            _toastEvent.emit("Riwayat berhasil dihapus")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
            _toastEvent.emit("Semua riwayat telah dibersihkan")
        }
    }

    fun setHistorySearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(historySearchQuery = query)
    }

    fun setActiveHistoryTab(tab: Int) {
        _uiState.value = _uiState.value.copy(activeHistoryTab = tab)
    }

    fun upgradeToPro(isPro: Boolean = true) {
        repository.setProUser(isPro)
        viewModelScope.launch {
            _toastEvent.emit(if (isPro) "Selamat! Anda sekarang akun Jualin AI PRO ⭐" else "Beralih ke Akun Gratis")
        }
    }

    fun testApiConnection(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val res = repository.testConnection()
            res.onSuccess { msg ->
                _toastEvent.emit("Koneksi Gemini AI Aktif! ✅")
                onResult(true, msg)
            }.onFailure { err ->
                val errorMsg = err.message ?: "Gagal terhubung ke Gemini AI"
                _toastEvent.emit("Gagal: $errorMsg")
                onResult(false, errorMsg)
            }
        }
    }

    fun saveCustomApiKey(key: String) {
        repository.setCustomApiKey(key)
        viewModelScope.launch {
            _toastEvent.emit("API Key berhasil disimpan!")
        }
    }

    fun updateShopProfile(name: String, contact: String, location: String, marketplaceLink: String) {
        repository.updateShopProfile(name, contact, location, marketplaceLink)
        _uiState.value = _uiState.value.copy(
            currentInput = _uiState.value.currentInput.copy(
                shopName = name,
                shopContact = contact,
                shopLocation = location
            )
        )
        viewModelScope.launch {
            _toastEvent.emit("Profil toko berhasil diperbarui!")
        }
    }

    fun loadSampleProduct(sampleIndex: Int = 0) {
        val samples = listOf(
            ProductInput(
                productName = "Keripik Pisang Coklat Lumer",
                normalPrice = "Rp25.000",
                promoPrice = "Rp19.900",
                promoType = PromoType.DISCOUNT,
                promoDetail = "Diskon 20% pembelian min 2 bungkus",
                uspList = "Pisang kepok pilihan, coklat lumer tebal khas Belgia, super renyah tanpa bahan pengawet",
                targetAudience = TargetAudience.YOUTH,
                tone = CopywritingTone.VIRAL,
                platform = PromotionPlatform.TIKTOK
            ),
            ProductInput(
                productName = "Gamis Rayon Premium Polos",
                normalPrice = "Rp185.000",
                promoPrice = "Rp149.000",
                promoType = PromoType.FREE_SHIPPING,
                promoDetail = "Gratis Ongkir se-Indonesia",
                uspList = "Bahan katun rayon twill adem, busui & wudhu friendly, jahitan butik rapi, tidak menerawang",
                targetAudience = TargetAudience.HOUSEWIVES,
                tone = CopywritingTone.IBU_IBU,
                platform = PromotionPlatform.WHATSAPP
            ),
            ProductInput(
                productName = "Kopi Susu Gula Aren 1 Liter",
                normalPrice = "Rp85.000",
                promoPrice = "Rp69.000",
                promoType = PromoType.FLASH_SALE,
                promoDetail = "Flash Sale Weekend",
                uspList = "Biji kopi arabika blend lokal fresh brew, gula aren organik murni, creamy pas tidak bikin asam lambung",
                targetAudience = TargetAudience.WORKERS,
                tone = CopywritingTone.CASUAL,
                platform = PromotionPlatform.INSTAGRAM
            )
        )
        val selected = samples[sampleIndex % samples.size]
        _uiState.value = _uiState.value.copy(currentInput = selected)
        viewModelScope.launch {
            _toastEvent.emit("Contoh produk '${selected.productName}' dimuat! 💡")
        }
    }
}
