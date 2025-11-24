package com.promtuz.chat.di

import com.promtuz.chat.data.remote.QuicClient
import com.promtuz.chat.security.KeyManager
import com.promtuz.chat.utils.logs.AppLogger
import com.promtuz.chat.utils.media.ImageUtils
import com.promtuz.core.Core
import com.promtuz.core.Crypto
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val appModule = module {
    single { Core() }
    single { Crypto() }
    single { KeyManager(androidContext(), get()) }

    single { QuicClient(get(), get(), get()) }

    single { ImageUtils(get()) }
}