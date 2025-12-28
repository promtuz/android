package com.promtuz.chat.data.repository

import com.promtuz.chat.data.local.dao.UserDao
import com.promtuz.chat.data.local.entities.User
import com.promtuz.chat.security.KeyManager
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val users: UserDao, private val keyManager: KeyManager
) {
    fun fetchAll(nickname: String = ""): Flow<List<User>> {
        return if (nickname.isEmpty()) users.getAll() else users.getAll(nickname)
    }
}