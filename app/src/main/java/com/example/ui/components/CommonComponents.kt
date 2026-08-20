package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryIndigo
import com.example.ui.theme.SecondaryEmerald
import com.example.ui.theme.TertiaryAmber
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(
    title: String,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    isPro: Boolean = false,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isPro) {
                        Spacer(modifier = Modifier.width(8.dp))
                        ProBadge()
                    }
                }
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("nav_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali"
                    )
                }
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun ProBadge(modifier: Modifier = Modifier) {
    Surface(
        color = TertiaryAmber,
        shape = RoundedCornerShape(6.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "PRO",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp
                ),
                color = Color.White
            )
        }
    }
}

@Composable
fun CopyButton(
    textToCopy: String,
    label: String = "Salin",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            copyTextToClipboard(context, textToCopy)
            copied = true
            scope.launch {
                delay(2000)
                copied = false
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (copied) SecondaryEmerald else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (copied) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.testTag("copy_button")
    ) {
        Icon(
            imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = label,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = if (copied) "Tersalin! ✓" else label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun ShareButton(
    textToShare: String,
    photoUri: String? = null,
    title: String = "Bagikan Promosi",
    label: String = "Kirim / Share",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            sharePromoContent(context, textToShare, photoUri, title = title)
        },
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.testTag("share_button")
    ) {
        Icon(
            imageVector = Icons.Default.Share,
            contentDescription = "Bagikan",
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

@Composable
fun WhatsAppShareButton(
    textToShare: String,
    photoUri: String? = null,
    label: String = "Kirim ke WhatsApp (Foto + Teks)",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Button(
        onClick = {
            shareToWhatsApp(context, textToShare, photoUri)
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF25D366),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.testTag("btn_share_whatsapp")
    ) {
        Icon(
            imageVector = Icons.Default.Send,
            contentDescription = "WhatsApp",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
fun StatMetricCard(
    count: String,
    label: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = count,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ResultCardContainer(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    textToCopy: String,
    photoUri: String? = null,
    shareTitle: String = title,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    PrimaryIndigo.copy(alpha = 0.12f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = PrimaryIndigo,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body Content
            content()

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CopyButton(
                    textToCopy = textToCopy,
                    modifier = Modifier.weight(1f)
                )
                ShareButton(
                    textToShare = textToCopy,
                    photoUri = photoUri,
                    title = shareTitle,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

fun copyTextToClipboard(context: Context, text: String, message: String = "Berhasil disalin ke clipboard! 📋") {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Jualin AI Pro Copy", text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}

/**
 * Resolves a file/content URI into a secure shareable Content URI with read permissions.
 */
fun getShareableUri(context: Context, photoUriString: String?): Uri? {
    if (photoUriString.isNullOrBlank()) return null
    return try {
        val parsedUri = Uri.parse(photoUriString)
        if (parsedUri.scheme == "file" || (parsedUri.path != null && !parsedUri.scheme.equals("content", ignoreCase = true))) {
            val filePath = parsedUri.path ?: photoUriString
            val file = File(filePath)
            if (file.exists()) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                parsedUri
            }
        } else {
            parsedUri
        }
    } catch (e: Exception) {
        try {
            val file = File(photoUriString)
            if (file.exists()) {
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            } else {
                null
            }
        } catch (ex: Exception) {
            null
        }
    }
}

/**
 * Shares promo text and photo (if available) to any app (WhatsApp Status, Chat, Instagram, FB, etc.).
 */
fun sharePromoContent(
    context: Context,
    text: String,
    photoUriString: String? = null,
    targetPackage: String? = null,
    title: String = "Bagikan Promosi"
) {
    val shareableUri = getShareableUri(context, photoUriString)
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        if (shareableUri != null) {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, shareableUri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        if (targetPackage != null) {
            setPackage(targetPackage)
        }
    }

    try {
        if (targetPackage != null) {
            context.startActivity(sendIntent)
        } else {
            val chooser = Intent.createChooser(sendIntent, title).apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        try {
            val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
                if (shareableUri != null) {
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM, shareableUri)
                    putExtra(Intent.EXTRA_TEXT, text)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
            }
            context.startActivity(Intent.createChooser(fallbackIntent, title))
        } catch (ex: Exception) {
            Toast.makeText(context, "Tidak dapat membuka aplikasi berbagi: ${ex.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

/**
 * Direct shortcut to share to WhatsApp with Photo and Caption attached.
 */
fun shareToWhatsApp(context: Context, text: String, photoUriString: String? = null) {
    sharePromoContent(
        context = context,
        text = text,
        photoUriString = photoUriString,
        targetPackage = "com.whatsapp",
        title = "Kirim ke WhatsApp"
    )
}

fun shareText(context: Context, text: String, title: String = "Promosi Produk") {
    sharePromoContent(context, text, photoUriString = null, title = title)
}

/**
 * Shares multiple promo photos (up to 10 photos) along with a complete carousel caption.
 */
fun shareMultiplePromoImages(
    context: Context,
    text: String,
    photoUriStrings: List<String>,
    title: String = "Bagikan Korsel Promosi"
) {
    if (photoUriStrings.isEmpty()) {
        shareText(context, text, title)
        return
    }

    val uris = ArrayList<Uri>()
    for (uriStr in photoUriStrings) {
        val u = getShareableUri(context, uriStr)
        if (u != null) {
            uris.add(u)
        }
    }

    if (uris.isEmpty()) {
        shareText(context, text, title)
        return
    }

    if (uris.size == 1) {
        sharePromoContent(context, text, photoUriStrings.first(), title = title)
        return
    }

    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "image/*"
        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    try {
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Tidak dapat membuka aplikasi berbagi: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
