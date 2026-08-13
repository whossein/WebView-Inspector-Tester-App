package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val DONATION_URL = "https://daramet.com/whossein"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportSheet(
    sheetState: SheetState,
    onLoadUrlInApp: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heart Circle Icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFCE4EC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Heart",
                    tint = Color(0xFFE91E63),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "حمایت از توسعه‌دهنده برنامه",
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "سلام! اگر این ابزار به تست، اشکال‌زدایی و بهبود ابزارهای PWA و وب‌سایت شما کمک کرده، می‌توانید با حمایت مالی دلخواه خود به ماندگاری و توسعه ابزارهای بیشتر کمک کنید. ❤️",
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Donation Link Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "لینک درگاه حمایت (دارَمت):",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = DONATION_URL,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Donation Link", DONATION_URL)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "لینک حمایت کپی شد!", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            Button(
                onClick = {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DONATION_URL))
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "خطا در باز کردن مرورگر", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "باز کردن صفحه حمایت (Daramet)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = {
                    onLoadUrlInApp(DONATION_URL)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "باز کردن در همین برنامه",
                    fontSize = 13.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // App Version
            val packageInfo = try {
                context.packageManager.getPackageInfo(context.packageName, 0)
            } catch (e: Exception) {
                null
            }
            Text(
                text = "نسخه برنامه: ${packageInfo?.versionName ?: "1.0"}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }
    }
}
