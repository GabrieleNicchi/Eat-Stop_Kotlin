package com.example.progetto.classes

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/* ------------------------------------------ USER ------------------------------------------ */

// -> postUser
@Serializable
data class UserResponse(
    val sid: String,
    val uid: Int
)

// -> getUserInfo
@Serializable
data class UserInfo(
    val firstName : String?,
    val lastName : String?,
    val cardFullName : String?,
    val cardNumber : String?,
    val cardExpireMonth : Int?,
    val cardExpireYear : Int?,
    val cardCVV : String?,
    val uid : Int,
    val lastOid : Int?,
    val orderStatus : String?
)

// -> putUserInfo
@Serializable
data class UserInfoPut(
    val firstName : String?,
    val lastName : String?,
    val cardFullName : String?,
    val cardNumber : String?,
    val cardExpireMonth : Int?,
    val cardExpireYear : Int?,
    val cardCVV : String?,
    val sid : String?
)

/* ------------------------------------------ MENU ------------------------------------------ */

// -> getMenuImage
@Serializable
data class ImageBase64(
    val base64: String
)

// -> getListMenu
@Serializable
data class MenuList(
    val mid : Int,
    val name : String,
    val price : Double,
    val location : Location,
    val imageVersion : Int,
    val deliveryTime : Int,
    val shortDescription : String
)

// -> getMenuDetail
@Serializable
data class MenuDetail(
    val mid : Int,
    val name : String,
    val price : Double,
    val location : Location,
    val imageVersion : Int,
    val shortDescription: String,
    val deliveryTime: Int,
    val longDescription : String
)

/* ------------------------------------------ DB ------------------------------------------ */

// Testing
@Entity
data class ImageVersionInfo(
    @PrimaryKey val mid: Int,
    val version: Int
)

// -> DB table
@Entity
data class ImageVersion(
    @PrimaryKey val mid : Int,
    var version : Int,
    val image : String
)

/* --------------------------------------- ORDER --------------------------------------- */

@Serializable
data class OrderMenu(
    val sid: String,
    val deliveryLocation: Location
)

@Serializable
data class OrderInfo(
    val oid: Int,
    val mid: Int,
    val uid: Int,
    @SerialName("creationTimestamp") val creationTimeStamp: String,
    val status: String,
    val deliveryLocation: Location,
    @SerialName("deliveryTimestamp") val deliveryTimeStamp: String? = null, //if order is completed
    @SerialName("expectedDeliveryTimestamp") val expectedDeliveryTimeStamp: String? = null, //if order is not completed
    val currentPosition: Location
)

/* --------------------------------------- OTHER --------------------------------------- */

@Serializable
data class ResponseError(val message: String)

// 'location'
@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)