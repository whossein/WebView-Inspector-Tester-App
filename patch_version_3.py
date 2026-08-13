import sys

filepath = 'app/build.gradle.kts'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('versionCode = 4', 'versionCode = 5')
content = content.replace('versionName = "2.2"', 'versionName = "2.3"')

with open(filepath, 'w') as f:
    f.write(content)
print("Bumped version to 2.3")
