package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.GeneratedPromo
import com.example.ui.components.AppHeader
import com.example.ui.components.CopyButton
import com.example.ui.components.ResultCardContainer
import com.example.ui.components.ShareButton
import com.example.ui.components.copyTextToClipboard
import com.example.ui.components.shareText
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryEmerald
import com.example.ui.theme.TertiaryAmber
import com.example.ui.viewmodel.MainViewModel
import androidx.compose.material3.ExperimentalMaterial3Api

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
        "🎯 Judul & Hook",
        "📢 Iklan Multi-Platform",
        "💡 Jadwal 7 Hari",
        "💬 Balas Chat & Follow Up",
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
                            shareText(context, currentPromo.mainCaption, "Materi Promosi ${currentPromo.productName}")
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
                1 -> HooksAndTitlesTab(promo = currentPromo)
                2 -> AdsTab(promo = currentPromo)
                3 -> WeeklyPlanTab(promo = currentPromo)
                4 -> ChatAndFollowUpTab(promo = currentPromo)
                5 -> PromoAndStorytellingTab(promo = currentPromo)
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

@Composable
fun CaptionTab(
    promo: GeneratedPromo,
    onEdit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (promo.photoUri != null) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = promo.photoUri,
                        contentDescription = "Foto Produk",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        item {
            ResultCardContainer(
                title = "Caption Jualan Siap Posting",
                subtitle = "Format lengkap dengan hook, deskripsi, CTA & hashtag",
                textToCopy = promo.mainCaption,
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
                    title = "Hashtag Relevan (#)",
                    subtitle = "Gunakan untuk menaikkan jangkauan postingan",
                    textToCopy = promo.hashtags.joinToString(" "),
                    shareTitle = "Hashtags ${promo.productName}"
                ) {
                    Text(
                        text = promo.hashtags.joinToString(" "),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = PrimaryIndigo,
                            fontWeight = FontWeight.Medium
                        )
                    )
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
