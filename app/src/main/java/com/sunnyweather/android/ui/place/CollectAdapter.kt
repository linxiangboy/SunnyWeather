package com.sunnyweather.android.ui.place

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.databinding.CollectItemBinding
import com.sunnyweather.android.logic.model.CollectResponse
import com.sunnyweather.android.logic.model.Sky
import com.sunnyweather.android.logic.model.getSky

class CollectAdapter(private val fragment: PlaceFragment, private val collectList: List<CollectResponse>) :
    RecyclerView.Adapter<CollectAdapter.ViewHolder>(){

    class ViewHolder(val mbinding: CollectItemBinding) : RecyclerView.ViewHolder(mbinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectAdapter.ViewHolder {
        val mbinding = CollectItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(mbinding)

        //具体逻辑

        return holder
    }

    override fun onBindViewHolder(holder: CollectAdapter.ViewHolder, position: Int) {
        val collect = collectList[position]
        holder.mbinding.cityName.text = collect.city
        holder.mbinding.districtName.text = collect.district
        holder.mbinding.temperature.text = collect.temperature + " °C"
        holder.mbinding.collectItemLlayout.setBackgroundResource(getSky(collect.skycon).bg)
    }

    override fun getItemCount() = collectList.size

}