package com.example.data.api

import android.util.Log
import com.example.ai.GeminiPromptBuilder
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

    suspend fun testConnection(customApiKey: String = ""): Result<String> {
        val apiKey = getApiKey(customApiKey)
        if (apiKey.isBlank()) {
            return Result.failure(Exception("API Key belum diisi. Masukkan Google Gemini API Key Anda."))
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
            val response = service.generateContent(
                model = "gemini-2.5-flash",
                apiKey = apiKey,
                request = request
            )
            if (response.isSuccessful) {
                val reply = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "Koneksi berhasil terhubung!"
                Result.success(reply.trim())
            } else {
                Result.failure(Exception("Server menolak request (Error code: ${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generatePromotion(input: ProductInput, customApiKey: String = ""): GeneratedPromo {
        val apiKey = getApiKey(customApiKey)

        val prompt = GeminiPromptBuilder.buildPromotionPrompt(input)

        if (apiKey.isNotBlank()) {
            try {
                val parts = mutableListOf<ContentPart>()
                if (!input.photoBase64.isNullOrBlank()) {
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

                val response = service.generateContent(
                    model = "gemini-2.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!rawText.isNullOrBlank()) {
                        val parsed = parsePromotionJson(rawText, input)
                        if (parsed != null) return parsed
                    }
                } else {
                    Log.w("GeminiApiClient", "API returned error code: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("GeminiApiClient", "Failed to call Gemini API", e)
            }
        }

        // High quality deterministic fallback generator if offline / error
        return generateFallbackPromotion(input)
    }

    suspend fun analyzePhoto(photoBase64: String, productName: String, customApiKey: String = ""): PhotoAnalysisResult {
        val apiKey = getApiKey(customApiKey)

        val prompt = GeminiPromptBuilder.buildPhotoAnalysisPrompt(productName)

        if (apiKey.isNotBlank() && photoBase64.isNotBlank()) {
            try {
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

                val response = service.generateContent(
                    model = "gemini-2.5-flash",
                    apiKey = apiKey,
                    request = request
                )

                if (response.isSuccessful) {
                    val rawText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    if (!rawText.isNullOrBlank()) {
                        val parsed = parsePhotoAnalysisJson(rawText)
                        if (parsed != null) return parsed
                    }
                }
            } catch (e: Exception) {
                Log.e("GeminiApiClient", "Failed photo analysis", e)
            }
        }

        return PhotoAnalysisResult(
            productType = if (productName.isNotBlank()) productName else "Produk Unggulan UMKM",
            detectedColors = "Cerah & Kontras Menarik",
            packagingType = "Kemasan Rapi & Siap Jual",
            productVibe = "Segar, Menarik & Premium",
            suggestedAudience = "Pelanggan Online & Pengguna Media Sosial",
            suggestedHooks = listOf(
                "Pecinta $productName wajib coba yang satu ini!",
                "Sekali coba dijamin langsung repeat order!",
                "Kualitas terbaik dengan harga terjangkau khusus hari ini!"
            )
        )
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

            GeneratedPromo(
                productName = input.productName,
                photoUri = input.photoUri,
                mainCaption = obj.optString("mainCaption", ""),
                hookOpening = obj.optString("hookOpening", ""),
                description = obj.optString("description", ""),
                advantagesAndBenefits = obj.optString("advantagesAndBenefits", ""),
                callToAction = obj.optString("callToAction", ""),
                hashtags = hashtags,
                alternativeTitles = alternativeTitles,
                viralHooks = viralHooks,
                ctaVariations = ctaVariations,
                adVariations = adVariations,
                weeklyPlan = weeklyPlan,
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

    private fun generateFallbackPromotion(input: ProductInput): GeneratedPromo {
        val priceInfo = if (input.promoPrice.isNotBlank()) {
            "🔥 PROMO HARI INI: Hanya ${input.promoPrice} (Harga Normal: ${input.normalPrice})"
        } else if (input.normalPrice.isNotBlank()) {
            "💰 Harga Terbaik: ${input.normalPrice}"
        } else {
            "💰 Hubungi Admin untuk penawaran spesial!"
        }

        val contactInfo = if (input.shopContact.isNotBlank()) "\n📲 Order via WhatsApp: ${input.shopContact}" else ""
        val shopInfo = if (input.shopName.isNotBlank()) "\n🏬 ${input.shopName}" else ""

        val mainCaption = """
🌟 ${input.productName} - Pilihan Terbaik untuk Anda! 🌟

Lagi cari ${input.productName} yang berkualitas, terpercaya, dan bikin puas? Ini dia jawabannya!

✨ Kenapa Wajib Pilih Produk Kami?
${if (input.uspList.isNotBlank()) "✅ " + input.uspList.replace(",", "\n✅ ") else "✅ Kualitas Terjamin & Bahan Premium\n✅ Pelayanan Cepat & Terpercaya\n✅ Garansi Kepuasan Pelanggan"}

$priceInfo$shopInfo$contactInfo

👉 Jangan sampai kehabisan, stok sangat terbatas! Klik pesan sekarang sebelum promo berakhir! 🚀
""".trimIndent()

        return GeneratedPromo(
            productName = input.productName,
            photoUri = input.photoUri,
            mainCaption = mainCaption,
            hookOpening = "🔥 Cari ${input.productName} terbaik dengan harga terjangkau? Ini rahasianya!",
            description = "${input.productName} dirancang dengan bahan berkualitas pilihan untuk memberikan manfaat optimal bagi setiap pelanggan.",
            advantagesAndBenefits = if (input.uspList.isNotBlank()) input.uspList else "Kualitas premium, higienis, pengiriman cepat dan aman.",
            callToAction = "📲 Hubungi kami sekarang dan klaim promo spesial hari ini!",
            hashtags = listOf(
                "#${input.productName.replace(" ", "")}",
                "#Jual${input.productName.replace(" ", "")}",
                "#UMKMIndonesia",
                "#ProdukLokal",
                "#PromoSpesial",
                "#OlshopMurah",
                "#RekomendasiProduk",
                "#BelanjaOnline"
            ),
            alternativeTitles = listOf(
                "Rahasia ${input.productName} Laris Manis yang Bikin Pelanggan Ketagihan!",
                "Spesial Hari Ini: Dapatkan ${input.productName} Kualitas Premium dengan Harga Promo!",
                "Wajib Coba! Inilah Alasan Mengapa ${input.productName} Jadi Favorit Banyak Orang"
            ),
            viralHooks = listOf(
                "Jangan beli ${input.productName} sebelum kamu tahu fakta penting ini! 😱",
                "Satu hal yang bikin semua orang beralih ke ${input.productName}... 👀",
                "Nyesel banget baru tahu ada ${input.productName} seenak/sebagus ini! ✨"
            ),
            ctaVariations = listOf(
                "Klik tombol pesan sekarang sebelum promo berakhir malam ini! ⏳",
                "Chat WhatsApp kami sekarang untuk klaim gratis ongkir! 📦",
                "Stok tersisa 5 pcs lagi, amankan pesananmu sekarang juga! 🏃‍♂️"
            ),
            adVariations = mapOf(
                "whatsapp" to "Halo kak! Kabar gembira buat kamu yang lagi cari *${input.productName}* 🎉\n\n$priceInfo\n\nYuk order sekarang mumpung slot promo masih ada! $contactInfo",
                "wa_status" to "🔥 READY STOCK ${input.productName}!\n$priceInfo\nYang mau keep langsung reply ya kak, terbatas! 📲",
                "instagram" to "Tampil beda dan nikmati kualitas terbaik bersama ${input.productName}! ✨\n\nOrder sekarang via Link di Bio / DM kami langsung ya! 🛍️",
                "facebook" to "Buat bapak/ibu yang sedang mencari ${input.productName}, kami menyediakan stok terbaik dengan garansi kualitas! Silakan kirim pesan untuk info lebih lengkap.",
                "tiktok_script" to "[Visual: Close up produk ${input.productName}]\n[Voiceover: 'Kalian masih bingung cari ${input.productName} yang beneran bagus? Nih kenalin solusinya! Kualitas nomor satu, harga bersahabat. Klik keranjang kuning sekarang!']",
                "marketplace" to "${input.productName} Original & Kualitas Terjamin.\n\nSpesifikasi & Keunggulan:\n- ${input.uspList.ifBlank { "Bahan berkualitas tinggi" }}\n- Pengiriman cepat dan packing aman bubble wrap tebal."
            ),
            weeklyPlan = listOf(
                WeeklyDayPlan(1, "Senin", "Pengenalan Masalah & Solusi", "Tunjukkan kendala umum konsumen", "Pernah gak sih ngerasa butuh ${input.productName} yang beneran awet? Ini solusinya!"),
                WeeklyDayPlan(2, "Selasa", "Kualitas & Bahan", "Detail bahan/keunggulan", "Di balik ${input.productName}, ada proses teliti dengan bahan pilihan."),
                WeeklyDayPlan(3, "Rabu", "Testimoni Pelanggan", "Social proof", "Terima kasih untuk repeat ordernya! ${input.productName} memang selalu jadi favorit."),
                WeeklyDayPlan(4, "Kamis", "Tips & Trik Penggunaan", "Edukasi pelanggan", "Cara memaksimalkan manfaat ${input.productName} agar tahan lama."),
                WeeklyDayPlan(5, "Jumat", "Jumat Berkah / Promo Weekend", "Penawaran akhir pekan", "Jumat Berkah! Dapatkan diskon spesial untuk setiap pembelian ${input.productName}."),
                WeeklyDayPlan(6, "Sabtu", "Interaksi & Kuis", "Tingkatkan engagement", "Dari skala 1-10, seberapa butuh kamu sama ${input.productName}? Tulis di kolom komentar!"),
                WeeklyDayPlan(7, "Minggu", "Last Call Reminder", "Urgensi penutupan promo", "Pengingat terakhir! Promo mingguan ${input.productName} berakhir malam ini.")
            ),
            storytelling = "Perjalanan menghadirkan ${input.productName} berawal dari komitmen kami untuk memberikan kualitas terbaik bagi setiap pelanggan setia. Setiap proses kami jaga dengan penuh cinta dan standar kebersihan tertinggi.",
            promoCopy = "🚨 PROMO SPESIAL HARI INI SAJA! 🚨\nDapatkan ${input.productName} dengan penawaran istimewa. Kuota terbatas hanya untuk 10 pemesan pertama hari ini!",
            quickReplies = listOf(
                CustomerReplyItem("qr_1", "Tanya Harga", "Berapa harganya kak?", "Halo kak! Untuk ${input.productName} saat ini $priceInfo ya kak. Mau dikirim ke alamat mana kak? 😊"),
                CustomerReplyItem("qr_2", "Tanya Stok", "Apakah ready kak?", "Ready stock siap kirim kak! Silakan kirimkan alamat lengkap untuk langsung kami proses ya ✨"),
                CustomerReplyItem("qr_3", "Tanya Pengiriman", "Bisa COD atau gratis ongkir?", "Bisa banget kak! Kami melayani pengiriman ke seluruh wilayah dengan promo subsidi ongkir. Mau kami bantu hitung estimasinya?")
            ),
            followUps = listOf(
                FollowUpTemplate("fu_1", "Follow-up Belum Bayar", "Pelanggan sudah minta totalan", "Halo kak 😊 Mau konfirmasi untuk pesanan ${input.productName} mau dikirim dengan ekspedisi apa ya kak? Biar langsung kami jadwalkan pengirimannya sore ini!"),
                FollowUpTemplate("fu_2", "Follow-up Promo Hampir Habis", "Pelanggan belum konfirmasi", "Selamat siang kak! Mengingatkan slot promo spesial ${input.productName} tersisa sedikit lagi nih kak. Sayang banget kalau kelewatan, mau kami amankan sekarang?")
            ),
            selectedTone = input.tone.displayName,
            selectedPlatform = input.platform.displayName
        )
    }
}
