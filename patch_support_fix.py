import sys

filepath = 'app/src/main/java/com/example/ui/components/SupportSheet.kt'
with open(filepath, 'r') as f:
    content = f.read()

# I mistakenly changed Icons.Default.OpenInNew to automirrored without changing the import
# Let's change the usage back, or fix the import. 
# Fixing the usage back to Icons.Default.OpenInNew is easier since I don't know what imports are there.

content = content.replace("androidx.compose.material.icons.automirrored.filled.OpenInNew", "Icons.Default.OpenInNew")

with open(filepath, 'w') as f:
    f.write(content)
print("Patched SupportSheet fix successfully.")
