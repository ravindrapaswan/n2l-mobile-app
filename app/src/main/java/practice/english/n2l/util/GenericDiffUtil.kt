package practice.english.n2l.util

import androidx.recyclerview.widget.DiffUtil


class GenericDiffUtil<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    private val areItemsTheSame: (T, T) -> Boolean,
    private val areContentsTheSame: (T, T) -> Boolean
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }
}

/*class GenericDiffUtil<T>(
    private val oldList: List<T>,
    private val newList: List<T>,
    private val itemCallback: ItemCallback<T>
) : DiffUtil.Callback() {

    interface ItemCallback<T> {
        fun areItemsTheSame(oldItem: T, newItem: T): Boolean
        fun areContentsTheSame(oldItem: T, newItem: T): Boolean
    }
    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return itemCallback.areItemsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return itemCallback.areContentsTheSame(oldList[oldItemPosition], newList[newItemPosition])
    }
    fun calculateDiff(): DiffUtil.DiffResult {
        return DiffUtil.calculateDiff(this)
    }

    fun dispatchUpdatesTo(adapter: RecyclerView.Adapter<*>) {
        calculateDiff().dispatchUpdatesTo(adapter)
    }
}*/