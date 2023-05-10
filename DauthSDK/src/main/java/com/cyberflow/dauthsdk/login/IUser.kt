package com.cyberflow.dauthsdk.login

interface IUser {

    fun login()

    fun login(type: String)

    fun logout()

    fun loginByMobile()

    fun loginByEmail()

    fun loginByAccount()

}