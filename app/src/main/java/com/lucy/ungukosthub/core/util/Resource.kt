package com.lucy.ungukosthub.core.util

/**
 * Generic class yang membungkus data dengan statusnya (Success, Error, Loading).
 * Digunakan untuk mentransfer state data dari Repository -> UseCase -> ViewModel.
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
