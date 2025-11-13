package com.hereliesaz.julesapisdk.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hereliesaz.julesapisdk.Session

class SessionsAdapter(
    private val onSessionClicked: (Session) -> Unit
) : ListAdapter<Session, SessionsAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return SessionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        val session = getItem(position)
        holder.bind(session, onSessionClicked)
    }

    class SessionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(android.R.id.text1)
        private val nameTextView: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(session: Session, onSessionClicked: (Session) -> Unit) {
            // *** MODIFIED: We only have 'name' from PartialSession ***
            titleTextView.text = session.name.substringAfterLast('/') // Show ID as title
            nameTextView.text = session.name // Show full path as subtitle

            itemView.setOnClickListener {
                onSessionClicked(session)
            }
        }
    }

    private class SessionDiffCallback : DiffUtil.ItemCallback<Session>() {
        override fun areItemsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem.name == newItem.name
        }

        override fun areContentsTheSame(oldItem: Session, newItem: Session): Boolean {
            return oldItem == newItem
        }
    }
}