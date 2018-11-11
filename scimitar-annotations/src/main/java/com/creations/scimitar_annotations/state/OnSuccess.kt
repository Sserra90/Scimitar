package com.creations.scimitar_annotations.state

/**
 * Bind a field to the specified ViewModel class
 * <pre>`
 * @BindViewModel(SomeViewModel.class) BindViewModel vm;
 * @BindViewModel() BindViewModel vm;
`</pre> *
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class OnSuccess(val id: String = "")
