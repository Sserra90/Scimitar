package com.creations.scimitar_runtime

import java.lang.reflect.InvocationTargetException

import androidx.fragment.app.FragmentActivity

/**
 * @author Sérgio Serra.
 * sergioserra99@gmail.com
 */
object Scimitar {

    private const val SCIMITAR_SUFFIX = "$\$Scimitar"

    fun bind(target: FragmentActivity) {
        try {
            val c = Class.forName(target.javaClass.canonicalName!! + SCIMITAR_SUFFIX)
            c.getDeclaredConstructor(target.javaClass).newInstance(target)
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
