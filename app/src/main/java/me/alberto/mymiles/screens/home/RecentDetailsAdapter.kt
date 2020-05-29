package me.alberto.mymiles.screens.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import me.alberto.mymiles.database.Miles
import me.alberto.mymiles.databinding.MileItemBinding


class RecentDetailsAdapter :
    ListAdapter<Miles, RecyclerView.ViewHolder>(DiffCallback()) {
    class DiffCallback : DiffUtil.ItemCallback<Miles>() {
        override fun areItemsTheSame(oldItem: Miles, newItem: Miles): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Miles, newItem: Miles): Boolean {
            return oldItem == newItem
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MilesItemHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val miles = getItem(position)
        when (holder) {
            is MilesItemHolder -> holder.bind(miles)
        }
    }


    class MilesItemHolder(private val binding: MileItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            miles: Miles
        ) {
            binding.miles = miles
        }

        companion object {
            fun from(parent: ViewGroup): RecyclerView.ViewHolder {
                val binding =
                    MileItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

                return MilesItemHolder(binding)
            }
        }
    }

}