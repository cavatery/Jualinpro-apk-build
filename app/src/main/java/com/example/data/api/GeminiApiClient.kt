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
        if (apiKey.isBlank()) {
            throw Exception("Google Gemini API Key belum diatur. Silakan masukkan API Key Gemini Anda di menu Pengaturan.")
        }

        val prompt = GeminiPromptBuilder.buildPromotionPrompt(input)
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

        val response = service.generateContent(
            model = "gemini-2.5-flash",
            apiKey = apiKey,
            request = request
        )

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
}
