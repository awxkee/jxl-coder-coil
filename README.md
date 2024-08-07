# JPEG XL (JXL) coil plugin for Android 21+

The Coil JXL Plugin is a powerful and efficient library that allows you to seamlessly decode JPEG XL (JXL) images within your Android application using Coil, a popular image loading library. With this plugin, you can enhance your app's image loading capabilities and provide a superior user experience by effortlessly handling JXL images.

# Usage example

Just add to image loader heif decoder factory and use it as image loader in coil

```kotlin
val imageLoader = ImageLoader.Builder(context)
    .components {
        add(JxlDecoder.Factory())
    }
    .build()

// If you would like to support an animated JPEG XL files

val imageLoader = ImageLoader.Builder(context)
    .components {
        add(AnimatedJxlDecoder.Factory())
    }
    .build()
```

# Add repository

```groovy
implementation 'io.github.awxkee:jxl-coder-coil:2.2.0' // or any version above picker from release tags
```

### ~~Add Jitpack repository~~ Since 2.2.0 deprecated

```groovy
repositories {
    maven { url "https://jitpack.io" }
}
```

```groovy
implementation 'com.github.awxkee:jxl-coder-coil:2.1.9' // or any version above picker from release tags
```

# Disclaimer

Enhance your Android app's image loading capabilities with the Coil JXL Plugin and provide your users with a smoother experience when handling JPEG XL (JXL) images. If you have any questions or need assistance, please refer to the GitHub repository for more information and support.
