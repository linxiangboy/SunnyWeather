package com.sunnyweather.android.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.databinding.FragmentHomeBinding
import com.sunnyweather.android.tool.CharStrTool
import com.sunnyweather.android.tool.LogUtil
import com.sunnyweather.android.ui.weather.WeatherActivity

//打开APP加载的第一个页面
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    /*
    * lazy 懒加载技术来获取HomeViewModel实例
    * 允许我们在整个类中随时使用viewModel这个变量而完全不用关心它们何时初始化、是否为空等前提条件
    * */
    val viewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(HomeViewModel::class.java)
    }

    //加载fragment时调用
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false) //加载Fragment布局
        return binding.root
    }

    //确保与fragment相关联的Activity已经创建完毕时调用
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //如果HomeFragment被嵌入MainActivity并且本地存储有数据的时候跳转至Weather页面，并将lng/lat/city数据带过去
        //否则请求IP地址对应的城市 or 地址的经纬度城市信息

        viewModel.queryIpLngLatLiveData.observe(this, Observer { result ->
            val lnglatcity = result.getOrNull()
            if (lnglatcity != null){
                val intent = Intent(context, WeatherActivity::class.java).apply {
                    putExtra("location_lnglat", lnglatcity.location)
                    putExtra("city_name", lnglatcity.city)
                }
                startActivity(intent)
                activity?.finish()

                viewModel.saveLngLatCity("${lnglatcity.location}&${lnglatcity.city}&")

            } else {
                "自动获取城市失败，请手动选择定位城市" //跳转至WeatherActivity
                result.exceptionOrNull()?.printStackTrace()
            }
        })


        if (activity is MainActivity && viewModel.isLngLatCitySaved()){
            val lnglatcity = CharStrTool.splitString(viewModel.getSavedLngLatCity().toString())
            var district = ""
            if (lnglatcity.size >= 3){
                district = CharStrTool.sqlitStringblank(lnglatcity[2])
            }
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lnglat", lnglatcity[0])
                putExtra("city_name", CharStrTool.sqlitStringblank(lnglatcity[1]))
                putExtra("district_name", district)
            }
            startActivity(intent)
            activity?.finish()

        } else {
            viewModel.getQueryIp_LngLat()//获取ip对应的地址信息
        }

    }



    //当与Fragment关联的视图被移除时调用
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}