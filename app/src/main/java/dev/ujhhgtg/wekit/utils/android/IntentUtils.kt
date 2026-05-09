package dev.ujhhgtg.wekit.utils.android

import android.content.Intent

inline fun Intent(block: Intent.() -> Unit): Intent = Intent().apply(block)
