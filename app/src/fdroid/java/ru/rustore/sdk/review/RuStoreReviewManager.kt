package ru.rustore.sdk.review

import com.google.android.play.core.tasks.Task

/**
 * Заглушка для RuStoreReviewManager
 */
class RuStoreReviewManager {
    fun requestReviewFlow(): Task<ReviewInfo> {
        return NoOpTask()
    }

    fun launchReviewFlow(reviewInfo: ReviewInfo): Task<Void> {
        return NoOpVoidTask()
    }
}

/**
 * Заглушка для ReviewInfo
 */
class ReviewInfo

/**
 * Заглушка для Task<ReviewInfo>
 */
class NoOpTask<T> : Task<T> {
    override fun isComplete(): Boolean = true
    override fun isSuccessful(): Boolean = false
    override fun isCanceled(): Boolean = false
    override fun getResult(): T? = null
    override fun getResult(p0: Class<out RuntimeException>?): T? = null
    override fun getException(): Exception? = Exception("Not implemented in F-Droid flavor")

    override fun addOnSuccessListener(p0: com.google.android.play.core.tasks.OnSuccessListener<in T>): Task<T> = this

    override fun addOnSuccessListener(
        p0: Any,
        p1: com.google.android.play.core.tasks.OnSuccessListener<in T>,
    ): Task<T> = this

    override fun addOnSuccessListener(
        p0: java.util.concurrent.Executor,
        p1: com.google.android.play.core.tasks.OnSuccessListener<in T>,
    ): Task<T> = this

    override fun addOnFailureListener(p0: com.google.android.play.core.tasks.OnFailureListener): Task<T> = this

    override fun addOnFailureListener(p0: Any, p1: com.google.android.play.core.tasks.OnFailureListener): Task<T> = this

    override fun addOnFailureListener(
        p0: java.util.concurrent.Executor,
        p1: com.google.android.play.core.tasks.OnFailureListener,
    ): Task<T> = this

    override fun addOnCompleteListener(p0: com.google.android.play.core.tasks.OnCompleteListener<T>): Task<T> = this

    override fun addOnCompleteListener(p0: Any, p1: com.google.android.play.core.tasks.OnCompleteListener<T>): Task<T> =
        this

    override fun addOnCompleteListener(
        p0: java.util.concurrent.Executor,
        p1: com.google.android.play.core.tasks.OnCompleteListener<T>,
    ): Task<T> = this
}

/**
 * Заглушка для Task<Void>
 */
class NoOpVoidTask : Task<Void> {
    override fun isComplete(): Boolean = true
    override fun isSuccessful(): Boolean = false
    override fun isCanceled(): Boolean = false
    override fun getResult(): Void? = null
    override fun getResult(p0: Class<out RuntimeException>?): Void? = null
    override fun getException(): Exception? = Exception("Not implemented in F-Droid flavor")

    override fun addOnSuccessListener(p0: com.google.android.play.core.tasks.OnSuccessListener<in Void>): Task<Void> =
        this

    override fun addOnSuccessListener(
        p0: Any,
        p1: com.google.android.play.core.tasks.OnSuccessListener<in Void>,
    ): Task<Void> = this

    override fun addOnSuccessListener(
        p0: java.util.concurrent.Executor,
        p1: com.google.android.play.core.tasks.OnSuccessListener<in Void>,
    ): Task<Void> = this

    override fun addOnFailureListener(p0: com.google.android.play.core.tasks.OnFailureListener): Task<Void> = this

    override fun addOnFailureListener(p0: Any, p1: com.google.android.play.core.tasks.OnFailureListener): Task<Void> =
        this

    override fun addOnFailureListener(
        p0: java.util.concurrent.Executor,
        p1: com.google.android.play.core.tasks.OnFailureListener,
    ): Task<Void> = this

    override fun addOnCompleteListener(p0: com.google.android.play.core.tasks.OnCompleteListener<Void>): Task<Void> =
        this

    override fun addOnCompleteListener(
        p0: Any,
        p1: com.google.android.play.core.tasks.OnCompleteListener<Void>,
    ): Task<Void> = this

    override fun addOnCompleteListener(
        p0: java.util.concurrent.Executor,
        p1: com.google.android.play.core.tasks.OnCompleteListener<Void>,
    ): Task<Void> = this
}
