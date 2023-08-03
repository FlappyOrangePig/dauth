package com.infras.dauthsdk.login.utils

import com.infras.dauthsdk.wallet.impl.ConfigurationManager
import java.security.MessageDigest
import java.util.Arrays
import java.util.Locale

object SignUtils {
    private const val SECURITY_KEY = "security_key"

    private val securityKey get() = ConfigurationManager.stage().signSecurityKey

    //对参数升序排序
    fun sign(data: MutableMap<String, String>): String {
        data[SECURITY_KEY] = securityKey
        val keySet: Set<String> = data.keys
        val keyArray = keySet.toTypedArray()
        Arrays.sort(keyArray)
        val sb = StringBuilder()
        for (i in keyArray.indices) {
            if ("sign" == keyArray[i]) {
                continue
            }
            // 参数值为空，则不参与签名
            val v = data[keyArray[i]]
            if (v != null) {
                sb.append(keyArray[i]).append("=").append(v)
                if (i < keyArray.size - 1) {
                    sb.append("&")
                }
            }
        }
        val signParam = md5(sb.toString())
        data.remove(SECURITY_KEY)
        return signParam.orEmpty()
    }

    private fun md5(key: String): String? {
        val hexDigits = charArrayOf(
            '0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'A',
            'B',
            'C',
            'D',
            'E',
            'F'
        )
        return try {
            val btInput = key.toByteArray()
            // 获得MD5摘要算法的 MessageDigest 对象
            val mdInst = MessageDigest.getInstance("MD5")
            // 使用指定的字节更新摘要
            mdInst.update(btInput)
            // 获得密文
            val md = mdInst.digest()
            // 把密文转换成十六进制的字符串形式
            val j = md.size
            val str = CharArray(j * 2)
            var k = 0
            for (i in 0 until j) {
                val byte0 = md[i]
                str[k++] = hexDigits[byte0.toInt() ushr 4 and 0xf]
                str[k++] = hexDigits[byte0.toInt() and 0xf]
            }
            val upCaseStr = String(str)
            upCaseStr.lowercase(Locale.getDefault())
        } catch (e: Exception) {
            null
        }
    }

    fun objToMap(obj: Any): MutableMap<String, String> {
        val map: MutableMap<String, String> = HashMap()
        val fields = obj.javaClass.declaredFields // 获取f对象对应类中的所有属性域
        for (field in fields) {
            var varName = field.name
            varName = varName.lowercase(Locale.getDefault()) // 将key置为小写，默认为对象的属性
            try {
                val accessFlag = field.isAccessible // 获取原来的访问控制权限
                field.isAccessible = true // 修改访问控制权限
                val o = field[obj] // 获取在对象f中属性fields[i]对应的对象中的变量
                if (o != null) {
                    map[varName] = o.toString()
                }
                field.isAccessible = accessFlag // 恢复访问控制权限
            } catch (ex: IllegalArgumentException) {
                ex.printStackTrace()
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            }
        }
        return map
    }
}