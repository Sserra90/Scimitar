package com.creations.scimitar_annotations.state

/**
 * Bind a field with a StateObserver
 * <pre>`
 * @ResourceObserver() StateObserver stateObs;
`</pre> *
 */
@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class ResourceObserver(val id: String = "")
