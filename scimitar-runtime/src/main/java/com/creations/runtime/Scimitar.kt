package com.creations.runtime

import androidx.fragment.app.FragmentActivity
import java.lang.reflect.InvocationTargetException

fun FragmentActivity.scimitar() {
    Scimitar.bind(this)
}

fun androidx.fragment.app.Fragment.scimitar() {
    Scimitar.bind(this)
}

private object Scimitar {

    private const val SCIMITAR_SUFFIX = "$\$Scimitar"

    fun bind(target: FragmentActivity) {
        try {
            val c = Class.forName(target.javaClass.canonicalName!! + SCIMITAR_SUFFIX)
            c.getDeclaredConstructor(target.javaClass).newInstance(target)
            logd("Found class $c")
        } catch (e: ClassNotFoundException) {
            loge("Error: $e")
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            loge("Error: $e")
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            loge("Error: $e")
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            loge("Error: $e")
            e.printStackTrace()
        }

    }

    fun bind(target: androidx.fragment.app.Fragment) {
        try {
            val c = Class.forName(target.javaClass.canonicalName!! + SCIMITAR_SUFFIX)
            c.getDeclaredConstructor(target.javaClass).newInstance(target)
            logd("Found class $c")
        } catch (e: ClassNotFoundException) {
            loge("Error: $e")
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            loge("Error: $e")
            e.printStackTrace()
        } catch (e: InstantiationException) {
            loge("Error: $e")
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            loge("Error: $e")
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            loge("Error: $e")
            e.printStackTrace()
        }

    }

}
