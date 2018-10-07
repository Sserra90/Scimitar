package com.creations.scimitar_runtime;

import java.lang.reflect.InvocationTargetException;

import androidx.fragment.app.FragmentActivity;

/**
 * @author Sérgio Serra on 22/09/2018.
 * Criations
 * sergioserra99@gmail.com
 */
public class Scimitar {

    private static final String SCIMITAR_SUFFIX = "$$Scimitar";

    public static void bind(FragmentActivity target) {
        try {
            Class<?> c = Class.forName(target.getClass().getCanonicalName() + SCIMITAR_SUFFIX);
            c.getDeclaredConstructor(target.getClass()).newInstance(target);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void bind(androidx.fragment.app.Fragment target) {
        try {
            Class<?> c = Class.forName(target.getClass().getCanonicalName() + SCIMITAR_SUFFIX);
            c.getDeclaredConstructor(target.getClass()).newInstance(target);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
