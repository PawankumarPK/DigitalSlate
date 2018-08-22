package com.technocom.digitalslate.adapter

import android.content.Context
import android.graphics.Typeface
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.technocom.digitalslate.R
import com.technocom.digitalslate.fragments.FragmentAlphabets
import kotlinx.android.synthetic.main.view_holder.view.*


/*
Created by Pawan kumar
 */


class RecyclerViewAdapter(private val context: Context, private val list: ArrayList<String>) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>(), FragmentAlphabets.SmallAlphabets {
    private var textCaps = true

    companion object {
        lateinit var myTypeface: Typeface
    }

    init {
        FragmentAlphabets.alpha = this
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder (LayoutInflater.from(parent.context).inflate(R.layout.view_holder, parent, false))
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    override fun alphabetsSmall(bool: Boolean) {
        textCaps = bool
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun bindItems(data: String) {
            itemView.mTextview.text = data
            itemView.mTextview.setAllCaps(textCaps)
            itemView.mTextview.typeface = myTypeface
        }

    }
}