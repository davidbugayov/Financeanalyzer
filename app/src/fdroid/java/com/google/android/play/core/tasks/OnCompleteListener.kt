package com.google.android.play.core.tasks

/**
 * Заглушка для OnCompleteListener
 */
interface OnCompleteListener<TResult> {
    fun onComplete(task: Task<TResult>)
}
