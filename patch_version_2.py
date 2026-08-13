import sys

filepath = 'app/build.gradle.kts'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('versionCode = 3', 'versionCode = 4')
content = content.replace('versionName = "2.1"', 'versionName = "2.2"')

with open(filepath, 'w') as f:
    f.write(content)
print("Bumped version to 2.2")
