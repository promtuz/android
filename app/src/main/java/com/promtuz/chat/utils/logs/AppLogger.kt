package com.promtuz.chat.utils.logs

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.util.Calendar

data class AppLog(
    val time: Long,
    val priority: Int,
    val tag: String?,
    val message: String,
    val t: Throwable?
)

class AppLogger : Timber.Tree() {
    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?
    ) {
        val time = Calendar.getInstance().timeInMillis

        val cleanMessage = if (t != null) message.substringBefore('\n') else message

        _logs.update { listOf(AppLog(time, priority, tag, cleanMessage, t)) + it }
    }

    companion object {
        private var _logs = MutableStateFlow(emptyList<AppLog>())
        val logs = _logs.asStateFlow()
    }
}