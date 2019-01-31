package com.creations.annotations

/**
 * Bind a field to the specified ViewModel class
 * <pre>`
 * @ViewModel(SomeViewModel.class) ViewModel vm;
 * @ViewModel() ViewModel vm;
`</pre> *
 */
@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention
annotation class ViewModel

@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention
annotation class ViewModelFactory(val useAsDefault: Boolean = false)

