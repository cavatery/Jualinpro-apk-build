package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CopywritingTone
import com.example.model.CustomerReplyItem
import com.example.model.FollowUpTemplate
import com.example.model.GeneratedPromo
import com.example.model.ProductCategory
import com.example.model.PromotionPlatform
import com.example.model.PromoType
import com.example.ui.components.AppHeader
import com.example.ui.components.ResultCardContainer
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryEmerald
import com.example.ui.viewmodel.MainViewModel

@Composable
fun CaptionStudioScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenResult: (GeneratedPromo) -> Unit,
    modifier: Modifier = Modifier
) {
    StudioGeneratorScreen(
        title = "AI Caption Generator",
        subtitle = "Buat caption jualan dengan 11 gaya bahasa",
        icon = Icons.Default.Create,
        viewModel = viewModel,
        onBack = onBack,
        onSuccess = onOpenResult,
        modifier = modifier
    )
}

@Composable
fun AdStudioScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenResult: (GeneratedPromo) -> Unit,
    modifier: Modifier = Modifier
) {
    StudioGeneratorScreen(
        title = "Iklan Multi-Platform AI",
        subtitle = "Format iklan Instagram, FB, TikTok, WA & Shopee",
        icon = Icons.Default.Campaign,
        viewModel = viewModel,
        onBack = onBack,
        onSuccess = onOpenResult,
        modifier = modifier
    )
}

