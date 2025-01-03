# Android AAR Project

This Android project provides AAR (Android Archive) libraries for Unity integration and payment functionality.

https://www.jianshu.com/p/86b275da600e 制作思路来自这里
区别是添加了aar文件支持

unitylib 是unity 和 android 的桥梁
paylib 是支付的库 ，引入其他android库，参考这个paylib的结构
paylib/build.gradle 照着写
unitylib/build.gradle dependencies 添加`api project(':paylib')`
settings.gradle 添加`include ':paylib'`

要编release 注意要设置Build Variant 为release
编译用 Android Studio Build/RebuildProject, 或android studio 最左侧 Gradle 编译 Unitylib.aar. 
找不到就 命令行编译`./gradlew :unitylib:assembleRelease `
再拷贝Unitylib aar 到 unity 项目

    cp ～/AndroidStudioProjects/AndroidAAR/unitylib/build/outputs/aar/unitylib-release.aar ~/Project/UnityProject/Assets/Plugins/Android/unitylib-release.aar

另外 paylib 里面添加的aar 也拷贝到 Assets/Plugins/Android/

简而言之 Unitylib包含了 android 调用 其他lib 的逻辑， 但是不包含 其他lib 的aar 文件， 写的方法跟 原生开发比较像。


然后 在unity 那边 建一个gameobject, 名字叫BootScope （这个是自定义的写在Unity2Android.java 里面）, 添加脚本需要包含 Unity2Android.java设定的回调方法名， 这部分跟https://www.jianshu.com/p/86b275da600e 是一样的

## Project Structure

The project consists of multiple modules:
- `app`: Main Android application module
- `unitylib`: Unity integration library module
- `paylib`: Payment functionality library module

## Requirements

- Android Studio Arctic Fox (2021.3.1) or newer
- Gradle 8.1.0
- Android SDK 21 or higher
- JDK 17 or higher

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Build the project using `./gradlew build`

## Modules Description

### Unity Library (unitylib)
A library module that provides integration between Unity and Android native functionality. It includes custom Activity and Application classes for Unity integration.

### Payment Library (paylib)
A payment integration module that includes WeChat SDK integration (version 6.8.0) for handling payments.

### App
A sample Android application demonstrating the usage of the libraries.

## Building AAR Files

To build AAR files for each library module:

```bash
./gradlew :unitylib:assembleRelease
./gradlew :paylib:assembleRelease
```

The generated AAR files will be located in:
- `unitylib/build/outputs/aar/`
- `paylib/build/outputs/aar/`

## Usage in Unity

1. Copy the generated AAR files to your Unity project's `Assets/Plugins/Android` folder
2. Configure your Unity project's Android settings
3. Add necessary permissions and activities in your Android Manifest

## License

[Your License Here]

## Contact

For any inquiries, please contact [Your Contact Information] 