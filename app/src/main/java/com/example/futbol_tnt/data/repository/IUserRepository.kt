package com.example.futbol_tnt.data.repository

import com.example.futbol_tnt.data.model.User

interface IUserRepository {
    suspend fun syncUser(user: User)
    suspend fun getUser(uid: String): User?
    suspend fun updateUser(user: User)
}
