package com.promtuz.core

import android.content.Context

object API {
    init {
        System.loadLibrary("core")
    }

    external fun initApi(context: Context)

    external fun resolve(context: Context)
}