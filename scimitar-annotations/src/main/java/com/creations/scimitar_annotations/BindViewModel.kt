package com.creations.scimitar_annotations

/**
 * Bind a field to the specified ViewModel class
 * <pre>`
 * @BindViewModel(SomeViewModel.class) BindViewModel vm;
 * @BindViewModel() BindViewModel vm;
`</pre> *
 */
@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class BindViewModel
