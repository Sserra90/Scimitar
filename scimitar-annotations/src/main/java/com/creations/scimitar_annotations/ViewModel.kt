package com.creations.scimitar_annotations

/**
 * Bind a field to the specified ViewModel class
 * <pre>`
 * @BindViewModel(SomeViewModel.class) BindViewModel vm;
 * @BindViewModel() BindViewModel vm;
`</pre> *
 */
@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention
annotation class BindViewModel

@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention
annotation class ViewModelFactory(val useAsDefault: Boolean = false)

