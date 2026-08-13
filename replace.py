import sys

filepath = 'app/src/main/java/com/example/ui/components/FloatingControlsOverlay.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """                // Address Input
                TextField(
                    value = state.urlInput,
                    onValueChange = onUrlChange,
                    placeholder = { Text("https://example.com", fontSize = 14.sp) },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(onGo = {
                        focusManager.clearFocus()
                        onLoadUrl()
                    }),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("url_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        IconButton(onClick = {
                            focusManager.clearFocus()
                            onLoadUrl()
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Go", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                )"""

replacement = """                // Address Input
                Box(modifier = Modifier.weight(1f)) {
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
                    ) {
                        TextField(
                            value = state.urlInput,
                            onValueChange = onUrlChange,
                            placeholder = { Text("https://example.com", fontSize = 14.sp) },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 14.sp,
                                textDirection = androidx.compose.ui.text.style.TextDirection.Ltr
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                            keyboardActions = KeyboardActions(onGo = {
                                focusManager.clearFocus()
                                onLoadUrl()
                            }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("url_input_field"),
                            shape = RoundedCornerShape(24.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }
                }"""

if target in content:
    with open(filepath, 'w') as f:
        f.write(content.replace(target, replacement))
    print("Replaced successfully.")
else:
    print("Target not found.")

