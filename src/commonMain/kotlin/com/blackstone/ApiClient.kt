package com.blackstone


import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json


interface NetworkReachability {
    val isNetworkAvailable: Boolean
    val isNetworkMetered: Boolean
}



class RequestBuilder {
    val requestPerms = mutableListOf<Pair<String, Any?>>()
    var body: Any? = null
}

fun RequestBuilder.parameter(key: String, value: Any?) = requestPerms.add(Pair(key, value))



class ApiClient(val client: HttpClient,
                val networkReachability: NetworkReachability,
                val baseUrl: String,
                val sessionId: String?){

    suspend inline fun <reified T> get(route: String,
                                           block: RequestBuilder.() -> Unit = {}): Result<T> {
        if (networkReachability.isNetworkAvailable) {
            return try {
                val options = RequestBuilder().apply(block)
                val response = client.get<T>("$baseUrl$route") {
                    headers {
                        sessionId?.also {
                            append("X-API-KEY", it)
                        }
                    }
                    if (options.requestPerms.isNotEmpty()) {
                        options.requestPerms.forEach {
                            // this sets the URL query parameters
                            this.parameter(it.first, it.second)
                        }
                    }
                }
                Result.success(response)
            } catch (e: Exception) {
                return handleErrorNew(e)
            }
        } else {
            return Result.failure(Exception("no network connection"))
        }
    }



    suspend fun <T> handleErrorNew(exception: Exception): Result<T> {
        // todo I want to have an extensive logging system in here.
        return if (exception is ResponseException) {
            // for when our errors have json data to them
            when (exception.response.status.value) {
                in 400..499 -> Result.failure(Exception(""))

                else ->  Result.failure(Exception(""))
            }
        } else {
            // todo this is likely where we are handling things like network timeout and no connection errors.
            // todo log this here, because it's libely that this could be a serialization exception too.
//            print(exception.message)
//            Either.Left(UnknownError(500))
            // todo down the road handle this... but for now I want the crash info
            throw exception
        }
    }
}