package com.sunnyweather.android.ui.place

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.databinding.PlaceItemBinding
import com.sunnyweather.android.logic.model.Place
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceAdapter(private val fragment: PlaceFragment, private val placeList: List<Place>) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    class ViewHolder(val mbinding: PlaceItemBinding) : RecyclerView.ViewHolder(mbinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceAdapter.ViewHolder {
        val mbinding = PlaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(mbinding)
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition //返回点击项position
            val place = placeList[position]
            val intent = Intent(parent.context, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }

            //点击任何子项布局时，在跳转到WeatherActivity之前，调用savePlace()方法存储选中的城市
            //将传入的Fragment改成PlaceFragment，次啊能调用placeFragment对应的PlaceViewModel
            fragment.viewModel.savePlace(place)

            fragment.startActivity(intent)
            fragment.activity?.finish()
        }
        return holder
    }

    override fun onBindViewHolder(holder: PlaceAdapter.ViewHolder, position: Int) {
        val place = placeList[position]
        holder.mbinding.placeName.text = place.name
        holder.mbinding.placeAddress.text = place.address
    }

    override fun getItemCount() = placeList.size

}