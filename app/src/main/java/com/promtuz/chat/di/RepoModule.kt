package com.promtuz.chat.di

import com.promtuz.chat.data.repository.UserRepository
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val repoModule = module {
    singleOf(::UserRepository)
}