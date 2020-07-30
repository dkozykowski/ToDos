package com.gmail.dkozykowski

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gmail.dkozykowski.QueryTaskType.*
import com.gmail.dkozykowski.data.DB
import com.gmail.dkozykowski.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskAdapter(val queryType: QueryTaskType) : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
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
        }, updateCallback = {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        DB.db.taskDao().updateTask(it)
//                        if (it.done && queryType == ALL_ACTIVE) { notifyItemRemoved(position) }
//                        else if (!it.done && queryType == DONE) { notifyItemRemoved(position) }
//                        else if (!it.important && queryType == IMPORTANT) { notifyItemRemoved(position) }
//                        else if (it.done && queryType == IMPORTANT) { notifyItemRemoved(position) }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        })
    }

    fun updateData(data: List<Task>) {
        this.data = data as ArrayList<Task>
        sortData()
        notifyDataSetChanged()
    }

    fun sortData() {
        data.sortWith(compareBy(
            { !it.important },
            { it.date }
        ))
    }
}