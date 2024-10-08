package practice.english.n2l.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import practice.english.n2l.bridge.MyVideosImp
import practice.english.n2l.database.bao.MyVideos
import practice.english.n2l.databinding.ItemPracticeDownloadUploadStatusBinding
import practice.english.n2l.util.GenericDiffUtil

class MyVideosAdapter (private val myVideosImp: MyVideosImp): RecyclerView.Adapter<MyVideosAdapter.RecyclerViewViewHolder>() {
    private var myVideosList   : MutableList<MyVideos> = mutableListOf()
    class RecyclerViewViewHolder(val itemBinding: ItemPracticeDownloadUploadStatusBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(myVideos: MyVideos, myVideosImp: MyVideosImp) {
            itemBinding.apply {
                //attemptPracticeVar = myVideos
                ImgPlay.setOnClickListener {
                    myVideosImp.onPlay(myVideos)
                }
                ImgDelete.setOnClickListener {
                    myVideosImp.onDelete(myVideos)
                }
                ImgUpload.setOnClickListener {
                    myVideosImp.onUpload(myVideos)
                }
                executePendingBindings()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        val itemBinding: ItemPracticeDownloadUploadStatusBinding = ItemPracticeDownloadUploadStatusBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewViewHolder(itemBinding)
    }
    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        val myVideos = myVideosList[position]
        val pos=position+1
        holder.itemBinding.TvQuizId.text=pos.toString()
        holder.bind(myVideos,myVideosImp)
    }
    override fun getItemCount(): Int = myVideosList.size
    fun addItem(item: MyVideos) {
        myVideosList.add(item)
        //notifyItemInserted(attemptPracticeList.size - 1)
        notifyDataSetChanged()
    }
    fun removeItem(item: MyVideos) {
        val index = myVideosList.indexOf(item)
        if (index != -1) {
            myVideosList.removeAt(index)
            //notifyItemRangeRemoved(0, getItemCount())
            notifyDataSetChanged()
        }
    }
    fun updateItem(updatedItem: MyVideos) {
        val index = myVideosList.indexOfFirst { it.quizId == updatedItem.quizId }
        if (index != -1) {
            myVideosList[index] = updatedItem
            notifyDataSetChanged()
        }
    }
    fun removeAllItems() {
        myVideosList.clear()
        notifyDataSetChanged()
    }
    fun updateList(newList: List<MyVideos>) {
        val diffCallback = GenericDiffUtil(myVideosList, newList, { old, new -> old == new }, { old, new -> old == new })
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        myVideosList.clear()
        myVideosList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}