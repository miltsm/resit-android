package my.miltsm.resit.data.model

sealed class Response<T>() {
    class Idle<T> : Response<T>()
    class Loading<T> : Response<T>()
    data class Success<T>(val data: T? = null) : Response<T>()
    class Fail<T> : Response<T>()
}