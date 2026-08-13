package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserAgentPreset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAgentSheet(
    sheetState: SheetState,
    currentPreset: UserAgentPreset,
    customUserAgent: String,
    onSelectUserAgent: (UserAgentPreset, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedPreset by remember { mutableStateOf(currentPreset) }
    var customText by remember { mutableStateOf(customUserAgent) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Devices, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "User Agent Switcher",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            UserAgentPreset.entries.forEach { preset ->
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedPreset == preset)
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            selectedPreset = preset
                            onSelectUserAgent(preset, customText)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedPreset == preset,
                            onClick = {
                                selectedPreset = preset
                                onSelectUserAgent(preset, customText)
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = preset.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            if (preset.userAgentString != null) {
                                Text(
                                    text = preset.userAgentString,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2
                                )
                            } else if (preset == UserAgentPreset.DEFAULT_MOBILE) {
                                Text(
                                    text = "Default Android System WebView User Agent",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            if (selectedPreset == UserAgentPreset.CUSTOM) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customText,
                    onValueChange = {
                        customText = it
                        onSelectUserAgent(UserAgentPreset.CUSTOM, it)
                    },
                    placeholder = { Text("Enter custom User-Agent string...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }
    }
}
