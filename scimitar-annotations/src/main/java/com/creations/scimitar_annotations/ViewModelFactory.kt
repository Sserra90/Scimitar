package com.creations.scimitar_annotations

/**
 * Indicates the ViewModelFactory to use when binding view models
 * <pre>`
 * @ViewModelFactory ViewModelFactory vmFactory;
`</pre> *
 */
@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ViewModelFactory(val useAsDefault: Boolean = false)
