package com.creations.annotations


@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention
annotation class OnError(val id: String = "")

@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention
annotation class OnLoading(val id: String = "")

@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention
annotation class OnSuccess(val id: String = "")

@Target(AnnotationTarget.FUNCTION)
@kotlin.annotation.Retention
annotation class OnNoResults(val id: String = "")

@Target(AnnotationTarget.FIELD)
@kotlin.annotation.Retention
annotation class ResourceObserver(val id: String = "")
