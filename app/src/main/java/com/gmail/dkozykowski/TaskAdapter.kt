package com.gmail.dkozykowski

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    class ViewHolder(val postView: ItemListView) : RecyclerView.ViewHolder(postView)
    private var data: ArrayList<Task> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        println("dotarlem tutaj!!!")
        println("dotarlem tutaj!!!")
        println("dotarlem tutaj!!!")
        println("dotarlem tutaj!!!")
        println("dotarlem tutaj!!!")
        println("dotarlem tutaj!!!")
        println("dotarlem tutaj!!!")
        //TODO("Not yet implemented")
        return ViewHolder(ItemListView(parent.context))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.postView.bind(data[position])
    }

    fun updateData(data: List<Task>) {
        this.data = data as ArrayList<Task>
        notifyDataSetChanged()
    }
}