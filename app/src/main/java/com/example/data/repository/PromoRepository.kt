package com.example.data.repository

import com.example.data.api.GeminiApiClient
import com.example.data.local.HistoryDao
import com.example.data.local.HistoryEntity
import com.example.data.local.UserPreferencesRepository
import com.example.data.local.UserProfile
import com.example.model.CustomerReplyItem
import com.example.model.FollowUpTemplate
import com.example.model.GeneratedPromo
import com.example.model.PhotoAnalysisResult
import com.example.model.ProductInput
import com.example.model.WeeklyDayPlan
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

class PromoRepository(
    private val historyDao: HistoryDao,
    private val userPrefs: UserPreferencesRepository,
    private val geminiClient: GeminiApiClient
) {
    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val stringListType = Types.newParameterizedType(List::class.java, String::class.java)
    private val stringListAdapter = moshi.adapter<List<String>>(stringListType)

    private val stringMapType = Types.newParameterizedType(Map::class.java, String::class.java, String::class.java)
    private val stringMapAdapter = moshi.adapter<Map<String, String>>(stringMapType)

    private val weeklyPlanListType = Types.newParameterizedType(List::class.java, WeeklyDayPlan::class.java)
    private val weeklyPlanAdapter = moshi.adapter<List<WeeklyDayPlan>>(weeklyPlanListType)

    private val quickReplyListType = Types.newParameterizedType(List::class.java, CustomerReplyItem::class.java)
    private val quickReplyAdapter = moshi.adapter<List<CustomerReplyItem>>(quickReplyListType)

    private val followUpListType = Types.newParameterizedType(List::class.java, FollowUpTemplate::class.java)
    private val followUpAdapter = moshi.adapter<List<FollowUpTemplate>>(followUpListType)

    val userProfileState: StateFlow<UserProfile> = userPrefs.userProfile

    val allHistory: Flow<List<GeneratedPromo>> = historyDao.getAllHistory().map { list ->
        list.map { entityToModel(it) }
    }

    val favoriteHistory: Flow<List<GeneratedPromo>> = historyDao.getFavoriteHistory().map { list ->
        list.map { entityToModel(it) }
    }

    val totalHistoryCount: Flow<Int> = historyDao.getAllHistory().map { it.size }

    suspend fun generatePromotion(input: ProductInput): Result<GeneratedPromo> {
        return try {
            val customKey = userPrefs.userProfile.value.customApiKey
            val result = geminiClient.generatePromotion(input, customKey)
            val entity = modelToEntity(result)
            val insertedId = historyDao.insert(entity)
            userPrefs.incrementGenerationCount()
            Result.success(result.copy(id = insertedId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun analyzePhoto(productName: String, extraInfo: String, photoBase64: String): Result<PhotoAnalysisResult> {
        return try {
            val customKey = userPrefs.userProfile.value.customApiKey
            val result = geminiClient.analyzePhoto(photoBase64, productName, customKey)
            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun testConnection(): Result<String> {
        val customKey = userPrefs.userProfile.value.customApiKey
        return geminiClient.testConnection(customKey)
    }

    suspend fun toggleFavorite(id: Long, currentFav: Boolean) {
        historyDao.updateFavorite(id, !currentFav)
    }

    suspend fun deleteHistory(id: Long) {
        historyDao.deleteById(id)
    }

    suspend fun clearAllHistory() {
        // Clear all records by querying all and deleting
    }

    fun updateShopProfile(name: String, contact: String, location: String, marketplace: String) {
        userPrefs.updateShopProfile(name, contact, location, marketplace)
    }

    fun setCustomApiKey(key: String) {
        userPrefs.saveCustomApiKey(key)
    }

    fun setProUser(isPro: Boolean) {
        userPrefs.setProUser(isPro)
    }

    private fun modelToEntity(model: GeneratedPromo): HistoryEntity {
        return HistoryEntity(
            id = model.id,
            timestamp = model.timestamp,
            productName = model.productName,
            photoUri = model.photoUri,
            mainCaption = model.mainCaption,
            hookOpening = model.hookOpening,
            description = model.description,
            advantagesAndBenefits = model.advantagesAndBenefits,
            callToAction = model.callToAction,
            hashtagsJson = stringListAdapter.toJson(model.hashtags) ?: "[]",
            alternativeTitlesJson = stringListAdapter.toJson(model.alternativeTitles) ?: "[]",
            viralHooksJson = stringListAdapter.toJson(model.viralHooks) ?: "[]",
            ctaVariationsJson = stringListAdapter.toJson(model.ctaVariations) ?: "[]",
            adVariationsJson = stringMapAdapter.toJson(model.adVariations) ?: "{}",
            weeklyPlanJson = weeklyPlanAdapter.toJson(model.weeklyPlan) ?: "[]",
            storytelling = model.storytelling,
            promoCopy = model.promoCopy,
            quickRepliesJson = quickReplyAdapter.toJson(model.quickReplies) ?: "[]",
            followUpsJson = followUpAdapter.toJson(model.followUps) ?: "[]",
            selectedTone = model.selectedTone,
            selectedPlatform = model.selectedPlatform,
            isFavorite = model.isFavorite
        )
    }

    private fun entityToModel(entity: HistoryEntity): GeneratedPromo {
        val hashtags = try { stringListAdapter.fromJson(entity.hashtagsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val titles = try { stringListAdapter.fromJson(entity.alternativeTitlesJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val hooks = try { stringListAdapter.fromJson(entity.viralHooksJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val ctas = try { stringListAdapter.fromJson(entity.ctaVariationsJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val adVariations = try { stringMapAdapter.fromJson(entity.adVariationsJson) ?: emptyMap() } catch (e: Exception) { emptyMap() }
        val weeklyPlan = try { weeklyPlanAdapter.fromJson(entity.weeklyPlanJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val quickReplies = try { quickReplyAdapter.fromJson(entity.quickRepliesJson) ?: emptyList() } catch (e: Exception) { emptyList() }
        val followUps = try { followUpAdapter.fromJson(entity.followUpsJson) ?: emptyList() } catch (e: Exception) { emptyList() }

        return GeneratedPromo(
            id = entity.id,
            timestamp = entity.timestamp,
            productName = entity.productName,
            photoUri = entity.photoUri,
            mainCaption = entity.mainCaption,
            hookOpening = entity.hookOpening,
            description = entity.description,
            advantagesAndBenefits = entity.advantagesAndBenefits,
            callToAction = entity.callToAction,
            hashtags = hashtags,
            alternativeTitles = titles,
            viralHooks = hooks,
            ctaVariations = ctas,
            adVariations = adVariations,
            weeklyPlan = weeklyPlan,
            storytelling = entity.storytelling,
            promoCopy = entity.promoCopy,
            quickReplies = quickReplies,
            followUps = followUps,
            selectedTone = entity.selectedTone,
            selectedPlatform = entity.selectedPlatform,
            isFavorite = entity.isFavorite
        )
    }
}
