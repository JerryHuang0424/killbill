package org.wit.killbill.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.wit.killbill.databinding.CardBinding
import org.wit.killbill.models.NotifyModel
import android.view.LayoutInflater



interface NotifyAdapterListener{
    fun onCardClick(notify: NotifyModel)
}

class NotifyAdapter(private var notifies: List<NotifyModel>, private val listener: NotifyAdapterListener) :
    RecyclerView.Adapter<NotifyAdapter.MainHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainHolder {
        val binding = CardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return MainHolder(binding)
    }

    override fun onBindViewHolder(holder: MainHolder, position: Int) {
        val notify = notifies[holder.adapterPosition]
        holder.bind(notify, listener)
    }

    override fun getItemCount(): Int = notifies.size

    class MainHolder(private val binding : CardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(notify: NotifyModel, listener: NotifyAdapterListener) {
            binding.tvTitle.text = notify.title
            binding.tvContent.text = notify.context
            binding.tvTime.text = notify.time
            binding.root.setOnClickListener{listener.onCardClick(notify)}
        }
    }
}