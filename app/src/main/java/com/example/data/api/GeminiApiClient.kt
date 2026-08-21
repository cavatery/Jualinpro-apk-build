package com.example.data.api

import android.util.Log
import com.example.ai.GeminiPromptBuilder
import com.example.model.CarouselSlideItem
import com.example.model.CategorizedHashtags
import com.example.model.CustomerReplyItem
import com.example.model.FollowUpTemplate
import com.example.model.GeneratedPromo
import com.example.model.PhotoAnalysisResult
import com.example.model.ProductInput
import com.example.model.WeeklyDayPlan
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    private val service = retrofit.create(GeminiApiService::class.java)

    private fun getApiKey(customApiKey: String): String {
        if (customApiKey.isNotBlank()) return customApiKey
        return try {
            val field = com.example.BuildConfig::class.java.getField("GEMINI_API_KEY")
            val key = (field.get(null) as? String) ?: ""
            if (key == "MY_GEMINI_API_KEY") "" else key
        } catch (e: Throwable) {
            ""
        }
    }

    companion object {
        private const val PRIMARY_MODEL = "gemini-1.5-flash"
        private val FALLBACK_MODELS = listOf("gemini-2.0-flash", "gemini-2.0-flash-lite")
    }

    private suspend fun executeWithFallback(
        apiKey: String,
        request: GeminiContentRequest
    ): retrofit2.Response<GeminiResponse> {
        val modelsToTry = listOf(PRIMARY_MODEL) + FALLBACK_MODELS
        var lastResponse: retrofit2.Response<GeminiResponse>? = null
        for (model in modelsToTry) {
            try {
                val response = service.generateContent(
                    model = model,
                    apiKey = apiKey,
                    request = request
                )
                if (response.isSuccessful) {
                    return response
                }
                lastResponse = response
                // If not 404 and not 429, don't keep trying other models
                if (response.code() != 404 && response.code() != 429) {
                    return response
                }
            } catch (e: Exception) {
                Log.w("GeminiApiClient", "Model $model request failed: ${e.message}")
            }
        }
        return lastResponse ?: throw Exception("Gagal menghubungi server Gemini AI. Silakan periksa koneksi internet Anda.")
    }

    suspend fun testConnection(customApiKey: String = ""): Result<String> {
        val apiKey = getApiKey(customApiKey)
        if (apiKey.isBlank()) {
            return Result.failure(Exception("API Key belum diisi. Masukkan Google Gemini API Key Anda di Pengaturan."))
        }
        return try {
            val request = GeminiContentRequest(
                contents = listOf(
                    ContentItem(
                        parts = listOf(
                            ContentPart(text = "Halo! Balas dengan 1 kalimat pendek: 'Koneksi Jualin AI ke server Gemini berhasil terhubung aktif!'")
                        )
                    )
                )
            )
            val response = executeWithFallback(apiKey, request)
            if (response.isSuccessful) {
                val reply = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Koneksi berhasil terhubung!"
                Result.success(reply.trim())
            } else {
                val errBody = response.errorBody()?.string() ?: response.message()
                Result.failure(Exception("Gagal koneksi (Kode ${response.code()}): $errBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generatePromotion(input: ProductInput, customApiKey: String = ""): GeneratedPromo {
        val apiKey = getApiKey(customApiKey)
        if (apiKey.isBlank()) {
            throw Exception("Google Gemini API Key belum diatur. Silakan masukkan API Key Gemini Anda di menu Pengaturan.")
        }

        val prompt = GeminiPromptBuilder.buildPromotionPrompt(input)
        val parts = mutableListOf<ContentPart>()
        
        // Multi-photo base64 support (up to 10 images)
        if (input.photoBase64List.isNotEmpty()) {
            for (b64 in input.photoBase64List.take(10)) {
                if (b64.isNotBlank()) {
                    parts.add(
                        ContentPart(
                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = b64
                            )
                        )
                    )
                }
            }
        } else if (!input.photoBase64.isNullOrBlank()) {
            parts.add(
                ContentPart(
                    inlineData = InlineData(
                        mimeType = "image/jpeg",
                        data = input.photoBase64
                    )
                )
            )
        }
        parts.add(ContentPart(text = prompt))

        val request = GeminiContentRequest(
            contents = listOf(ContentItem(parts = parts))
        )

        val response = executeWithFallback(apiKey, request)

        if (!response.isSuccessful) {
            val errBody = response.errorBody()?.string() ?: response.message()
            throw Exception("Gagal menghubungi Gemini AI (Kode ${response.code()}): $errBody")
        }

        val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (rawText.isNullOrBlank()) {
            throw Exception("Respon dari Gemini AI kosong. Silakan coba generate ulang.")
        }

        val parsed = parsePromotionJson(rawText, input)
        return parsed ?: throw Exception("Gagal membaca format JSON hasil AI. Silakan klik buat ulang.")
    }

    suspend fun analyzePhoto(photoBase64: String, productName: String, customApiKey: String = ""): PhotoAnalysisResult {
        val apiKey = getApiKey(customApiKey)
        if (apiKey.isBlank()) {
            throw Exception("Google Gemini API Key belum diatur. Silakan masukkan API Key di menu Pengaturan.")
        }
        if (photoBase64.isBlank()) {
            throw Exception("Foto produk belum dipilih atau tidak valid.")
        }

        val prompt = GeminiPromptBuilder.buildPhotoAnalysisPrompt(productName)
        val request = GeminiContentRequest(
            contents = listOf(
                ContentItem(
                    parts = listOf(
                        ContentPart(
                            inlineData = InlineData(
                                mimeType = "image/jpeg",
                                data = photoBase64
                            )
                        ),
                        ContentPart(text = prompt)
                    )
                )
            )
        )

        val response = executeWithFallback(apiKey, request)

        if (!response.isSuccessful) {
            val errBody = response.errorBody()?.string() ?: response.message()
            throw Exception("Gagal analisis foto via Gemini Vision (Kode ${response.code()}): $errBody")
        }

        val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (rawText.isNullOrBlank()) {
            throw Exception("Respon analisis foto dari Gemini AI kosong.")
        }

        val parsed = parsePhotoAnalysisJson(rawText)
        return parsed ?: throw Exception("Gagal memproses hasil analisis visual AI.")
    }

    private fun cleanJson(raw: String): String {
        var cleaned = raw.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        return cleaned.trim()
    }

    private fun parsePromotionJson(rawText: String, input: ProductInput): GeneratedPromo? {
        return try {
            val jsonStr = cleanJson(rawText)
            val obj = JSONObject(jsonStr)

            val hashtags = mutableListOf<String>()
            val hashtagArray = obj.optJSONArray("hashtags")
            if (hashtagArray != null) {
                for (i in 0 until hashtagArray.length()) {
                    hashtags.add(hashtagArray.getString(i))
                }
            }

            val catHashtagsObj = obj.optJSONObject("categorizedHashtags")
            val viralList = mutableListOf<String>()
            val nicheList = mutableListOf<String>()
            val localList = mutableListOf<String>()
            val promoList = mutableListOf<String>()

            if (catHashtagsObj != null) {
                val viralArr = catHashtagsObj.optJSONArray("viralTrending")
                if (viralArr != null) {
                    for (i in 0 until viralArr.length()) viralList.add(viralArr.getString(i))
                }
                val nicheArr = catHashtagsObj.optJSONArray("nicheCategory")
                if (nicheArr != null) {
                    for (i in 0 until nicheArr.length()) nicheList.add(nicheArr.getString(i))
                }
                val localArr = catHashtagsObj.optJSONArray("localUmkm")
                if (localArr != null) {
                    for (i in 0 until localArr.length()) localList.add(localArr.getString(i))
                }
                val promoArr = catHashtagsObj.optJSONArray("promoDiscount")
                if (promoArr != null) {
                    for (i in 0 until promoArr.length()) promoList.add(promoArr.getString(i))
                }
            }

            val categorizedHashtags = CategorizedHashtags(
                viralTrending = viralList,
                nicheCategory = nicheList,
                localUmkm = localList,
                promoDiscount = promoList
            )

            // If main hashtags list is empty, aggregate from categorized
            if (hashtags.isEmpty()) {
                hashtags.addAll(viralList)
                hashtags.addAll(nicheList)
                hashtags.addAll(localList)
                hashtags.addAll(promoList)
            }

            val alternativeTitles = mutableListOf<String>()
            val titlesArray = obj.optJSONArray("alternativeTitles")
            if (titlesArray != null) {
                for (i in 0 until titlesArray.length()) {
                    alternativeTitles.add(titlesArray.getString(i))
                }
            }

            val viralHooks = mutableListOf<String>()
            val hooksArray = obj.optJSONArray("viralHooks")
            if (hooksArray != null) {
                for (i in 0 until hooksArray.length()) {
                    viralHooks.add(hooksArray.getString(i))
                }
            }

            val ctaVariations = mutableListOf<String>()
            val ctasArray = obj.optJSONArray("ctaVariations")
            if (ctasArray != null) {
                for (i in 0 until ctasArray.length()) {
                    ctaVariations.add(ctasArray.getString(i))
                }
            }

            val adVariations = mutableMapOf<String, String>()
            val adObj = obj.optJSONObject("adVariations")
            if (adObj != null) {
                val keys = adObj.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    adVariations[key] = adObj.optString(key)
                }
            }

            val weeklyPlan = mutableListOf<WeeklyDayPlan>()
            val planArray = obj.optJSONArray("weeklyPlan")
            if (planArray != null) {
                for (i in 0 until planArray.length()) {
                    val item = planArray.getJSONObject(i)
                    weeklyPlan.add(
                        WeeklyDayPlan(
                            dayNumber = item.optInt("dayNumber", i + 1),
                            dayName = item.optString("dayName", "Hari ${i + 1}"),
                            theme = item.optString("theme", "Tema Konten"),
                            concept = item.optString("concept", "Konsep"),
                            readyCaption = item.optString("readyCaption", "")
                        )
                    )
                }
            }

            val quickReplies = mutableListOf<CustomerReplyItem>()
            val qrArray = obj.optJSONArray("quickReplies")
            if (qrArray != null) {
                for (i in 0 until qrArray.length()) {
                    val item = qrArray.getJSONObject(i)
                    quickReplies.add(
                        CustomerReplyItem(
                            id = item.optString("id", "qr_$i"),
                            questionCategory = item.optString("questionCategory", "Umum"),
                            questionSample = item.optString("questionSample", ""),
                            suggestedReply = item.optString("suggestedReply", "")
                        )
                    )
                }
            }

            val followUps = mutableListOf<FollowUpTemplate>()
            val fuArray = obj.optJSONArray("followUps")
            if (fuArray != null) {
                for (i in 0 until fuArray.length()) {
                    val item = fuArray.getJSONObject(i)
                    followUps.add(
                        FollowUpTemplate(
                            id = item.optString("id", "fu_$i"),
                            title = item.optString("title", "Follow Up"),
                            targetBuyerCondition = item.optString("targetBuyerCondition", ""),
                            messageText = item.optString("messageText", "")
                        )
                    )
                }
            }

            val carouselSlides = mutableListOf<CarouselSlideItem>()
            val slidesArray = obj.optJSONArray("carouselSlides")
            if (slidesArray != null && slidesArray.length() > 0) {
                for (i in 0 until slidesArray.length()) {
                    val item = slidesArray.getJSONObject(i)
                    carouselSlides.add(
                        CarouselSlideItem(
                            slideNumber = item.optInt("slideNumber", i + 1),
                            title = item.optString("title", "Slide ${i + 1}"),
                            badgeLabel = item.optString("badgeLabel", "✨ KEUNGGULAN #${i + 1}"),
                            headline = item.optString("headline", ""),
                            captionText = item.optString("captionText", ""),
                            recommendedVisual = item.optString("recommendedVisual", "Foto produk pendukung")
                        )
                    )
                }
            }

            // If carouselSlides is empty, synthesize a full 10-slide pro carousel
            if (carouselSlides.isEmpty()) {
                carouselSlides.addAll(generateFallbackCarousel(input, obj))
            }

            GeneratedPromo(
                productName = input.productName,
                category = input.category.displayName,
                photoUri = input.photoUri,
                photoUris = if (input.photoUris.isNotEmpty()) input.photoUris else if (input.photoUri != null) listOf(input.photoUri) else emptyList(),
                mainCaption = obj.optString("mainCaption", ""),
                hookOpening = obj.optString("hookOpening", ""),
                description = obj.optString("description", ""),
                advantagesAndBenefits = obj.optString("advantagesAndBenefits", ""),
                callToAction = obj.optString("callToAction", ""),
                hashtags = hashtags,
                categorizedHashtags = categorizedHashtags,
                alternativeTitles = alternativeTitles,
                viralHooks = viralHooks,
                ctaVariations = ctaVariations,
                adVariations = adVariations,
                weeklyPlan = weeklyPlan,
                carouselSlides = carouselSlides,
                storytelling = obj.optString("storytelling", ""),
                promoCopy = obj.optString("promoCopy", ""),
                quickReplies = quickReplies,
                followUps = followUps,
                selectedTone = input.tone.displayName,
                selectedPlatform = input.platform.displayName
            )
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Error parsing promo json", e)
            null
        }
    }

    private fun generateFallbackCarousel(input: ProductInput, obj: JSONObject): List<CarouselSlideItem> {
        val hook = obj.optString("hookOpening", "Spill Rahasia Produk Viral yang Bikin Ketagihan! ✨")
        val pName = input.productName.ifBlank { "Produk Unggulan" }
        val priceText = if (input.promoPrice.isNotBlank()) "Harga Spesial: ${input.promoPrice}" else "Pesan Sekarang"
        val shopInfo = if (input.shopContact.isNotBlank()) "WhatsApp: ${input.shopContact}" else "Hubungi Admin Kami"
        val usp = input.uspList.ifBlank { "Kualitas premium, higienis, dan terpercaya" }

        return listOf(
            CarouselSlideItem(
                slideNumber = 1,
                title = "Slide 1: Hook Utama & Tampilan Produk",
                badgeLabel = "✨ PRODUK UTAMA",
                headline = hook,
                captionText = "Kenalan dulu sama $pName! Pilihan terbaik buat kamu yang cari kualitas tanpa kompromi.",
                recommendedVisual = "Foto cover produk tampak depan dengan pencahayaan terang dan estetik"
            ),
            CarouselSlideItem(
                slideNumber = 2,
                title = "Slide 2: Keunggulan Bahan / Rasa / Kualitas",
                badgeLabel = "🧵 MATERIAL PREMIUM",
                headline = "Dibuat dari Material Pilihan Terbaik",
                captionText = "Rincian keunggulan: $usp. Terasa bedanya sejak sentuhan / suapan pertama!",
                recommendedVisual = "Foto close-up detail tekstur bahan / produk secara tajam dan jelas"
            ),
            CarouselSlideItem(
                slideNumber = 3,
                title = "Slide 3: Solusi Masalah Konsumen",
                badgeLabel = "💡 SOLUSI NYATA",
                headline = "Bikin Aktivitas Harian Jadi Jauh Lebih Mudah",
                captionText = "Gak perlu bingung lagi cari yang pas. $pName dirancang khusus menjawab kebutuhanmu.",
                recommendedVisual = "Foto produk saat digunakan dalam aktivitas / lifestyle sehari-hari"
            ),
            CarouselSlideItem(
                slideNumber = 4,
                title = "Slide 4: Pilihan Varian & Warna",
                badgeLabel = "🎨 PILIHAN LENGKAP",
                headline = "Tersedia Berbagai Pilihan Favorit",
                captionText = "Bebas pilih varian yang paling cocok sama kepribadian dan gayamu.",
                recommendedVisual = "Foto jajaran seluruh varian warna / rasa yang tersusun rapi"
            ),
            CarouselSlideItem(
                slideNumber = 5,
                title = "Slide 5: Jaminan Kualitas & Keamanan",
                badgeLabel = "🌿 100% AMAN & ASLI",
                headline = "Teruji Kualitasnya, Aman Dipakai",
                captionText = "Proses pembuatan higienis dengan kontrol mutu ketat demi kepuasan 100% pelanggan.",
                recommendedVisual = "Foto detail kemasan, segel keamanan, atau label sertifikasi"
            ),
            CarouselSlideItem(
                slideNumber = 6,
                title = "Slide 6: Ulasan & Testimoni Pembeli",
                badgeLabel = "⭐ TESTIMONI JUJUR",
                headline = "Sudah Terbukti Banyak yang Suka & Repeat Order",
                captionText = "\"Barangnya bagus banget, pengiriman super cepat!\" - Testimoni dari pelanggan setia kami.",
                recommendedVisual = "Foto tangkapan layar review positif bintang 5 dari pembeli"
            ),
            CarouselSlideItem(
                slideNumber = 7,
                title = "Slide 7: Panduan Praktis / Cara Pakai",
                badgeLabel = "🥣 MUDAH & PRAKTIS",
                headline = "Sangat Praktis & Siap Pakai Kapan Saja",
                captionText = "Cukup ikuti langkah mudahnya untuk mendapatkan hasil yang paling maksimal.",
                recommendedVisual = "Foto step by step cara penyajian / pemakaian produk"
            ),
            CarouselSlideItem(
                slideNumber = 8,
                title = "Slide 8: Promo Spesial Terbatas",
                badgeLabel = "🏷️ DISKON SPESIAL",
                headline = "Promo Khusus Hari Ini Saja!",
                captionText = "$priceText! Amankan kuota diskonmu sebelum harga kembali normal.",
                recommendedVisual = "Foto produk dengan grafis banner harga diskon spesial"
            ),
            CarouselSlideItem(
                slideNumber = 9,
                title = "Slide 9: Layanan Ekstra COD & Garansi",
                badgeLabel = "🚚 BISA COD & AMAN",
                headline = "Bisa Bayar di Tempat (COD) & Kirim Cepat",
                captionText = "Packing ekstra aman dengan bubble wrap tebal + garansi pengiriman aman sampai tujuan.",
                recommendedVisual = "Foto paket siap kirim rapi dengan stempel garansi aman"
            ),
            CarouselSlideItem(
                slideNumber = 10,
                title = "Slide 10: Call to Action (Pesan Sekarang)",
                badgeLabel = "📲 ORDER SEKARANG",
                headline = "Yuk Pesan Sekarang Sebelum Kehabisan!",
                captionText = "Langsung klik link di bio atau WhatsApp ke $shopInfo sekarang juga ya!",
                recommendedVisual = "Foto produk estetik dengan ajakan chat WA / order sekarang"
            )
        )
    }

    private fun parsePhotoAnalysisJson(rawText: String): PhotoAnalysisResult? {
        return try {
            val jsonStr = cleanJson(rawText)
            val obj = JSONObject(jsonStr)

            val hooks = mutableListOf<String>()
            val hooksArray = obj.optJSONArray("suggestedHooks")
            if (hooksArray != null) {
                for (i in 0 until hooksArray.length()) {
                    hooks.add(hooksArray.getString(i))
                }
            }

            PhotoAnalysisResult(
                productType = obj.optString("productType", "Produk UMKM"),
                detectedColors = obj.optString("detectedColors", "Dominan Cerah"),
                packagingType = obj.optString("packagingType", "Kemasan Rapi"),
                productVibe = obj.optString("productVibe", "Menarik & Berkualitas"),
                suggestedAudience = obj.optString("suggestedAudience", "Semua Kalangan"),
                suggestedHooks = hooks
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun generateChatResponses(
        customerMessage: String,
        productName: String,
        productPrice: String,
        shopName: String,
        shopContact: String,
        shopLocation: String,
        tone: String,
        customApiKey: String = ""
    ): List<CustomerReplyItem> {
        val apiKey = getApiKey(customApiKey)
        if (apiKey.isBlank()) {
            throw Exception("Google Gemini API Key belum diatur. Silakan masukkan API Key di menu Pengaturan.")
        }

        val prompt = GeminiPromptBuilder.buildChatAssistantPrompt(
            customerMessage = customerMessage,
            productName = productName,
            productPrice = productPrice,
            shopName = shopName,
            shopContact = shopContact,
            shopLocation = shopLocation,
            tone = tone
        )

        val request = GeminiContentRequest(
            contents = listOf(
                ContentItem(
                    parts = listOf(ContentPart(text = prompt))
                )
            )
        )

        val response = executeWithFallback(apiKey, request)
        if (!response.isSuccessful) {
            val errBody = response.errorBody()?.string() ?: response.message()
            throw Exception("Gagal menghasilkan balasan chat (Kode ${response.code()}): $errBody")
        }

        val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        if (rawText.isNullOrBlank()) {
            throw Exception("Respon balasan chat dari Gemini AI kosong.")
        }

        return parseChatRepliesJson(rawText, customerMessage, productName)
    }

    private fun parseChatRepliesJson(
        rawText: String,
        customerMessage: String,
        productName: String
    ): List<CustomerReplyItem> {
        return try {
            val jsonStr = cleanJson(rawText)
            val replies = mutableListOf<CustomerReplyItem>()
            if (jsonStr.startsWith("[")) {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    replies.add(
                        CustomerReplyItem(
                            id = obj.optString("id", "reply_$i"),
                            questionCategory = obj.optString("questionCategory", "Balasan CS"),
                            questionSample = obj.optString("questionSample", customerMessage),
                            suggestedReply = obj.optString("suggestedReply", "")
                        )
                    )
                }
            } else {
                val obj = JSONObject(jsonStr)
                val array = obj.optJSONArray("replies") ?: obj.optJSONArray("quickReplies")
                if (array != null) {
                    for (i in 0 until array.length()) {
                        val itemObj = array.getJSONObject(i)
                        replies.add(
                            CustomerReplyItem(
                                id = itemObj.optString("id", "reply_$i"),
                                questionCategory = itemObj.optString("questionCategory", "Balasan CS"),
                                questionSample = itemObj.optString("questionSample", customerMessage),
                                suggestedReply = itemObj.optString("suggestedReply", "")
                            )
                        )
                    }
                }
            }

            if (replies.isEmpty()) {
                getDefaultChatReplies(customerMessage, productName)
            } else {
                replies
            }
        } catch (e: Exception) {
            getDefaultChatReplies(customerMessage, productName)
        }
    }

    private fun getDefaultChatReplies(customerMessage: String, productName: String): List<CustomerReplyItem> {
        val prod = if (productName.isNotBlank()) productName else "produk kami"
        return listOf(
            CustomerReplyItem(
                id = "def_1",
                questionCategory = "🌸 Ramah & Hangat",
                questionSample = customerMessage.ifBlank { "Tanya Produk" },
                suggestedReply = "Halo kak! Terima kasih sudah menghubungi kami 😊 Untuk $prod ready stock dan siap dikirim hari ini ya kak. Boleh dibantu mau dikirim ke daerah mana kak?"
            ),
            CustomerReplyItem(
                id = "def_2",
                questionCategory = "⚡ Fast Closing & Promo",
                questionSample = customerMessage.ifBlank { "Tanya Promo" },
                suggestedReply = "Hai kak! Kabar baiknya hari ini lagi ada promo potongan harga khusus untuk $prod ✨ Kuota promo terbatas untuk 5 orang pertama hari ini. Mau kami amankan slotnya sekarang kak?"
            ),
            CustomerReplyItem(
                id = "def_3",
                questionCategory = "📦 Format Order Langsung",
                questionSample = customerMessage.ifBlank { "Format Pemesanan" },
                suggestedReply = "Siap kak! Agar bisa langsung diproses pengirimannya, kakak bisa isi format order ini ya:\n\n• Nama Penerima:\n• No HP:\n• Alamat Lengkap:\n• Jumlah Pesanan:\n\nSetelah itu kami buatkan totalan dan resinya kak! 🙏"
            )
        )
    }
}

