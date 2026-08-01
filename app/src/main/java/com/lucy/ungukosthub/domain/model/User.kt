package com.lucy.ungukosthub.domain.model

/**
 * Domain Model yang merepresentasikan Pengguna (User) terautentikasi.
 */
data class User(
    val uid: String,
    val email: String,
    val displayName: String = ""
)
