package com.gmail.dkozykowski

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    class ViewHolder(val postView: ItemListView) : RecyclerView.ViewHolder(postView)

    private var data: ArrayList<Task> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemListView(parent.context))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.postView.bind(data[position], deleteCallback = {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        DB.db.taskDao().deleteTask(data[position])
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            data.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, data.size - 1)
        })
    }

    fun updateData(data: List<Task>) {
        this.data = data as ArrayList<Task>
        notifyDataSetChanged()
    }
}