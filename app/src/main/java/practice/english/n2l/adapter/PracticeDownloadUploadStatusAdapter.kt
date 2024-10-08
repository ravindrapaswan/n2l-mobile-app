package practice.english.n2l.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import practice.english.n2l.bridge.PracticeDownloadUploadStatusImp
import practice.english.n2l.database.bao.AttemptPractice
import practice.english.n2l.databinding.ItemPracticeDownloadUploadStatusBinding
import practice.english.n2l.util.GenericDiffUtil

class PracticeDownloadUploadStatusAdapter(private val practiceDownloadUploadStatusImp: PracticeDownloadUploadStatusImp): RecyclerView.Adapter<PracticeDownloadUploadStatusAdapter.RecyclerViewViewHolder>() {
    private var attemptPracticeList   : MutableList<AttemptPractice> = mutableListOf()
    class RecyclerViewViewHolder(val itemBinding: ItemPracticeDownloadUploadStatusBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(attemptPractice: AttemptPractice,practiceDownloadUploadStatusImp: PracticeDownloadUploadStatusImp) {
            itemBinding.apply {
                attemptPracticeVar = attemptPractice
                ImgPlay.setOnClickListener {
                    practiceDownloadUploadStatusImp.onPlay(attemptPractice)
                }
               ImgDelete.setOnClickListener {
                    practiceDownloadUploadStatusImp.onDelete(attemptPractice)
                }
               ImgUpload.setOnClickListener {
                    practiceDownloadUploadStatusImp.onUpload(attemptPractice)
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
    override fun onBindViewHolder(holder:RecyclerViewViewHolder, position: Int) {
        val attemptPractice = attemptPracticeList[position]
        val pos=position+1
        holder.itemBinding.TvQuizId.text=pos.toString()
        holder.bind(attemptPractice,practiceDownloadUploadStatusImp)
    }
    override fun getItemCount(): Int = attemptPracticeList.size
    fun addItem(item: AttemptPractice) {
        attemptPracticeList.add(item)
        //notifyItemInserted(attemptPracticeList.size - 1)
        notifyDataSetChanged()
    }
    fun removeItem(item: AttemptPractice) {
        val index = attemptPracticeList.indexOf(item)
        if (index != -1) {
            attemptPracticeList.removeAt(index)
            //notifyItemRangeRemoved(0, getItemCount())
            notifyDataSetChanged()
        }
    }
    fun updateItem(updatedItem: AttemptPractice) {
        val index = attemptPracticeList.indexOfFirst { it.attemptPracticeId == updatedItem.attemptPracticeId }
        if (index != -1) {
            attemptPracticeList[index] = updatedItem
            notifyDataSetChanged()
        }
    }
    fun removeAllItems() {
        attemptPracticeList.clear()
        notifyDataSetChanged()
    }
    fun updateList(newList: List<AttemptPractice>) {
        /*val diffCallback = object : GenericDiffUtil.ItemCallback<AttemptPractice> {
            override fun areItemsTheSame(oldItem: AttemptPractice, newItem: AttemptPractice): Boolean {
                return oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: AttemptPractice, newItem: AttemptPractice): Boolean {
                return oldItem == newItem
            }
        }
        val diffUtil = GenericDiffUtil(attemptPracticeList, newList, diffCallback)
        val diffResult = diffUtil.calculateDiff()
        attemptPracticeList = newList.toMutableList()
        diffResult.dispatchUpdatesTo(this)*/

        val diffCallback = GenericDiffUtil(attemptPracticeList, newList, { old, new -> old == new }, { old, new -> old == new })
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        attemptPracticeList.clear()
        attemptPracticeList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)
    }
}

/*class PracticeDownloadUploadStatusAdapter(private val context: Activity,private val practiceDownloadUploadStatusImp: PracticeDownloadUploadStatusImp): RecyclerView.Adapter<PracticeDownloadUploadStatusAdapter.RecyclerViewViewHolder>() {

    private val attemptPracticeList   : MutableList<AttemptPractice> by lazy { mutableListOf() }
    class RecyclerViewViewHolder(itemBinding: ItemPracticeDownloadUploadStatusBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        val itemBinding: ItemPracticeDownloadUploadStatusBinding
        init { this.itemBinding = itemBinding }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        val itemBinding: ItemPracticeDownloadUploadStatusBinding = ItemPracticeDownloadUploadStatusBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerViewViewHolder(itemBinding)
    }
    override fun getItemCount(): Int { return attemptPracticeList.size }
    fun isEmpty(): Boolean { return attemptPracticeList.isEmpty() }
    fun getObject(position: Int): AttemptPractice { return attemptPracticeList[position] }
    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        val attemptPractice=attemptPracticeList[position]
        holder.itemBinding.ImgDelete.tag = position
        holder.itemBinding.ImgPlay.tag = position
        holder.itemBinding.ImgUpload.tag = position

        val p= position+1
        holder.itemBinding.TvQuizId.text=p.toString()
        holder.itemBinding.TvQuizName.text=attemptPractice.practiceName
        holder.itemBinding.TvQuizDate.text= TimestampConverter.convertDateToTime(attemptPractice.practiceDate)
        holder.itemBinding.ImgPlay.setOnClickListener {
            val pos= it.tag as Int
            practiceDownloadUploadStatusImp.onPlay(attemptPractice,pos)
        }
        holder.itemBinding.ImgDelete.setOnClickListener {
            val pos= it.tag as Int
            practiceDownloadUploadStatusImp.onDelete(attemptPractice,pos)
        }

        if(attemptPractice.uploadStatus == "N")
            holder.itemBinding.ImgUpload.visibility=View.VISIBLE
        else
            holder.itemBinding.ImgUpload.visibility=View.INVISIBLE

        holder.itemBinding.ImgUpload.setOnClickListener {
            val pos= it.tag as Int
            practiceDownloadUploadStatusImp.onUpload(attemptPractice,pos)
        }
    }
    fun add(models: List<AttemptPractice>) {
        if (models.isEmpty()) {
            return
        }
        attemptPracticeList.clear()
        attemptPracticeList.addAll(models)
        notifyDataSetChanged()
    }
    fun clear(isNotify: Boolean) {
        attemptPracticeList.clear()
        if (isNotify) {
            notifyDataSetChanged()
        }
    }
}*/
