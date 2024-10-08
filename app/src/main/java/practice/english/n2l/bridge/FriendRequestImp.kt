package practice.english.n2l.bridge

import practice.english.n2l.database.bao.FriendRequests


interface FriendRequestImp {
    fun onAccept(friendRequests: FriendRequests)
    fun onReject(friendRequests: FriendRequests)
}