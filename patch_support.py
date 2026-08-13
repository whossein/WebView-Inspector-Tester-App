import sys

filepath = 'app/src/main/java/com/example/ui/components/SupportSheet.kt'
with open(filepath, 'r') as f:
    content = f.read()

target = """            OutlinedButton(
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
        }
    }
}"""

replacement = """            OutlinedButton(
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
}"""

content = content.replace("Icons.Default.OpenInNew", "androidx.compose.material.icons.automirrored.filled.OpenInNew")

if target in content:
    with open(filepath, 'w') as f:
        f.write(content.replace(target, replacement))
    print("Patched SupportSheet successfully.")
else:
    print("Target not found in SupportSheet.")
