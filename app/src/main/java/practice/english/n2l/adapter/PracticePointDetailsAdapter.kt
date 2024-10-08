package practice.english.n2l.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import practice.english.n2l.database.bao.PointDetails
import practice.english.n2l.databinding.ItemPointsDetailsBinding
import practice.english.n2l.util.GenericDiffUtil

class PracticePointDetailsAdapter(private val onItemClickListener: (PointDetails) -> Unit): RecyclerView.Adapter<PracticePointDetailsAdapter.RecyclerViewViewHolder>()  {
    private var pointDetailsList   : MutableList<PointDetails> = mutableListOf()
    class RecyclerViewViewHolder(private val itemBinding: ItemPointsDetailsBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(item: PointDetails,onItemClick: (PointDetails) -> Unit,position:Int) {
            itemBinding.pointDetailsItem = item
            val pos=position+1
            itemBinding.txtSNO.text=pos.toString()
            itemBinding.executePendingBindings()
            itemBinding.imgPlay.setOnClickListener {
                onItemClick.invoke(item)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewViewHolder {
        val itemBinding: ItemPointsDetailsBinding = ItemPointsDetailsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        /*itemBinding.root.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val clickedItem = pointDetailsList[position]
                onItemClickListener.invoke(clickedItem)
            }
        }*/

        return RecyclerViewViewHolder(itemBinding)
    }
    override fun onBindViewHolder(holder: RecyclerViewViewHolder, position: Int) {
        val pointDetails = pointDetailsList[position]
        holder.bind(pointDetails,onItemClickListener,position)
    }
    override fun getItemCount(): Int= pointDetailsList.size
    fun updateItems(newList: List<PointDetails>) {
        val diffCallback = GenericDiffUtil(pointDetailsList, newList, { old, new -> old == new }, { old, new -> old == new })
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        pointDetailsList.clear()
        pointDetailsList.addAll(newList)
        diffResult.dispatchUpdatesTo(this)

        /*val diffCallback = object : GenericDiffUtil.ItemCallback<PointDetails> {
            override fun areItemsTheSame(oldItem: PointDetails, newItem: PointDetails): Boolean {
                return oldItem == newItem
            }
            override fun areContentsTheSame(oldItem: PointDetails, newItem: PointDetails): Boolean {
                return oldItem == newItem
            }
        }
        val diffUtil = GenericDiffUtil(pointDetailsList, newList, diffCallback)
        val diffResult = diffUtil.calculateDiff()
        pointDetailsList = newList.toMutableList()
        diffResult.dispatchUpdatesTo(this)*/

        //val diffUtil = GenericDiffUtil(data, newList, diffCallback)
        //diffUtil.dispatchUpdatesTo(this)
        //pointDetailsList = newList

        //val diffCallback = PointDetailsItemsDiffCallback(pointDetailsList, newList)
        //val diffResult = DiffUtil.calculateDiff(diffCallback)
        //pointDetailsList = newList
        //diffResult.dispatchUpdatesTo(this)
    }
    fun addItem(item: PointDetails) {
        pointDetailsList.add(item)
        notifyItemInserted(pointDetailsList.size - 1)
    }
    fun removeItem(item: PointDetails) {
        val index = pointDetailsList.indexOf(item)
        if (index != -1) {
            pointDetailsList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
    fun updateItem(updatedItem: PointDetails) {
        val index = pointDetailsList.indexOfFirst { it == updatedItem }
        if (index != -1) {
            pointDetailsList[index] = updatedItem
            notifyItemChanged(index)
        }
    }
    /*class PointDetailsItemsDiffCallback(
        private val oldList: List<PointDetails>,
        private val newList: List<PointDetails>
    ) : DiffUtil.Callback() {
        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].assignmentId == newList[newItemPosition].assignmentId
        }
        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }*/
}
