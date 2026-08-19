package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CopywritingTone
import com.example.model.GeneratedPromo
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

@Composable
fun ChatAssistantScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    onOpenResult: (GeneratedPromo) -> Unit,
    modifier: Modifier = Modifier
) {
    StudioGeneratorScreen(
        title = "Asisten Balas Chat & Follow-up",
        subtitle = "Template jawaban cepat & follow up pembeli",
        icon = Icons.AutoMirrored.Filled.Chat,
        viewModel = viewModel,
        onBack = onBack,
        onSuccess = onOpenResult,
        modifier = modifier
    )
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
