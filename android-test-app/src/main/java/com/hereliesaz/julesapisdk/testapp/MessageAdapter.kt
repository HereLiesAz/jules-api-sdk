package com.hereliesaz.julesapisdk.testapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return getItem(position).type.name.hashCode()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val view = when (viewType) {
            MessageType.USER.name.hashCode() -> layoutInflater.inflate(android.R.layout.simple_list_item_2, parent, false)
            MessageType.BOT.name.hashCode() -> layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            MessageType.ERROR.name.hashCode() -> layoutInflater.inflate(android.R.layout.simple_list_item_1, parent, false)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val text1: TextView? = itemView.findViewById(android.R.id.text1)
        private val text2: TextView? = itemView.findViewById(android.R.id.text2)

        fun bind(message: Message) {
            when (message.type) {
                MessageType.USER -> {
                    text2?.text = message.text
                }
                MessageType.BOT -> {
                    text1?.apply {
                        text = message.text
                        setTextColor(Color.BLACK)
                    }
                }
                MessageType.ERROR -> {
                    text1?.apply {
                        text = message.text
                        setTextColor(Color.RED)
                    }
                }
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}
