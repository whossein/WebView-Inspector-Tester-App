import sys

filepath = 'app/build.gradle.kts'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('versionCode = 2', 'versionCode = 3')
content = content.replace('versionName = "2.0"', 'versionName = "2.1"')

with open(filepath, 'w') as f:
    f.write(content)
print("Bumped version to 2.1")
