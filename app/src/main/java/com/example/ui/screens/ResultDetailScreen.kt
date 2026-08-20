package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.CarouselSlideItem
import com.example.model.GeneratedPromo
import com.example.ui.components.AppHeader
import com.example.ui.components.CopyButton
import com.example.ui.components.ResultCardContainer
import com.example.ui.components.ShareButton
import com.example.ui.components.WhatsAppShareButton
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.shareMultiplePromoImages
import com.example.ui.components.sharePromoContent
import com.example.ui.components.shareText
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryEmerald
import com.example.ui.theme.TertiaryAmber
import com.example.ui.viewmodel.MainViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultDetailScreen(
    viewModel: MainViewModel,
    promo: GeneratedPromo,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var currentPromo by remember(promo) { mutableStateOf(promo) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editTextValue by remember { mutableStateOf("") }
    var editTargetField by remember { mutableStateOf("caption") }

    val tabTitles = listOf(
        "📝 Caption",
        "🎞️ Slide & Korsel Pro (10 Foto)",
        "#️⃣ Tagar Trending",
        "🎯 Judul & Hook",
        "📢 Iklan Medsos",
        "💡 Jadwal 7 Hari",
        "💬 Balas Chat & CS",
        "🏷️ Promo & Cerita"
    )

    Scaffold(
        topBar = {
            AppHeader(
                title = currentPromo.productName,
                subtitle = "${currentPromo.selectedPlatform} • ${currentPromo.selectedTone}",
                onBackClick = onBack,
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.toggleFavorite(currentPromo)
                            currentPromo = currentPromo.copy(isFavorite = !currentPromo.isFavorite)
                        },
                        modifier = Modifier.testTag("btn_toggle_favorite")
                    ) {
                        Icon(
                            imageVector = if (currentPromo.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorit",
                            tint = if (currentPromo.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = {
                            sharePromoContent(
                                context = context,
                                text = currentPromo.mainCaption,
                                photoUriString = currentPromo.photoUri,
                                title = "Materi Promosi ${currentPromo.productName}"
                            )
                        },
                        modifier = Modifier.testTag("btn_share_top")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Bagikan Semua",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Scrollable Tabs
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = PrimaryIndigo
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            )
                        }
                    )
                }
            }

            // Tab Content
            when (selectedTab) {
                0 -> CaptionTab(
                    promo = currentPromo,
                    onEdit = {
                        editTextValue = currentPromo.mainCaption
                        editTargetField = "caption"
                        showEditDialog = true
                    }
                )
                1 -> CarouselProTab(promo = currentPromo)
                2 -> TrendingHashtagsTab(promo = currentPromo)
                3 -> HooksAndTitlesTab(promo = currentPromo)
                4 -> AdsTab(promo = currentPromo)
                5 -> WeeklyPlanTab(promo = currentPromo)
                6 -> ChatAndFollowUpTab(promo = currentPromo)
                7 -> PromoAndStorytellingTab(promo = currentPromo)
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Teks Promosi", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = editTextValue,
                    onValueChange = { editTextValue = it },
                    minLines = 8,
                    maxLines = 14,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editTargetField == "caption") {
                            currentPromo = currentPromo.copy(mainCaption = editTextValue)
                            viewModel.updateResultDetail(currentPromo)
                        }
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald)
                ) {
                    Text("Simpan Perubahan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CaptionTab(
    promo: GeneratedPromo,
    onEdit: () -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (promo.photoUri != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(14.dp))
                        ) {
                            AsyncImage(
                                model = promo.photoUri,
                                contentDescription = "Foto Produk",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fast Share Buttons with Photo Included
                        WhatsAppShareButton(
                            textToShare = promo.mainCaption,
                            photoUri = promo.photoUri,
                            label = "Kirim ke Status WA (Foto + Teks)",
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ShareButton(
                            textToShare = promo.mainCaption,
                            photoUri = promo.photoUri,
                            title = "Bagikan Materi ${promo.productName}",
                            label = "Bagikan ke Aplikasi Lain (Foto + Teks)",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
            ResultCardContainer(
                title = "Caption Jualan Siap Posting",
                subtitle = "Format lengkap dengan hook, deskripsi, CTA & hashtag",
                textToCopy = promo.mainCaption,
                photoUri = promo.photoUri,
                shareTitle = "Caption ${promo.productName}"
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onEdit,
                            modifier = Modifier.testTag("btn_edit_caption")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Teks")
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = promo.mainCaption,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        if (promo.hashtags.isNotEmpty()) {
            item {
                ResultCardContainer(
                    title = "Hashtag Relevan & Trending (#)",
                    subtitle = "Gunakan untuk menaikkan jangkauan postingan di medsos",
                    textToCopy = promo.hashtags.joinToString(" "),
                    photoUri = promo.photoUri,
                    shareTitle = "Hashtags ${promo.productName}"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        promo.hashtags.forEach { tag ->
                            SuggestionChip(
                                onClick = { copyTextToClipboard(context, tag, "Tagar disalin") },
                                label = { Text(tag, fontSize = 13.sp, color = PrimaryIndigo, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TrendingHashtagsTab(promo: GeneratedPromo) {
    val context = LocalContext.current
    val allTags = if (promo.hashtags.isNotEmpty()) {
        promo.hashtags
    } else {
        val list = mutableListOf<String>()
        list.addAll(promo.categorizedHashtags.viralTrending)
        list.addAll(promo.categorizedHashtags.nicheCategory)
        list.addAll(promo.categorizedHashtags.localUmkm)
        list.addAll(promo.categorizedHashtags.promoDiscount)
        list
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimaryIndigo.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Rekomendasi Tagar Trending & Relevan",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = PrimaryIndigo
                        )
                        Text(
                            text = "Disesuaikan dengan deskripsi produk dan kategori untuk memaksimalkan FYP / Explore",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // All Combined Copy Card
        if (allTags.isNotEmpty()) {
            item {
                ResultCardContainer(
                    title = "🚀 Semua Tagar Rekomendasi (Siap Copy)",
                    subtitle = "${allTags.size} tagar optimal untuk caption Instagram, TikTok & Facebook",
                    textToCopy = allTags.joinToString(" "),
                    photoUri = promo.photoUri,
                    shareTitle = "Tagar ${promo.productName}"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allTags.forEach { tag ->
                            SuggestionChip(
                                onClick = { copyTextToClipboard(context, tag, "Tagar disalin") },
                                label = { Text(tag, fontSize = 13.sp, color = PrimaryIndigo, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }
        }

        // 1. Viral & Trending FYP Tags
        if (promo.categorizedHashtags.viralTrending.isNotEmpty()) {
            item {
                ResultCardContainer(
                    title = "🔥 Tagar Viral & Algoritma FYP",
                    subtitle = "Memicu algoritma video & explore medsos",
                    textToCopy = promo.categorizedHashtags.viralTrending.joinToString(" "),
                    photoUri = promo.photoUri,
                    shareTitle = "Viral Tags ${promo.productName}"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        promo.categorizedHashtags.viralTrending.forEach { tag ->
                            SuggestionChip(
                                onClick = { copyTextToClipboard(context, tag, "Tagar disalin") },
                                label = { Text(tag, fontSize = 13.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }
        }

        // 2. Niche & Category Specific
        if (promo.categorizedHashtags.nicheCategory.isNotEmpty()) {
            item {
                ResultCardContainer(
                    title = "🎯 Tagar Niche Kategori Produk",
                    subtitle = "Menargetkan pembeli yang spesifik mencari jenis produk ini",
                    textToCopy = promo.categorizedHashtags.nicheCategory.joinToString(" "),
                    photoUri = promo.photoUri,
                    shareTitle = "Niche Tags ${promo.productName}"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        promo.categorizedHashtags.nicheCategory.forEach { tag ->
                            SuggestionChip(
                                onClick = { copyTextToClipboard(context, tag, "Tagar disalin") },
                                label = { Text(tag, fontSize = 13.sp, color = SecondaryEmerald, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }
        }

        // 3. Local UMKM & Brand
        if (promo.categorizedHashtags.localUmkm.isNotEmpty()) {
            item {
                ResultCardContainer(
                    title = "🇮🇩 Tagar Komunitas UMKM & Lokal",
                    subtitle = "Membangun kredibilitas dan dukungan produk lokal",
                    textToCopy = promo.categorizedHashtags.localUmkm.joinToString(" "),
                    photoUri = promo.photoUri,
                    shareTitle = "UMKM Tags ${promo.productName}"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        promo.categorizedHashtags.localUmkm.forEach { tag ->
                            SuggestionChip(
                                onClick = { copyTextToClipboard(context, tag, "Tagar disalin") },
                                label = { Text(tag, fontSize = 13.sp, color = TertiaryAmber, fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Promo, Flash Sale & Discount Tags
        if (promo.categorizedHashtags.promoDiscount.isNotEmpty()) {
            item {
                ResultCardContainer(
                    title = "🏷️ Tagar Diskon & Promo Menarik",
                    subtitle = "Menarik pemburu diskon dan promo hemat",
                    textToCopy = promo.categorizedHashtags.promoDiscount.joinToString(" "),
                    photoUri = promo.photoUri,
                    shareTitle = "Promo Tags ${promo.productName}"
                ) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        promo.categorizedHashtags.promoDiscount.forEach { tag ->
                            SuggestionChip(
                                onClick = { copyTextToClipboard(context, tag, "Tagar disalin") },
                                label = { Text(tag, fontSize = 13.sp, color = Color(0xFF8B5CF6), fontWeight = FontWeight.SemiBold) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HooksAndTitlesTab(promo: GeneratedPromo) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Alternative Titles
        item {
            Text(
                text = "Alternatif Nama / Judul Produk Menjual",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(promo.alternativeTitles) { title ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { copyTextToClipboard(context, title) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 10 Viral Hooks
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "10 Pilihan Hook Pembuka Viral",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        itemsIndexed(promo.viralHooks) { index, hook ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(SecondaryEmerald.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = SecondaryEmerald,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = hook,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IconButton(
                        onClick = { copyTextToClipboard(context, hook) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin Hook",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // CTA Variations
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Variasi Call to Action (CTA)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(promo.ctaVariations) { cta ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = cta,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = { copyTextToClipboard(context, cta) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Salin CTA",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdsTab(promo: GeneratedPromo) {
    val adLabels = listOf(
        "short" to "Iklan Singkat (2-3 Baris)",
        "whatsapp" to "Broadcast WhatsApp",
        "instagram" to "Iklan Feed / Story Instagram",
        "facebook" to "Iklan Facebook & Fanpage",
        "tiktok" to "Naskah Video TikTok (Voiceover)",
        "marketplace" to "Deskripsi Produk Shopee / Tokopedia"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(adLabels) { (key, label) ->
            val text = promo.adVariations[key] ?: "Teks iklan untuk format ini..."
            ResultCardContainer(
                title = label,
                textToCopy = text,
                photoUri = promo.photoUri,
                shareTitle = label
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun WeeklyPlanTab(promo: GeneratedPromo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(promo.weeklyPlan) { day ->
            ResultCardContainer(
                title = day.dayName,
                subtitle = day.theme,
                textToCopy = "${day.dayName} (${day.theme})\nKonsep: ${day.concept}\n\n${day.readyCaption}",
                shareTitle = day.dayName
            ) {
                Column {
                    Surface(
                        color = PrimaryIndigo.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "💡 Konsep: ${day.concept}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = PrimaryIndigo,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = day.readyCaption,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatAndFollowUpTab(promo: GeneratedPromo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "💬 Asisten Balas Chat Cepat (FAQ Pembeli)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(promo.quickReplies) { reply ->
            ResultCardContainer(
                title = reply.questionCategory,
                subtitle = "Tanya: \"${reply.questionSample}\"",
                textToCopy = reply.suggestedReply,
                shareTitle = reply.questionCategory
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = reply.suggestedReply,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "🎯 Template Follow-Up Pembeli",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(promo.followUps) { fu ->
            ResultCardContainer(
                title = fu.title,
                subtitle = "Target: ${fu.targetBuyerCondition}",
                textToCopy = fu.messageText,
                shareTitle = fu.title
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = fu.messageText,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PromoAndStorytellingTab(promo: GeneratedPromo) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (promo.promoCopy.isNotBlank()) {
            item {
                ResultCardContainer(
                    title = "🔥 Teks Penawaran Promo & Diskon",
                    subtitle = "Mendorong calon pembeli segera transaksi sekarang",
                    textToCopy = promo.promoCopy,
                    shareTitle = "Promo ${promo.productName}"
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = promo.promoCopy,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        if (promo.storytelling.isNotBlank()) {
            item {
                ResultCardContainer(
                    title = "📖 Naskah Storytelling Produk",
                    subtitle = "Cerita inspiratif dan menyentuh hati di balik brand",
                    textToCopy = promo.storytelling,
                    shareTitle = "Cerita ${promo.productName}"
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = promo.storytelling,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CarouselProTab(promo: GeneratedPromo) {
    val context = LocalContext.current
    val slides = promo.carouselSlides.ifEmpty {
        listOf(
            CarouselSlideItem(1, "Produk Utama", "✨ PRODUK UTAMA", promo.productName, "Solusi terbaik untuk kebutuhan harian Anda.", "Foto produk tampak depan pencahayaan terang"),
            CarouselSlideItem(2, "Masalah Pelanggan", "💡 MASALAH UMUM", "Sering Mengalami Kendala Ini?", "Banyak yang merasa repot tanpa produk yang tepat.", "Foto ekspresi bingung atau masalah pelanggan"),
            CarouselSlideItem(3, "Solusi Praktis", "🔥 SOLUSI NYATA", "Hadir Memberi Jawaban Pasti", "Praktis, hemat waktu dan berkualitas premium.", "Foto produk saat digunakan secara mudah"),
            CarouselSlideItem(4, "Kualitas Unggul", "⭐ KEUNGGULAN #1", "Bahan Berkualitas Tinggi", "Dibuat dengan standar terbaik agar tahan lama.", "Foto close up detail tekstur dan material"),
            CarouselSlideItem(5, "Desain Modern", "✨ KEUNGGULAN #2", "Desain Ergonomis & Modern", "Nyaman dipakai setiap saat tanpa khawatir.", "Foto produk dari sudut samping estetik"),
            CarouselSlideItem(6, "Harga Terbaik", "💎 KEUNGGULAN #3", "Harga Terjangkau & Worth It", "Investasi terbaik tanpa menguras kantong.", "Foto produk dengan packaging rapi"),
            CarouselSlideItem(7, "Testimoni Asli", "💬 TESTIMONI JUJUR", "Kata Mereka yang Sudah Coba", "\"Beneran puas, kualitas melebihi ekspektasi!\"", "Screenshot rating bintang 5 atau review"),
            CarouselSlideItem(8, "Penawaran Promo", "🎁 PENAWARAN SPESIAL", "Promo Terbatas Minggu Ini", "Dapatkan potongan harga dan bonus khusus.", "Foto produk dengan banner diskon promo"),
            CarouselSlideItem(9, "Garansi Resmi", "🛡️ GARANSI KEPUASAN", "100% Aman & Terpercaya", "Garansi uang kembali dan pelayanan ramah.", "Foto sertifikat garansi atau segel resmi"),
            CarouselSlideItem(10, "Cara Pemesanan", "🚀 CARA ORDER MUDAH", "Pesan Sekarang Sebelum Kehabisan", "Klik link di bio atau kirim pesan WhatsApp.", "Foto katalog lengkap dengan tombol CTA")
        )
    }

    var activeSlideIndex by remember { mutableIntStateOf(0) }
    var isAutoPlaying by remember { mutableStateOf(false) }

    val activeSlide = slides.getOrElse(activeSlideIndex) { slides.first() }
    val photoCount = promo.photoUris.size
    val activePhotoUri = if (photoCount > 0) {
        promo.photoUris.getOrElse(activeSlideIndex % photoCount) { promo.photoUris.first() }
    } else {
        promo.photoUri
    }

    // Auto slide timer
    LaunchedEffect(isAutoPlaying, activeSlideIndex) {
        if (isAutoPlaying) {
            delay(3500)
            activeSlideIndex = (activeSlideIndex + 1) % slides.size
        }
    }

    // Combine all 10 slides into single full script
    val allSlidesFullText = buildString {
        appendLine("🎞️ KORSEL 10 SLIDE PRO: ${promo.productName.uppercase()}")
        appendLine("━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        slides.forEach { s ->
            appendLine("📌 SLIDE ${s.slideNumber}: [${s.badgeLabel}] ${s.headline}")
            appendLine("📝 Naskah: ${s.captionText}")
            appendLine("📸 Panduan Foto: ${s.recommendedVisual}")
            appendLine("----------------------------")
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP ACTION BANNER
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(TertiaryAmber, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Collections,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Korsel & Slide Pro (10 Foto)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = TertiaryAmber,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "PRO",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Black,
                                            fontSize = 9.sp
                                        ),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Formula 10 slide berantai siap pakai untuk Instagram Carousel, TikTok Photo Mode & Status WA Berantai.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                copyTextToClipboard(context, allSlidesFullText, "Semua 10 Slide berhasil disalin! 📋")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Salin 10 Slide", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }

                        Button(
                            onClick = {
                                if (promo.photoUris.isNotEmpty()) {
                                    shareMultiplePromoImages(context, allSlidesFullText, promo.photoUris, "Korsel Promosi ${promo.productName}")
                                } else {
                                    sharePromoContent(context, allSlidesFullText, promo.photoUri, title = "Korsel Promosi ${promo.productName}")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan Foto", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }
        }

        // INTERACTIVE SLIDE SIMULATOR (VISUAL PREVIEW)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "📱 Simulasi Tampilan Slide",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        // Auto Play / Pause button
                        Surface(
                            color = if (isAutoPlaying) SecondaryEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { isAutoPlaying = !isAutoPlaying }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isAutoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = if (isAutoPlaying) SecondaryEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isAutoPlaying) "Jeda Auto" else "Putar Auto",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAutoPlaying) SecondaryEmerald else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // THE SLIDE CANVAS
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        PrimaryIndigo.copy(alpha = 0.85f),
                                        Color(0xFF0F172A)
                                    )
                                )
                            )
                    ) {
                        // Background Photo if available
                        if (activePhotoUri != null) {
                            AsyncImage(
                                model = activePhotoUri,
                                contentDescription = "Foto Slide ${activeSlide.slideNumber}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            // Dark gradient overlay for readability
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.4f),
                                                Color.Black.copy(alpha = 0.2f),
                                                Color.Black.copy(alpha = 0.85f)
                                            )
                                        )
                                    )
                            )
                        }

                        // Top Bar over slide
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .align(Alignment.TopCenter)
                        ) {
                            Surface(
                                color = TertiaryAmber,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = activeSlide.badgeLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                color = Color.Black.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "Slide ${activeSlide.slideNumber} / ${slides.size}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        // Bottom Text Overlay on slide
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = activeSlide.headline,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    lineHeight = 26.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeSlide.captionText,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.White.copy(alpha = 0.9f),
                                    lineHeight = 18.sp
                                ),
                                maxLines = 3
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // SLIDE CONTROLS (Prev / Next & Thumbnail dots)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconButton(
                            onClick = {
                                activeSlideIndex = if (activeSlideIndex > 0) activeSlideIndex - 1 else slides.size - 1
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Slide Sebelumnya",
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Thumbnails row (1 to 10)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                        ) {
                            itemsIndexed(slides) { index, _ ->
                                val isSelected = activeSlideIndex == index
                                Surface(
                                    color = if (isSelected) PrimaryIndigo else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable { activeSlideIndex = index }
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                activeSlideIndex = (activeSlideIndex + 1) % slides.size
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Slide Selanjutnya",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ACTIVE SLIDE DETAILS & COPY
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "📸 Panduan Pengambilan Foto Slide #${activeSlide.slideNumber}:",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryIndigo
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = activeSlide.recommendedVisual,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "✍️ Naskah Slide #${activeSlide.slideNumber}:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${activeSlide.headline}\n\n${activeSlide.captionText}",
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        copyTextToClipboard(
                                            context,
                                            "[Slide ${activeSlide.slideNumber}] ${activeSlide.headline}\n${activeSlide.captionText}",
                                            "Naskah Slide #${activeSlide.slideNumber} disalin!"
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Salin Slide Ini", style = MaterialTheme.typography.labelSmall)
                                }

                                Button(
                                    onClick = {
                                        val singleText = "[Slide ${activeSlide.slideNumber}: ${activeSlide.badgeLabel}]\n${activeSlide.headline}\n\n${activeSlide.captionText}"
                                        sharePromoContent(context, singleText, activePhotoUri, title = "Slide ${activeSlide.slideNumber} ${promo.productName}")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Bagikan Slide", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        // ALL 10 SLIDES DETAILED LIST
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "📑 Rincian Lengkap Semua Slide (1 - 10)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        itemsIndexed(slides) { idx, slide ->
            val slidePhoto = if (photoCount > 0) {
                promo.photoUris.getOrElse(idx % photoCount) { promo.photoUris.first() }
            } else {
                promo.photoUri
            }

            ResultCardContainer(
                title = "Slide #${slide.slideNumber}: ${slide.headline}",
                subtitle = "Badge: ${slide.badgeLabel}",
                textToCopy = "[Slide ${slide.slideNumber} - ${slide.badgeLabel}]\n${slide.headline}\n\n${slide.captionText}\n\n📸 Saran Foto: ${slide.recommendedVisual}",
                photoUri = slidePhoto,
                shareTitle = "Slide ${slide.slideNumber} ${promo.productName}"
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = TertiaryAmber.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "🏷️ Label: ${slide.badgeLabel}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TertiaryAmber
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = slide.captionText,
                                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = SecondaryEmerald,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Saran Foto: ${slide.recommendedVisual}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

