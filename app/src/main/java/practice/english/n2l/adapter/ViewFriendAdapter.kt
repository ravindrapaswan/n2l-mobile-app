package practice.english.n2l.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import practice.english.n2l.bridge.ViewFriendImp
import practice.english.n2l.database.bao.FriendList
import practice.english.n2l.databinding.ItemViewFriendsBinding
import practice.english.n2l.util.GenericDiffUtil

class ViewFriendAdapter (private val viewFriendImp: ViewFriendImp): RecyclerView.Adapter<ViewFriendAdapter.RecyclerViewViewHolder>() {
    private var friendList   : MutableList<FriendList> = mutableListOf()
    class RecyclerViewViewHolder(val itemBinding: ItemViewFriendsBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(friend: FriendList, viewFriendImp: ViewFriendImp) {
            itemBinding.apply {
                friendListVar = friend
                BtnUnFriend.setOnClickListener {
                    viewFriendImp.onUnFriend(friend)
                }

                executePendingBindings()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        val itemBinding: ItemViewFriendsBinding = ItemViewFriendsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewViewHolder(itemBinding)
    }
    override fun getItemCount(): Int = friendList.size
    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        val friendRequests = friendList[position]
        val pos=position+1
        holder.itemBinding.TvQuizId.text=pos.toString()
        holder.bind(friendRequests,viewFriendImp)
    }
    fun removeItem(item: FriendList) {
        val index = friendList.indexOf(item)
        if (index != -1) {
            friendList.removeAt(index)
            //notifyItemRangeRemoved(0, getItemCount())
            notifyDataSetChanged()
        }
    }
    fun removeAllItems() {
        friendList.clear()
        notifyDataSetChanged()
    }
    fun updateList(newList: List<FriendList>) {
        val diffCallback = GenericDiffUtil(friendList, newList, { old, new -> old == new }, { old, new -> old == new })
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        friendList.clear()
        friendList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}