@Composable
fun ContentPlanScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenResult: (GeneratedPromo) -> Unit,
    modifier: Modifier = Modifier
) {
    StudioGeneratorScreen(
        title = "Jadwal Konten 7 Hari",
        subtitle = "Ide tema harian & caption siap posting seminggu",
        icon = Icons.Default.DateRange,
        viewModel = viewModel,
        onBack = onBack,
        onSuccess = onOpenResult,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatAssistantScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenResult: (GeneratedPromo) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    var selectedTab by remember { mutableIntStateOf(0) }
    var copiedId by remember { mutableStateOf<String?>(null) }

    val quickScenarios = listOf(
        "📦 Apakah produk ini masih ready stock kak?",
        "💰 Boleh nego / minta diskon gak kak?",
        "🚚 Berapa ongkir & estimasi sampai ke alamat saya?",
        "🤝 Apakah bisa bayar di tempat (COD)?",
        "💔 Kak, barang yang saya terima rusak / tidak sesuai",
        "⏰ Halo kak, jadi ambil pesanan yang kemarin?",
        "💳 Boleh minta nomor rekening & format ordernya?",
        "⭐ Minta review & testimoni kepuasan pelanggan"
    )

    val tones = listOf(
        "🌸 Ramah & Hangat",
        "⚡ Fast Closing & Urgent",
        "📦 Info Pengiriman & Stok",
        "🛡️ Tangani Komplain Santun",
        "⏰ Follow Up Lembut"
    )

    // Standard pre-made templates for instant zero-wait responses
    val defaultTemplates = remember(uiState.currentInput.productName, uiState.currentInput.shopName) {
        val prodName = uiState.currentInput.productName.ifBlank { "produk kami" }
        val shopName = uiState.currentInput.shopName.ifBlank { "Toko Kami" }
        listOf(
            CustomerReplyItem(
                id = "tpl_salam",
                questionCategory = "👋 Salam Hangat & Fast Respon",
                questionSample = "Sapaan Awal Pelanggan",
                suggestedReply = "Halo kak! Selamat datang di $shopName 😊 Terima kasih sudah menghubungi kami. Ada yang bisa kami bantu mengenai $prodName hari ini?"
            ),
            CustomerReplyItem(
                id = "tpl_ready",
                questionCategory = "📦 Konfirmasi Stok Siap Kirim",
                questionSample = "Tanya Kesiapan Barang",
                suggestedReply = "Halo kak! $prodName ready stock dan siap dikirim hari ini ya kak. Mau dikirim ke kota/kabupaten mana agar kami bantu siapkan pesanannya? ✨"
            ),
            CustomerReplyItem(
                id = "tpl_format",
                questionCategory = "💳 Format Order & Pembayaran",
                questionSample = "Formulir Pemesanan Cepat",
                suggestedReply = "Siap kak! Untuk mempercepat proses pengiriman $prodName, silakan lengkapi format order berikut ya:\n\n• Nama Penerima:\n• No. WhatsApp:\n• Alamat Lengkap + Kode Pos:\n• Jumlah Pesanan:\n\nSetelah dikirim, kami langsung buatkan invoice dan siapkan packing amannya ya kak! 🙏"
            ),
            CustomerReplyItem(
                id = "tpl_followup",
                questionCategory = "⏰ Follow Up Lembut (Belum Transfer)",
                questionSample = "Pengingat Pesanan Tersimpan",
                suggestedReply = "Halo kak 😊 Mau info, pesanan $prodName kakak sudah kami amankan ya. Mau sekalian kami jadwalkan kirim dengan kurir sore ini biar cepat sampai? Terima kasih kak!"
            ),
            CustomerReplyItem(
                id = "tpl_komplain",
                questionCategory = "🛡️ Respon Komplain Santun & Solutif",
                questionSample = "Penanganan Masalah / Garansi",
                suggestedReply = "Halo kak, mohon maaf sekali atas ketidaknyamanannya 🙏 Kepuasan kakak adalah prioritas kami. Boleh kirimkan foto/video kendala produknya kak? Kami akan berikan solusi terbaik / ganti baru secepatnya ya kak."
            )
        )
    }

    Scaffold(
        topBar = {
            AppHeader(
                title = "Asisten Balas Chat CS AI",
                subtitle = "Balas pesan pembeli & teknik closing cepat",
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Context Card: Product & Shop (No Photo generation needed)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PrimaryIndigo.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Konteks Produk & CS",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "AI akan membalas chat secara otomatis sesuai data produk ini",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = uiState.currentInput.productName,
                                onValueChange = { viewModel.updateInput { prev -> prev.copy(productName = it) } },
                                label = { Text("Nama Produk") },
                                placeholder = { Text("Contoh: Keripik Pisang / Gamis") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.2f)
                            )

                            OutlinedTextField(
                                value = uiState.currentInput.promoPrice.ifBlank { uiState.currentInput.normalPrice },
                                onValueChange = { viewModel.updateInput { prev -> prev.copy(promoPrice = it) } },
                                label = { Text("Harga/Promo") },
                                placeholder = { Text("Rp25.000") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.8f)
                            )
                        }
                    }
                }
            }

            // Input Section: Customer Chat / Inquiry
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.QuestionAnswer,
                                contentDescription = null,
                                tint = SecondaryEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Pesan / Pertanyaan Pembeli:",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.chatInquiryInput,
                            onValueChange = { viewModel.setChatInquiry(it) },
                            placeholder = { Text("Ketik atau pilih pertanyaan pembeli di bawah...") },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_inquiry_field")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Pilih Contoh Pertanyaan Cepat:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            quickScenarios.forEach { scenario ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (uiState.chatInquiryInput == scenario) PrimaryIndigo.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (uiState.chatInquiryInput == scenario) PrimaryIndigo else Color.Transparent
                                    ),
                                    modifier = Modifier.clickable {
                                        viewModel.setChatInquiry(scenario)
                                    }
                                ) {
                                    Text(
                                        text = scenario,
                                        fontSize = 11.sp,
                                        fontWeight = if (uiState.chatInquiryInput == scenario) FontWeight.Bold else FontWeight.Normal,
                                        color = if (uiState.chatInquiryInput == scenario) PrimaryIndigo else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Gaya Sikap CS:",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tones.forEach { tone ->
                                FilterChip(
                                    selected = uiState.chatSelectedTone == tone,
                                    onClick = { viewModel.setChatSelectedTone(tone) },
                                    label = { Text(tone, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // AI Generate Button (Chat ONLY, no photo generation)
                        Button(
                            onClick = {
                                viewModel.generateChatAssistantReplies()
                            },
                            enabled = !uiState.isGeneratingChat && uiState.chatInquiryInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("generate_chat_button")
                        ) {
                            if (uiState.isGeneratingChat) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("AI Sedang Meracik Balasan...")
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("BUAT BALASAN CHAT AI ⚡", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Results Section Tab Header
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryIndigo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Hasil AI (${uiState.chatGeneratedReplies.size})",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "Template Siap Pakai (${defaultTemplates.size})",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }

            // Display Content Based on Tab
            if (selectedTab == 0) {
                if (uiState.chatGeneratedReplies.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = PrimaryIndigo.copy(alpha = 0.6f),
                                    modifier = Modifier.size(44.dp)
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Belum ada balasan yang digenerate",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Pilih pertanyaan pembeli di atas lalu klik 'Buat Balasan Chat AI' untuk mendapatkan variasi jawaban closing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.chatGeneratedReplies, key = { it.id }) { replyItem ->
                        ChatReplyCard(
                            replyItem = replyItem,
                            isCopied = copiedId == replyItem.id,
                            onCopy = { text ->
                                clipboardManager.setText(AnnotatedString(text))
                                copiedId = replyItem.id
                                Toast.makeText(context, "Balasan chat berhasil disalin! 📋", Toast.LENGTH_SHORT).show()
                            },
                            onShareWhatsApp = { text ->
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, text)
                                    type = "text/plain"
                                }
                                val shareIntent = Intent.createChooser(sendIntent, "Kirim Balasan via WhatsApp / Sosmed")
                                context.startActivity(shareIntent)
                            }
                        )
                    }
                }
            } else {
                items(defaultTemplates, key = { it.id }) { templateItem ->
                    ChatReplyCard(
                        replyItem = templateItem,
                        isCopied = copiedId == templateItem.id,
                        onCopy = { text ->
                            clipboardManager.setText(AnnotatedString(text))
                            copiedId = templateItem.id
                            Toast.makeText(context, "Template disalin ke papan klip! 📋", Toast.LENGTH_SHORT).show()
                        },
                        onShareWhatsApp = { text ->
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Kirim Template via WhatsApp")
                            context.startActivity(shareIntent)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatReplyCard(
    replyItem: CustomerReplyItem,
    isCopied: Boolean,
    onCopy: (String) -> Unit,
    onShareWhatsApp: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryIndigo.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = replyItem.questionCategory,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = PrimaryIndigo,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { onCopy(replyItem.suggestedReply) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Salin",
                            tint = if (isCopied) SecondaryEmerald else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onShareWhatsApp(replyItem.suggestedReply) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Kirim ke WA",
                            tint = SecondaryEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = replyItem.suggestedReply,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onCopy(replyItem.suggestedReply) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = if (isCopied) SecondaryEmerald else MaterialTheme.colorScheme.onSurface),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isCopied) "Tersalin!" else "Salin Chat", fontSize = 12.sp)
                }

                Button(
                    onClick = { onShareWhatsApp(replyItem.suggestedReply) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Kirim ke WA", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PromoStudioScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenResult: (GeneratedPromo) -> Unit,
    modifier: Modifier = Modifier
) {
    StudioGeneratorScreen(
        title = "Copywriter Promo & Diskon",
        subtitle = "Penawaran menarik, diskon, & flash sale",
        icon = Icons.Default.LocalOffer,
        viewModel = viewModel,
        onBack = onBack,
        onSuccess = onOpenResult,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StudioGeneratorScreen(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onSuccess: (GeneratedPromo) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val input = uiState.currentInput

    Scaffold(
        topBar = {
            AppHeader(
                title = title,
                subtitle = subtitle,
                onBackClick = onBack
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PrimaryIndigo.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Atur Produk untuk $title",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "AI akan menyesuaikan output sesuai gaya bahasa pilihan",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = input.productName,
                            onValueChange = { viewModel.updateInput { prev -> prev.copy(productName = it) } },
                            label = { Text("Nama Produk *") },
                            placeholder = { Text("Contoh: Keripik Pisang Coklat / Gamis Rayon") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Kategori Produk:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ProductCategory.values().forEach { cat ->
                                FilterChip(
                                    selected = input.category == cat,
                                    onClick = { viewModel.updateInput { prev -> prev.copy(category = cat) } },
                                    label = { Text("${cat.iconEmoji} ${cat.displayName}", fontSize = 12.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = input.normalPrice,
                                onValueChange = { viewModel.updateInput { prev -> prev.copy(normalPrice = it) } },
                                label = { Text("Harga Normal") },
                                placeholder = { Text("Rp25.000") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = input.promoPrice,
                                onValueChange = { viewModel.updateInput { prev -> prev.copy(promoPrice = it) } },
                                label = { Text("Harga Promo") },
                                placeholder = { Text("Rp19.900") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = input.uspList,
                            onValueChange = { viewModel.updateInput { prev -> prev.copy(uspList = it) } },
                            label = { Text("Keunggulan / Bahan / USP") },
                            placeholder = { Text("Contoh: Bahan premium, renyah, higienis, garansi") },
                            minLines = 2,
                            maxLines = 4,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Gaya Bahasa:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                CopywritingTone.DIRECT,
                                CopywritingTone.PERSUASIVE,
                                CopywritingTone.VIRAL,
                                CopywritingTone.CASUAL
                            ).forEach { tone ->
                                FilterChip(
                                    selected = input.tone == tone,
                                    onClick = { viewModel.updateInput { prev -> prev.copy(tone = tone) } },
                                    label = { Text(tone.displayName, fontSize = 11.sp) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                viewModel.generatePromotion { promo ->
                                    onSuccess(promo)
                                }
                            },
                            enabled = !uiState.isGenerating && input.productName.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryEmerald),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            if (uiState.isGenerating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Sedang Menghasilkan...")
                            } else {
                                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("GENERATE SEKARANG ✨", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

