package com.keerthi77459.attendease.cloud

interface DataFetch {
    fun onSuccess(data: Array<String>)
    fun onFailure(errorMessage: String)
}