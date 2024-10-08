package practice.english.n2l.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TransportEnum
import com.microsoft.signalr.messagepack.MessagePackHubProtocol
import practice.english.n2l.bridge.SignalRProcessImp
import practice.english.n2l.database.bao.Users


class SignalRClient(
    userName: String?,
    private val signalRProcessImp: SignalRProcessImp,
){
    private var hubConnection: HubConnection? = null

    init {
        hubConnection= HubConnectionBuilder.create(Constants.HubURL)
            .withHeader("username",userName)
            .withTransport(TransportEnum.ALL)
            .withHubProtocol(MessagePackHubProtocol())
            .build()
    }

    companion object {
        @Volatile
        private var INSTANCE: SignalRClient? = null
        fun getInstance(userName: String?,signalRProcessImp: SignalRProcessImp): SignalRClient {
            if (INSTANCE != null) return INSTANCE!!
            synchronized(this) {
                INSTANCE = SignalRClient(userName,signalRProcessImp)
                return INSTANCE!!
            }
        }
    }

    fun receiveMessageToAllUsers() {
        hubConnection!!.on("ReceiveMessageToAllUsers", { response ->
            val gson = Gson()
            val type = object : TypeToken<List<Users>>() {}.type
            val users=gson.fromJson<List<Users>>(response,type)
            signalRProcessImp.receiveMessageToAllUsers(users)
        }, String::class.java)
    }
    fun start() {
        if(hubConnection!!.connectionState== HubConnectionState.DISCONNECTED)
            hubConnection!!.start().blockingAwait()
    }
    fun stop() {
        if(hubConnection!!.connectionState== HubConnectionState.CONNECTED)
            hubConnection!!.stop().blockingAwait();
    }
}



 /*hubConnection= HubConnectionBuilder.create("http://192.168.29.175:5065/hub")
                .withHeader("username",params[0])
                .withTransport(TransportEnum.ALL)
                //.withHubProtocol(MessagePackHubProtocol())
                .build()
            if(hubConnection!!.connectionState==HubConnectionState.DISCONNECTED)
                hubConnection!!.start().blockingAwait()

            /*val customClassType = object : TypeReference<List<Users>>() {}.type
            hubConnection!!.on<List<Users>>("SendMessageToAllUsers", { userList ->
                // runOnUiThread {
                Log.e("Receive123**** ", userList[0].username)
                //}
            }, customClassType)*/

            hubConnection!!.on("SendMessageToAllUsers", { response ->
                // runOnUiThread {
                val gson = Gson()
                val type = object : TypeToken<List<Users>>() {}.type
                val users=gson.fromJson<List<Users>>(response,type)
                Log.e("Receive123**** ", users[0].username)
                //}
            }, String::class.java)*/


/*hubConnection.on(
            "SendMessageToAllUsers",
            { user: String, message: String ->  // OK
                Log.d("sendMessageToAllUsers", "$user: $message")
            }
            String::class.java,
            String::class.java
        )*/