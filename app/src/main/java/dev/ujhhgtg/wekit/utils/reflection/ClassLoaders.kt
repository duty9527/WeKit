package dev.ujhhgtg.wekit.utils.reflection

import android.content.Context
import com.highcapable.kavaref.extension.ClassLoaderProvider

object ClassLoaders {

    inline val HOST: ClassLoader get() = ClassLoaderProvider.classLoader!!

    inline val MODULE: ClassLoader get() = ClassLoaders.javaClass.classLoader!!

    inline val BOOT: ClassLoader get() = Context::class.java.classLoader!!
}
