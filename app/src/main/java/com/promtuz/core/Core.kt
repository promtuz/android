package com.promtuz.core

class Core {
    companion object {
        init {
            System.loadLibrary("core")
        }
    }

    init {
        initLogger()
    }

    external fun initLogger()
}