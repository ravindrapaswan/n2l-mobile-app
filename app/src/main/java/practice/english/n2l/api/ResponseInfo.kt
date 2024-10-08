package practice.english.n2l.api

import com.google.gson.annotations.SerializedName


data class ResponseInfo(
    @SerializedName("ResponseCode")
    val responseCode: Int,
    @SerializedName("ResponseMsg")
    val responseMessage: String,
    @SerializedName("ResponseException")
    val responseException: String,
    @SerializedName("data")
    val responseData: ResponseData
)
