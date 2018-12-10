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
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

    }

    fun bind(target: androidx.fragment.app.Fragment) {
        try {
            val c = Class.forName(target.javaClass.canonicalName!! + SCIMITAR_SUFFIX)
            c.getDeclaredConstructor(target.javaClass).newInstance(target)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        } catch (e: InstantiationException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        }

    }

}
