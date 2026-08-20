package com.example.model

enum class CopywritingTone(val displayName: String, val description: String) {
    DIRECT("Singkat & Langsung Jual", "To the point, jelas, harga & CTA utama"),
    PERSUASIVE("Menarik & Persuasif", "Menggugah minat dan meyakinkan konsumen"),
    CASUAL("Santai & Akrab", "Bahasa mengalir santai seperti teman"),
    FUNNY("Lucu & Menghibur", "Menggunakan humor ringan yang relatable"),
    PROFESSIONAL("Profesional & Kredibel", "Gaya bahasa terpercaya dan elegan"),
    ELEGANT("Elegan & Eksklusif", "Memberi kesan premium dan berkelas"),
    VIRAL("Gaya Viral Medsos", "Gaya kekinian dengan hook menarik"),
    YOUTH("Anak Muda / Gen Z", "Bahasa gaul kekinian, seru & dinamis"),
    IBU_IBU("Ramah Ibu-Ibu / Keluarga", "Hangat, solutif untuk kebutuhan rumah tangga"),
    LOCAL("Lokal & Kultural", "Sentuhan kearifan lokal nusantara"),
    STORYTELLING("Storytelling Inspiratif", "Cerita emosional perjalanan produk")
}

enum class PromotionPlatform(val displayName: String) {
    WHATSAPP("WhatsApp & WA Grup"),
    WA_STORY("WhatsApp Status"),
    INSTAGRAM("Instagram Feed & Story"),
    FACEBOOK("Facebook & Grup Jual Beli"),
    TIKTOK("TikTok Video / Naskah"),
    SHOPEE("Shopee / Tokopedia"),
    GENERAL("Semua Media Sosial")
}

enum class TargetAudience(val displayName: String) {
    ALL("Semua Kalangan"),
    YOUTH("Anak Muda & Remaja"),
    HOUSEWIVES("Ibu Rumah Tangga"),
    STUDENTS("Pelajar & Mahasiswa"),
    WORKERS("Pekerja & Karyawan"),
    FAMILIES("Keluarga"),
    MEN("Pria"),
    WOMEN("Wanita"),
    CUSTOM("Kustom...")
}

enum class ProductCategory(val displayName: String, val iconEmoji: String) {
    FASHION("Fashion & Busana", "👗"),
    FOOD_BEVERAGE("Kuliner & Makanan", "🍲"),
    BEAUTY_SKINCARE("Kecantikan & Skincare", "💄"),
    ELECTRONICS_GADGET("Elektronik & Gadget", "📱"),
    HOME_LIVING("Perabot & Rumah Tangga", "🏠"),
    HEALTH_HERBAL("Kesehatan & Herbal", "🌿"),
    HANDICRAFT_UMKM("Kerajinan & Produk Unik", "🎨"),
    MOM_BABY("Ibu & Perlengkapan Bayi", "👶"),
    AUTOMOTIVE("Otomotif & Aksesoris", "🛵"),
    SERVICES("Jasa & Layanan", "💼"),
    GENERAL("Umum / Produk Lainnya", "📦")
}

enum class PromoType(val displayName: String) {
    NONE("Harga Normal"),
    DISCOUNT("Diskon Khusus (%)"),
    BUY_GET("Beli X Gratis Y"),
    FREE_SHIPPING("Gratis Ongkir"),
    FLASH_SALE("Flash Sale Terbatas"),
    BONUS("Bonus Hadiah")
}

data class CategorizedHashtags(
    val viralTrending: List<String> = emptyList(),
    val nicheCategory: List<String> = emptyList(),
    val localUmkm: List<String> = emptyList(),
    val promoDiscount: List<String> = emptyList()
)

data class CarouselSlideItem(
    val slideNumber: Int,
    val title: String,
    val badgeLabel: String,
    val headline: String,
    val captionText: String,
    val recommendedVisual: String
)

data class ProductInput(
    val productName: String = "",
    val category: ProductCategory = ProductCategory.GENERAL,
    val normalPrice: String = "",
    val promoPrice: String = "",
    val promoType: PromoType = PromoType.NONE,
    val promoDetail: String = "",
    val uspList: String = "",
    val targetAudience: TargetAudience = TargetAudience.ALL,
    val customTarget: String = "",
    val tone: CopywritingTone = CopywritingTone.PERSUASIVE,
    val platform: PromotionPlatform = PromotionPlatform.GENERAL,
    val photoUri: String? = null,
    val photoBase64: String? = null,
    val photoUris: List<String> = emptyList(),
    val photoBase64List: List<String> = emptyList(),
    val shopName: String = "",
    val shopContact: String = "",
    val shopLocation: String = ""
)

data class WeeklyDayPlan(
    val dayNumber: Int,
    val dayName: String,
    val theme: String,
    val concept: String,
    val readyCaption: String
)

data class CustomerReplyItem(
    val id: String,
    val questionCategory: String,
    val questionSample: String,
    val suggestedReply: String
)

data class FollowUpTemplate(
    val id: String,
    val title: String,
    val targetBuyerCondition: String,
    val messageText: String
)

data class PhotoAnalysisResult(
    val productType: String,
    val detectedColors: String,
    val packagingType: String,
    val productVibe: String,
    val suggestedAudience: String,
    val suggestedHooks: List<String>
)

data class GeneratedPromo(
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val productName: String,
    val category: String = "",
    val photoUri: String? = null,
    val photoUris: List<String> = emptyList(),
    val mainCaption: String,
    val hookOpening: String,
    val description: String,
    val advantagesAndBenefits: String,
    val callToAction: String,
    val hashtags: List<String>,
    val categorizedHashtags: CategorizedHashtags = CategorizedHashtags(),
    val alternativeTitles: List<String>,
    val viralHooks: List<String>,
    val ctaVariations: List<String>,
    val adVariations: Map<String, String>,
    val weeklyPlan: List<WeeklyDayPlan>,
    val carouselSlides: List<CarouselSlideItem> = emptyList(),
    val storytelling: String,
    val promoCopy: String,
    val quickReplies: List<CustomerReplyItem>,
    val followUps: List<FollowUpTemplate>,
    val selectedTone: String,
    val selectedPlatform: String,
    val isFavorite: Boolean = false
)
