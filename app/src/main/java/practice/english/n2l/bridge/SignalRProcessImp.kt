package practice.english.n2l.bridge

import practice.english.n2l.database.bao.Users

interface SignalRProcessImp {
    fun receiveMessageToAllUsers(users:List<Users>)
}