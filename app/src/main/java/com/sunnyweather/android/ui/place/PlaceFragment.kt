package com.sunnyweather.android.ui.place

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunnyweather.android.MainActivity
import com.sunnyweather.android.Tool.showToast

import com.sunnyweather.android.databinding.FragmentPlaceBinding
import com.sunnyweather.android.ui.weather.WeatherActivity

class PlaceFragment : Fragment() {

    private var _binding: FragmentPlaceBinding? = null
    private val binding get() = _binding!!

    /*
    * lazy 懒加载技术来获取PlaceViewModel实例
    * 允许我们在整个类中随时使用viewModel这个变量而完全不用关心它们何时初始化、是否为空等前提条件
    * */
    val viewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(PlaceViewModel::class.java)
    }

    private lateinit var adapter: PlaceAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlaceBinding.inflate(inflater, container, false) //加载Fragment布局
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (activity is MainActivity && viewModel.isPlaceSaved()){ //只有当PlaceFragment被嵌入到MainActivity并且本地存储有数据时才会跳转到WeatherActivity
            val place = viewModel.getSavedPlace()
            val intent = Intent(context, WeatherActivity::class.java).apply {
                putExtra("location_lng", place.location.lng)
                putExtra("location_lat", place.location.lat)
                putExtra("place_name", place.name)
            }
            startActivity(intent)
            activity?.finish()
            return
        }

        val layoutManager = LinearLayoutManager(activity)
        //给RecyclerView设置和layoutManager和适配器,并使用了viewModel.placeList集合作为数据源
        _binding?.recyclerView?.layoutManager = layoutManager
        adapter = PlaceAdapter(this, viewModel.placeList)
        _binding?.recyclerView?.adapter = adapter

        //addTextChangedListener用来监听搜索框内容的变化情况，每当搜索框内容发生变化就获取新的内容
        _binding?.searchPlaceEdit?.addTextChangedListener { editable ->
            val content = editable.toString()
            if (content.isNotEmpty()){ //不为空
                viewModel.searchPlaces(content) //发起网络请求
            } else{
                _binding?.recyclerView?.visibility = View.GONE //滚动控件隐藏
                _binding?.bgImaeView?.visibility = View.VISIBLE //背景图片显示
                viewModel.placeList.clear() //清除placeList中的数据
                adapter.notifyDataSetChanged() //刷新RecyclerView
            }
        }

        /*
        * 对PlaceLiveModel中的placeLiveData进行观察
        * 当有任何数据变化时，就会回调到传入的Observer接口实现中
        *
        * result是一个list<Place>集合
        * */
        viewModel.placeLiveData.observe(this, Observer{ result ->
            val places = result.getOrNull() //如果实例表示成功，则返回封装的值;如果实例表示失败，则返回null
            if (places != null){
                _binding?.recyclerView?.visibility = View.VISIBLE //滚动控件显示
                _binding?.bgImaeView?.visibility = View.GONE //背景图片隐藏
                viewModel.placeList.clear() //清除list中的数据
                viewModel.placeList.addAll(places) //将places数据添加进来
                adapter.notifyDataSetChanged() //刷新RecyclerView
            } else{
                "未能查询到任何地点".showToast()
                /*
                * 打印具体原因
                * 如果该实例表示失败，则返回封装的可抛出异常;如果成功，则返回null
                * */
                result.exceptionOrNull()?.printStackTrace()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}