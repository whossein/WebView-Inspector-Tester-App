package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TestProfile
import com.example.data.UrlHistory
import com.example.data.Bookmark
import androidx.compose.material.icons.filled.Home

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilesSheet(
    sheetState: SheetState,
    profiles: List<TestProfile>,
    history: List<UrlHistory>,
    bookmarks: List<Bookmark>,
    currentUrl: String,
    onSaveProfile: (String) -> Unit,
    onLoadProfile: (TestProfile) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onSelectHistoryUrl: (String) -> Unit,
    onClearHistory: () -> Unit,
    onAddBookmark: (String, String) -> Unit,
    onDeleteBookmark: (Long) -> Unit,
    onSetHomePage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var profileNameInput by remember { mutableStateOf("") }
    var bookmarkNameInput by remember { mutableStateOf("") }

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
                Text(
                    text = "Profiles & History",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Saved Setups") },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Bookmarks") },
                    icon = { Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("History") },
                    icon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(16.dp)) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Save current profile input
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Save Current Params & URL as Preset",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = profileNameInput,
                                onValueChange = { profileNameInput = it },
                                placeholder = { Text("Profile Name (e.g. Dark Theme Staging)") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (profileNameInput.isNotBlank()) {
                                        onSaveProfile(profileNameInput)
                                        profileNameInput = ""
                                    }
                                },
                                enabled = profileNameInput.isNotBlank()
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (profiles.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No saved test profiles yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "Save your favorite query parameters and target URL for 1-tap testing later.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profiles, key = { it.id }) { profile ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = profile.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = profile.baseUrl,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Button(
                                            onClick = { onLoadProfile(profile) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Load")
                                        }

                                        IconButton(onClick = { onDeleteProfile(profile.id) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Delete Profile",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (selectedTab == 1) {
                // Bookmarks & Home Page Tab
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Save Current URL as Bookmark",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = bookmarkNameInput,
                                onValueChange = { bookmarkNameInput = it },
                                placeholder = { Text("Bookmark Title") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = {
                                    if (bookmarkNameInput.isNotBlank()) {
                                        onAddBookmark(bookmarkNameInput, currentUrl)
                                        bookmarkNameInput = ""
                                    }
                                },
                                enabled = bookmarkNameInput.isNotBlank()
                            ) {
                                Text("Add")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                onSetHomePage(currentUrl)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Set Current URL as Home Page")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (bookmarks.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No bookmarks saved.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bookmarks, key = { it.id }) { b ->
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        onSelectHistoryUrl(b.url)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = b.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(text = b.url, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                    IconButton(onClick = { onDeleteBookmark(b.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Recent History Tab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Last 30 Opened URLs", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    if (history.isNotEmpty()) {
                        TextButton(onClick = onClearHistory) {
                            Text("Clear History")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (history.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No recent URLs in history", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(history, key = { it.id }) { item ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectHistoryUrl(item.fullUrl)
                                        onDismiss()
                                    },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Launch,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        if (item.pageTitle.isNotBlank()) {
                                            Text(
                                                text = item.pageTitle,
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = item.fullUrl,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
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
}
