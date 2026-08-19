package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.GeminiPromptBuilder
import com.example.model.CopywritingTone
import com.example.model.ProductInput
import com.example.model.PromoType
import com.example.model.PromotionPlatform
import com.example.model.TargetAudience
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Jualin AI Pro", appName)
    }

    @Test
    fun `build promotion prompt contains product details`() {
        val input = ProductInput(
            productName = "Keripik Pisang Renyah",
            normalPrice = "Rp20.000",
            promoPrice = "Rp15.000",
            promoType = PromoType.DISCOUNT,
            promoDetail = "Diskon 25%",
            uspList = "Pisang pilihan, renyah gurih",
            targetAudience = TargetAudience.YOUTH,
            tone = CopywritingTone.VIRAL,
            platform = PromotionPlatform.TIKTOK
        )
        val prompt = GeminiPromptBuilder.buildPromotionPrompt(input)
        assertTrue(prompt.contains("Keripik Pisang Renyah"))
        assertTrue(prompt.contains("Rp15.000"))
        assertTrue(prompt.contains("Pisang pilihan"))
        assertTrue(prompt.contains("viralHooks"))
        assertTrue(prompt.contains("weeklyPlan"))
    }
}
