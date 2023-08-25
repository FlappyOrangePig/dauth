package com.infras.dauth.entity

sealed class PersonalInfoEntity {
    abstract fun getText(): String
    abstract fun getClipInfo(): String?
    class CopyableText(
        val title: String,
        val content: String
    ) : PersonalInfoEntity() {
        override fun getText(): String {
            return "$title:$content"
        }

        override fun getClipInfo(): String? {
            return content
        }
    }
}
