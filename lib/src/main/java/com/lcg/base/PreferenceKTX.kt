package com.lcg.base

import android.content.Context
import android.content.SharedPreferences
import com.alibaba.fastjson.JSON

/**
 * SharedPreferences数据存储
 *
 * @author lei.chuguang Email:475825657@qq.com
 * @version 2.0
 * @since 2023/9/19 16:00
 */
object PreferenceKTX {
    @JvmStatic
    fun getSharedPreferences(): SharedPreferences {
        val context = Core.context!!
        return context.getSharedPreferences(context.packageName, Context.MODE_PRIVATE)
    }

    @JvmStatic
    fun getEdit(): SharedPreferences.Editor {
        return getSharedPreferences().edit()
    }

    @JvmStatic
    fun setString(key: String, value: String) {
        getEdit().putString(key, value).commit()
    }

    @JvmStatic
    fun setInt(key: String, value: Int) {
        getEdit().putInt(key, value).commit()
    }

    @JvmStatic
    fun setBoolean(key: String, value: Boolean) {
        getEdit().putBoolean(key, value).commit()
    }

    @JvmStatic
    fun setByte(key: String, value: ByteArray) {
        setString(key, value.contentToString())
    }

    @JvmStatic
    fun setShort(key: String, value: Short) {
        setString(key, value.toString())
    }

    @JvmStatic
    fun setLong(key: String, value: Long) {
        getEdit().putLong(key, value).commit()
    }

    @JvmStatic
    fun setFloat(key: String, value: Float) {
        getEdit().putFloat(key, value).commit()
    }

    @JvmStatic
    fun setDouble(key: String, value: Double) {
        setString(key, value.toString())
    }

    @JvmStatic
    fun getString(key: String, defaultValue: String = ""): String {
        return getSharedPreferences().getString(key, defaultValue)!!
    }

    @JvmStatic
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return getSharedPreferences().getInt(key, defaultValue)
    }

    @JvmStatic
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getSharedPreferences().getBoolean(key, defaultValue)
    }

    @JvmStatic
    fun getByte(key: String, defaultValue: Byte = 0): Byte {
        try {
            return getSharedPreferences().getInt(key, defaultValue.toInt()).toByte()
        } catch (_: Exception) {
        }
        return 0
    }

    @JvmStatic
    fun getShort(key: String, defaultValue: Short = 0): Short {
        try {
            return getString(key, "").toShort()
        } catch (_: Exception) {
        }
        return defaultValue
    }

    @JvmStatic
    fun getLong(key: String, defaultValue: Long = 0): Long {
        return getSharedPreferences().getLong(key, defaultValue)
    }

    @JvmStatic
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return getSharedPreferences().getFloat(key, defaultValue)
    }

    @JvmStatic
    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        try {
            return getString(key, "").toDouble()
        } catch (_: Exception) {
        }
        return defaultValue
    }

    @JvmStatic
    fun remove(key: String) {
        getEdit().remove(key).commit()
    }

    @JvmStatic
    fun clear() {
        getEdit().clear().commit()
    }

    @JvmStatic
    fun contains(key: String): Boolean {
        return getSharedPreferences().contains(key)
    }
}

/**
 * Allows editing of this preference instance with a call to [apply][SharedPreferences.Editor.apply]
 * or [commit][SharedPreferences.Editor.commit] to persist the changes.
 * Default behaviour is [apply][SharedPreferences.Editor.apply].
 * ```
 * preferenceEdit {
 *     putString("key", value)
 * }
 * ```
 * To [commit][SharedPreferences.Editor.commit] changes:
 * ```
 * preferenceEdit(commit = true) {
 *     putString("key", value)
 * }
 * ```
 */
inline fun preferenceEdit(commit: Boolean = false, action: SharedPreferences.Editor.() -> Unit) {
    val editor = PreferenceKTX.getEdit()
    action(editor)
    if (commit) {
        editor.commit()
    } else {
        editor.apply()
    }
}

/**直接保存bean对象*/
fun Any?.saveConfig() {
    if (this != null)
        PreferenceKTX.setString(this::class.java.name.replace(".", "_"), JSON.toJSONString(this))
}

/**获取bean对象,不支持二级泛型嵌套*/
inline fun <reified T> getConfig(): T? {
    val clazz = T::class.java
    val string = PreferenceKTX.getString(clazz.name.replace(".", "_"), "")
    return if (string == "") null else JSON.parseObject(string, clazz)
}

/**配置文件中是否包含该bean的存储*/
fun containsConfig(clazz: Class<*>): Boolean {
    return PreferenceKTX.getSharedPreferences().contains(clazz.name.replace(".", "_"))
}

fun removeConfig(clazz: Class<*>) {
    PreferenceKTX.getEdit().remove(clazz.name.replace(".", "_")).commit()
}