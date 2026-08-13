import sys

filepath = 'app/src/main/AndroidManifest.xml'
with open(filepath, 'r') as f:
    content = f.read()

content = content.replace('android:icon="@drawable/app_icon"', 'android:icon="@mipmap/ic_launcher"')
content = content.replace('android:roundIcon="@drawable/app_icon"', 'android:roundIcon="@mipmap/ic_launcher_round"')

with open(filepath, 'w') as f:
    f.write(content)
print("Updated AndroidManifest.xml successfully.")
