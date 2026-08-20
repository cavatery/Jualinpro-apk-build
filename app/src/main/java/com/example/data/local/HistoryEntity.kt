package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.CustomerReplyItem
import com.example.model.FollowUpTemplate
import com.example.model.GeneratedPromo
import com.example.model.WeeklyDayPlan

@Entity(tableName = "promo_history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val productName: String,
    val category: String = "",
    val photoUri: String? = null,
    val photoUrisJson: String = "",
    val mainCaption: String,
    val hookOpening: String,
    val description: String,
    val advantagesAndBenefits: String,
    val callToAction: String,
    val hashtagsJson: String = "",
    val categorizedHashtagsJson: String = "",
    val alternativeTitlesJson: String = "",
    val viralHooksJson: String = "",
    val ctaVariationsJson: String = "",
    val adVariationsJson: String = "",
    val weeklyPlanJson: String = "",
    val carouselSlidesJson: String = "",
    val storytelling: String = "",
    val promoCopy: String = "",
    val quickRepliesJson: String = "",
    val followUpsJson: String = "",
    val selectedTone: String = "",
    val selectedPlatform: String = "",
    val isFavorite: Boolean = false
)
