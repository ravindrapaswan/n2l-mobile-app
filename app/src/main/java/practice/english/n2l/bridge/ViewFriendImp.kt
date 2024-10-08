package practice.english.n2l.bridge

import practice.english.n2l.database.bao.FriendList


interface ViewFriendImp {
    fun onUnFriend(friendList: FriendList)
}