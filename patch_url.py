import sys

filepath = 'app/src/main/java/com/example/ui/InspectorViewModel.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('"https://pwa-builder.github.io/pwa-starter/"', '"https://daramet.com/whossein"')

with open(filepath, 'w') as f:
    f.write(content)

filepath = 'app/src/main/java/com/example/data/Entities.kt'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('"https://pwa-builder.github.io/pwa-starter/"', '"https://daramet.com/whossein"')

with open(filepath, 'w') as f:
    f.write(content)

print("Updated URLs successfully.")
