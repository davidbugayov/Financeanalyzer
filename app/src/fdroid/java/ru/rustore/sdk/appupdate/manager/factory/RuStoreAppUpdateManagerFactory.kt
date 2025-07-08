package ru.rustore.sdk.appupdate.manager.factory

import android.content.Context
import com.google.android.play.core.tasks.Task
import ru.rustore.sdk.appupdate.model.AppUpdateInfo

/**
 * Заглушка для RuStoreAppUpdateManagerFactory
 */
object RuStoreAppUpdateManagerFactory {
    fun create(context: Context): RuStoreAppUpdateManager {
        return RuStoreAppUpdateManager()
    }
}

/**
 * Заглушка для RuStoreAppUpdateManager
 */
class RuStoreAppUpdateManager {
    fun getAppUpdateInfo(): Task<AppUpdateInfo> {
        return NoOpAppUpdateInfoTask()
    }
}

/**
 * Заглушка для AppUpdateInfo
 */
class AppUpdateInfo {
    fun updateAvailable(): Boolean = false
}

/**
 * Заглушка для Task<AppUpdateInfo>
 */
class NoOpAppUpdateInfoTask : Task<AppUpdateInfo> {
    override fun isComplete(): Boolean = true

    override fun isSuccessful(): Boolean = false

    override fun isCanceled(): Boolean = false

    override fun getResult(): AppUpdateInfo? = null

    override fun getResult(p0: Class<out RuntimeException>?): AppUpdateInfo? = null

    override fun getException(): Exception? = Exception("Not implemented in F-Droid flavor")

    override fun addOnSuccessListener(
        p0: com.google.android.play.core.tasks.OnSuccessListener<in AppUpdateInfo>,
    ): Task<AppUpdateInfo> = this

    override fun addOnSuccessListener(
        p0: Any,
        p1: com.google.android.play.core.tasks.OnSuccessListener<in AppUpdateInfo>,
    ): Task<AppUpdateInfo> = this

    override fun addOnSuccessListener(
        p0: java.util.concurrent.Executor,
        p1: com.google.android.play.core.tasks.OnSuccessListener<in AppUpdateInfo>,
    ): Task<AppUpdateInfo> = this

    override fun addOnFailureListener(p0: com.google.android.play.core.tasks.OnFailureListener): Task<AppUpdateInfo> =
        this

    override fun addOnFailureListener(
        p0: Any,
        p1: com.google.android.play.core.tasks.OnFailureListener,
    ): Task<AppUpdateInfo> = this

    override fun addOnFailureListener(
        p0: java.util.concurrent.Executor,
        p1: com.google.android.play.core.tasks.OnFailureListener,
    ): Task<AppUpdateInfo> = this

    override fun addOnCompleteListener(
        p0: com.google.android.play.core.tasks.OnCompleteListener<AppUpdateInfo>,
    ): Task<AppUpdateInfo> = this

    override fun addOnCompleteListener(
        p0: Any,
        p1: com.google.android.play.core.tasks.OnCompleteListener<AppUpdateInfo>,
    ): Task<AppUpdateInfo> = this

    override fun addOnCompleteListener(
        p0: java.util.concurrent.Executor,
        p1: com.google.android.play.core.tasks.OnCompleteListener<AppUpdateInfo>,
    ): Task<AppUpdateInfo> = this
}
