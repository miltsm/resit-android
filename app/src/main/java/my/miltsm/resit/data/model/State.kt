package my.miltsm.resit.data.model

sealed class State<T>(open val data: T? = null) {
    class IdleState<T> : State<T>()
    class LoadingState<T> : State<T>()
    data class SuccessState<T>(override val data: T? = null) : State<T>()
    class FailedState<T> : State<T>()
}