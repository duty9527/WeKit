package dev.ujhhgtg.wekit.hooks.api.core

import android.content.Context
import dev.ujhhgtg.wekit.constants.PackageNames
import dev.ujhhgtg.wekit.utils.RuntimeConfig
import dev.ujhhgtg.wekit.utils.android.Intent

object WeApi {

    private var _selfWxId: String = ""
    val selfWxId: String
        get() {
            if (_selfWxId.isEmpty()) {
                val result = RuntimeConfig.loggedInWxId
                if (result.isNotEmpty()) _selfWxId = result
                return result
            }
            return _selfWxId
        }

    private var _selfCustomWxId: String = ""
    val selfCustomWxId: String
        get() {
            if (_selfCustomWxId.isEmpty()) {
                val result = WeMessageApi.selfCustomWxId
                if (result.isNotEmpty()) _selfCustomWxId = result
                return result
            }
            return _selfCustomWxId
        }

    fun openContact(context: Context, wxId: String, dst: OpenContactDestination) {
        when (dst) {
            OpenContactDestination.HOMEPAGE -> {
                context.startActivity(Intent {
                    setClassName(context.packageName, "${PackageNames.WECHAT}.plugin.profile.ui.ContactInfoUI")
                    putExtra("Contact_User", wxId)
                })
            }

            OpenContactDestination.SETTINGS -> {
                context.startActivity(Intent {
                    setClassName(context.packageName, "${PackageNames.WECHAT}.plugin.profile.ui.ProfileSettingUI")
                    putExtra("Contact_User", wxId)
                })
            }

            OpenContactDestination.CONVERSATION -> {
                context.startActivity(Intent {
                    setClassName(context.packageName, "${PackageNames.WECHAT}.ui.chatting.ChattingUI")
                    putExtra("Chat_User", wxId)
                })
            }
        }
    }

    enum class OpenContactDestination {
        HOMEPAGE,
        SETTINGS,
        CONVERSATION
    }
}
