package practice.english.n2l.api

sealed class ResponseHandle<T>(val data: T?=null, val errorMessage:String?=null)
{
    class Loading<T>: ResponseHandle<T>()
    class Network<T>: ResponseHandle<T>()
    class Success<T>(data: T?=null): ResponseHandle<T>(data=data)
    class Error<T>(errorMessage: String): ResponseHandle<T>(errorMessage=errorMessage)
}