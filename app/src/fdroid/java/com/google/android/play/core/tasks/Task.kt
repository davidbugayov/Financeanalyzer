package com.google.android.play.core.tasks

import java.util.concurrent.Executor

/**
 * Заглушка для Task
 */
interface Task<TResult> {
    fun isComplete(): Boolean

    fun isSuccessful(): Boolean

    fun isCanceled(): Boolean

    fun getResult(): TResult?

    fun getResult(p0: Class<out RuntimeException>?): TResult?

    fun getException(): Exception?

    fun addOnSuccessListener(p0: OnSuccessListener<in TResult>): Task<TResult>

    fun addOnSuccessListener(
        p0: Any,
        p1: OnSuccessListener<in TResult>,
    ): Task<TResult>

    fun addOnSuccessListener(
        p0: Executor,
        p1: OnSuccessListener<in TResult>,
    ): Task<TResult>

    fun addOnFailureListener(p0: OnFailureListener): Task<TResult>

    fun addOnFailureListener(
        p0: Any,
        p1: OnFailureListener,
    ): Task<TResult>

    fun addOnFailureListener(
        p0: Executor,
        p1: OnFailureListener,
    ): Task<TResult>

    fun addOnCompleteListener(p0: OnCompleteListener<TResult>): Task<TResult>

    fun addOnCompleteListener(
        p0: Any,
        p1: OnCompleteListener<TResult>,
    ): Task<TResult>

    fun addOnCompleteListener(
        p0: Executor,
        p1: OnCompleteListener<TResult>,
    ): Task<TResult>
}
