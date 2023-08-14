package com.infras.dauthsdk.api.annotation

@MustBeDocumented
@Retention(value = AnnotationRetention.BINARY)
@RequiresOptIn(
    level = RequiresOptIn.Level.WARNING,
    message = "这是一个试验性质的API，使用可能带来稳定性问题。"
)
annotation class DAuthExperimentalApi