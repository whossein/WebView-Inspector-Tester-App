import sys

filepath = 'app/src/main/AndroidManifest.xml'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('android:icon="@mipmap/ic_launcher"', 'android:icon="@drawable/app_icon"')
content = content.replace('android:roundIcon="@mipmap/ic_launcher_round"', 'android:roundIcon="@drawable/app_icon"')

with open(filepath, 'w') as f:
    f.write(content)
print("Updated AndroidManifest.xml successfully.")
