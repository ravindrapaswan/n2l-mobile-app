package practice.english.n2l.util

object Constants {

    private const val signalServerOfficeURL:String="http://10.132.47.202/EasyLearningGateway/hub"
    const val HubURL:String=signalServerOfficeURL
    private const val serverURL:String="https://n2lacademy.in/api/"
    //private const val serverURL:String="http://192.168.29.175:3000/"
    const val URL:String=serverURL
    const val ConnectTimeout :Long= 120000
    const val ReadTimeout :Long= 120000
    const val NetworkMessage  = "Please check your device's internet connection"
    const val ServerFailure   = "Sorry for the inconvenience"
    const val ServerDataNotFound   = "Data not found"
    const val Information: String="Information"
    const val Error: String="Error"
    enum class Status {
        PENDING,
        RUNNING,
        FINISHED
    }
}