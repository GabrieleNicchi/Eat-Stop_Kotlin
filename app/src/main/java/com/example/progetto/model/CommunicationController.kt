package com.example.progetto.model

import android.net.Uri
import android.util.Log
import androidx.room.Index
import com.example.progetto.classes.ImageBase64
import com.example.progetto.classes.MenuDetail
import com.example.progetto.classes.MenuList
import com.example.progetto.classes.OrderInfo
import com.example.progetto.classes.OrderMenu
import com.example.progetto.classes.UserInfo
import com.example.progetto.classes.UserInfoPut
import com.example.progetto.classes.UserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object CommunicationController {
    private val BASE_URL = "https://develop.ewlab.di.unimi.it/mc/2425"
    private val TAG = CommunicationController::class.simpleName

    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    enum class HttpMethod {
        GET,
        POST,
        DELETE,
        PUT
    }

    //Generic network call
    suspend fun genericRequest(url: String, method: HttpMethod,
                               queryParameters: Map<String, Any?> = emptyMap(),
                               requestBody: Any? = null): HttpResponse {

        val urlUri = Uri.parse(url)
        val urlBuilder = urlUri.buildUpon()
        queryParameters.forEach { (key, value) ->
            urlBuilder.appendQueryParameter(key, value.toString())
        }
        val completeUrlString = urlBuilder.build().toString()
        Log.d(TAG, completeUrlString)

        val request: HttpRequestBuilder.() -> Unit = {
            requestBody?.let {
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }
        }

        return try {
            val result = when (method) {
                HttpMethod.GET -> client.get(completeUrlString, request)
                HttpMethod.POST -> client.post(completeUrlString, request)
                HttpMethod.DELETE -> client.delete(completeUrlString, request)
                HttpMethod.PUT -> client.put(completeUrlString, request)
            }
            if (result.status.value in 200..299) {
                result
            } else {
                throw Exception("HTTP error: ${result.status.value}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Request failed: ${e.message}")
            throw e
        }
    }


    /* ------------------------------------ GET ------------------------------------ */

    // Network call to get user info
    suspend fun getUserInfo(sid: String?, oid: Int?) : UserInfo? {
        //Log.d(TAG, "getUserInfo")

        val url = "$BASE_URL/user/$oid?"
        val queryParams = mapOf("sid" to sid)
        //Log.d(TAG, "url: $url+$queryParams")

        try{
            val httpResponse = genericRequest(url, HttpMethod.GET, queryParams)
            val result: UserInfo = httpResponse.body()
            return result
        } catch (e : Exception) {
            Log.d(TAG , "getUserInfo error: ${e.message}")
            return null
        }
    }

    // Network call to get the entire menu list in the area
    suspend fun getMenuList(lat : Double , lng : Double , sid : String) : List<MenuList> {

        Log.d(TAG , "getListMenu")

        val url = BASE_URL+"/menu?lat=$lat&lng=$lng&"
        val queryParams = mapOf("sid" to sid)
        Log.d(TAG, "url: $url+$queryParams")

        try{
            val httpResponse = genericRequest(url, HttpMethod.GET, queryParams)
            val result: List<MenuList> = httpResponse.body()
            return result
        } catch (e : Exception) {
            Log.d(TAG , "getListMenu error: ${e.message}")
            return emptyList()
        }
    }

    // Network call to get the image of a single menu
    suspend fun getMenuImage(mid : Int, sid : String) : ImageBase64 {
        Log.d(TAG, "getMenuImage")

        val url = BASE_URL+"/menu/$mid/image?"
        val queryParams = mapOf("sid" to sid)
        Log.d(TAG, "url: $url+$queryParams")

        val httpResponse = genericRequest(url, HttpMethod.GET, queryParams)
        val result: ImageBase64 = httpResponse.body()
        return result
    }

    // Network call to get the detail of a menu
    suspend fun getMenuDetail(mid: Int, lat: Double, lng: Double, sid: String) : MenuDetail? {
        Log.d(TAG, "getMenuDetail")

        val url = BASE_URL+"/menu/$mid?lat=$lat&lng=$lng"
        val queryParams = mapOf("sid" to sid)
        Log.d(TAG, "url: $url+$queryParams")

        try{
            val httpResponse = genericRequest(url, HttpMethod.GET, queryParams)
            val result: MenuDetail = httpResponse.body()
            return result
        } catch (e : Exception) {
            Log.d(TAG, "getMenuDetail error: ${e.message}")
            return null
        }
    }

    // Network call to get the detail of last user's order
    suspend fun getOrderInfo(oid: Int, sid: String) : OrderInfo? {
        Log.d(TAG, "getOrderInfo")

        val url = BASE_URL+"/order/$oid?"
        val queryParams = mapOf("sid" to sid)
        Log.d(TAG, "url: $url+$queryParams")

        try{
            val httpResponse = genericRequest(url, HttpMethod.GET, queryParams)
            val result: OrderInfo = httpResponse.body()
            return result
        } catch (e : Exception) {
            Log.d(TAG, "getOrderInfo error: ${e.message}")
            return null
        }
    }

    /* ------------------------------------ POST ------------------------------------ */

    // Network call to register a new user
    suspend fun postUser() : UserResponse? {
        //Log.d(TAG, "createUser")
        val url = BASE_URL+"/user"
        try{
            val httpResponse = genericRequest (url, HttpMethod.POST)
            val result : UserResponse = httpResponse.body()
            return result
        } catch (e : Exception) {
            Log.d(TAG , "postUser error: ${e.message}")
            return null
        }
    }

    // Network call to make a new order
    suspend fun postOrder(mid: Int, orderMenu: OrderMenu) : OrderInfo {
        //Log.d(TAG, "createOrder")
        val url = BASE_URL+"/menu/$mid/buy"
        val httpResponse = genericRequest (url, HttpMethod.POST, emptyMap(), orderMenu)
        val result : OrderInfo = httpResponse.body()
        return result
    }

    /* ------------------------------------ PUT ------------------------------------ */

    // Network call to put user info
    suspend fun putUserInfo(oid: Int, userInfo: UserInfoPut) {
        Log.d(TAG, "putUserInfo")

        val url = "$BASE_URL/user/$oid"
        Log.d(TAG, "url: $url")

        try{

            val httpResponse = genericRequest(url, HttpMethod.PUT, emptyMap(), userInfo)

        } catch (e : Exception) {
            Log.d(TAG , "putUserInfo error: ${e.message}")
        }

    }


}
