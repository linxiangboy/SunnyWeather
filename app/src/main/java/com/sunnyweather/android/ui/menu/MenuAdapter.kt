package com.sunnyweather.android.ui.menu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sunnyweather.android.databinding.MenuItemBinding
import com.sunnyweather.android.logic.model.LngLatCityDisSkyTem
import com.sunnyweather.android.logic.model.MenuCollect
import com.sunnyweather.android.logic.model.Weather
import com.sunnyweather.android.logic.model.getSky
import com.sunnyweather.android.tool.LogUtil
import com.sunnyweather.android.tool.showToast
import com.sunnyweather.android.ui.weather.WeatherActivity

class MenuAdapter(private val fragment: MenuFragment, private val llcdList: List<MenuCollect>) : RecyclerView.Adapter<MenuAdapter.ViewHolder>() {

    class ViewHolder(val mbinding: MenuItemBinding): RecyclerView.ViewHolder(mbinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mbinding = MenuItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val holder = ViewHolder(mbinding)

        //按钮点击事件
        holder.itemView.setOnClickListener {
            val position = holder.adapterPosition
            val llcd = llcdList[position]
            val activity = fragment.activity
            if (activity is WeatherActivity){ //如果此时嵌入的是WeatherActivity
                activity.mBinding.drawerLayout.closeDrawers() //关闭drawerLayout
                activity.viewModel.location = llcd.location
                activity.viewModel.cityName = llcd.city
                activity.viewModel.districtName = llcd.district

                activity.viewModel.collectTag = 1 //收藏且不触发点击事件
                activity.changeCheckBox() //改变按钮状态

                activity.viewModel.homecity = llcd.homecity //是否为主页城市
                activity.changefabBtn() //改变悬浮按钮隐藏/显示

                activity.refreshShowWeather(Weather(llcd.realtime, llcd.daily)) //将list里的weather数据传给Weather
            } else { "其他布局".showToast() }
        }

        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val llcd = llcdList[position]
        holder.mbinding.cityName.text = llcd.city
        holder.mbinding.districtName.text = llcd.district
        holder.mbinding.temperature.text = "${llcd.realtime.temperature.toInt()} °C"
        holder.mbinding.menuItemLlayout.setBackgroundResource(getSky(llcd.realtime.skycon).bg)
        if (llcd.homecity.toBoolean()){ //主页城市按钮
            holder.mbinding.homecityTag.visibility = View.VISIBLE
        } else {
            holder.mbinding.homecityTag.visibility = View.GONE
        }
    }

    override fun getItemCount() = llcdList.size

}