package practice.english.n2l.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import practice.english.n2l.bridge.MyVideosImp
import practice.english.n2l.database.AppDatabase
import practice.english.n2l.database.bao.MyVideos
import practice.english.n2l.databinding.ItemMyVideoBinding
import practice.english.n2l.util.GenericDiffUtil

class MyVideoAdapter (contexts:Context, private val myVideosImp: MyVideosImp): RecyclerView.Adapter<MyVideoAdapter.RecyclerViewViewHolder>() {

    private var appDatabase  = AppDatabase.getInstance(contexts)
    private var myVideosList   : MutableList<MyVideos> = mutableListOf()
    class RecyclerViewViewHolder(val itemBinding: ItemMyVideoBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(myVideos: MyVideos, myVideosImp: MyVideosImp) {
            itemBinding.apply {
                myVideosVar = myVideos

                if(myVideos.uploadStatus == "N") {
                    ImgUpload.visibility = View.VISIBLE
                    ImgDownload.visibility = View.INVISIBLE
                    ImgShare.visibility = View.INVISIBLE
                    ImgDelete.visibility = View.INVISIBLE
                    ImgPlay.visibility = View.INVISIBLE
                }
                else if(myVideos.uploadStatus == "Y" && myVideos.practiceLocalFilePath!=null) {
                    ImgUpload.visibility = View.INVISIBLE
                    ImgDownload.visibility = View.INVISIBLE
                    ImgShare.visibility = View.VISIBLE
                    ImgDelete.visibility = View.VISIBLE
                    ImgPlay.visibility = View.VISIBLE
                }
                else if(myVideos.uploadStatus == "Y") {
                    ImgDelete.visibility = View.INVISIBLE
                    ImgPlay.visibility = View.INVISIBLE
                    ImgUpload.visibility = View.INVISIBLE
                    ImgDownload.visibility = View.VISIBLE
                    ImgShare.visibility = View.INVISIBLE
                }
                ImgPlay.setOnClickListener {
                    myVideosImp.onPlay(myVideos)
                }
                ImgDelete.setOnClickListener {
                    myVideosImp.onDelete(myVideos)
                }
                ImgUpload.setOnClickListener {
                    myVideosImp.onUpload(myVideos)
                }
                ImgDownload.setOnClickListener {
                    myVideosImp.onDownload(myVideos)
                }
                ImgShare.setOnClickListener {
                    myVideosImp.onShare(myVideos)
                }
                executePendingBindings()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        val itemBinding: ItemMyVideoBinding = ItemMyVideoBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewViewHolder(itemBinding)
    }
    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        val myVideos = myVideosList[position]
        val pos=position+1
        holder.itemBinding.ImgPlay.tag=position
        holder.itemBinding.ImgDelete.tag=position
        holder.itemBinding.ImgShare.tag=position
        holder.itemBinding.TvQuizId.text=pos.toString()
        holder.bind(myVideos,myVideosImp)
    }

    private suspend fun getMyVideosForDb(myVideos:MyVideos):MyVideos {
            return withContext(Dispatchers.IO) {
                appDatabase.myVideosDao().getMyVideos(myVideos.practicesNo!!)
            }
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
        val index = myVideosList.indexOfFirst { it.practicesNo == updatedItem.practicesNo }
        if (index != -1) {
            myVideosList[index] = updatedItem
            updateList(myVideosList)
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