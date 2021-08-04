package com.example.kotlintest

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.abc.*
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var abc: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.abc)

        val m = application as MyApp
        val adapter = abcd(listOf())
        oof.adapter = adapter
        abc.getMutable().observe(this, {
            adapter.update(it)
        })
    }

    class abcd(var list: List<Pair<String, String>>) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return object : RecyclerView.ViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(android.R.layout.simple_list_item_1, parent, false)
            ) {}
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            logit(list[position])
            holder.itemView.findViewById<TextView>(android.R.id.text1).text = list[position].first
        }

        override fun getItemCount(): Int {
            return list.size
        }

        fun update(newList: List<Pair<String, String>>) {
            val m = test(newList, list)
            list = newList
            DiffUtil.calculateDiff(m).dispatchUpdatesTo(this)
        }
    }

    class test(
        private val list: List<Pair<String, String>>,
        private val oldList: List<Pair<String, String>>
    ) : DiffUtil.Callback() {

        override fun getOldListSize() = oldList.size

        override fun getNewListSize() = list.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == list[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].second == list[newItemPosition].second
        }

    }
}