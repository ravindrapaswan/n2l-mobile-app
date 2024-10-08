package practice.english.n2l.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import practice.english.n2l.bridge.FriendRequestImp
import practice.english.n2l.database.bao.FriendRequests
import practice.english.n2l.databinding.ItemFriendRequestBinding
import practice.english.n2l.util.GenericDiffUtil

class FriendRequestAdapter(private val friendRequestImp: FriendRequestImp): RecyclerView.Adapter<FriendRequestAdapter.RecyclerViewViewHolder>() {
    private var friendRequestsList   : MutableList<FriendRequests> = mutableListOf()
    class RecyclerViewViewHolder(val itemBinding: ItemFriendRequestBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(friendRequests: FriendRequests, viewFriendRequestImp: FriendRequestImp) {
            itemBinding.apply {
                friendRequestsVar = friendRequests
                BtnAccept.setOnClickListener {
                    viewFriendRequestImp.onAccept(friendRequests)
                }
                BtnReject.setOnClickListener {
                    viewFriendRequestImp.onReject(friendRequests)
                }
                executePendingBindings()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        val itemBinding: ItemFriendRequestBinding = ItemFriendRequestBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewViewHolder(itemBinding)
    }
    override fun getItemCount(): Int = friendRequestsList.size
    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        val friendRequests = friendRequestsList[position]
        val pos=position+1
        holder.itemBinding.TvQuizId.text=pos.toString()
        holder.bind(friendRequests,friendRequestImp)
    }
    fun removeItem(item: FriendRequests) {
        val index = friendRequestsList.indexOf(item)
        if (index != -1) {
            friendRequestsList.removeAt(index)
            //notifyItemRangeRemoved(0, getItemCount())
            notifyDataSetChanged()
        }
    }
    fun removeAllItems() {
        friendRequestsList.clear()
        notifyDataSetChanged()
    }
    fun updateList(newList: List<FriendRequests>) {
        val diffCallback = GenericDiffUtil(friendRequestsList, newList, { old, new -> old == new }, { old, new -> old == new })
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        friendRequestsList.clear()
        friendRequestsList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}