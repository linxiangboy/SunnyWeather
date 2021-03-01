package com.sunnyweather.android.ui.weather

import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bigkoo.pickerview.builder.OptionsPickerBuilder
import com.bigkoo.pickerview.listener.OnOptionsSelectListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sunnyweather.android.R
import com.sunnyweather.android.tool.LogUtil
import com.sunnyweather.android.tool.showToast
import com.sunnyweather.android.databinding.ActivityWeatherBinding
import com.sunnyweather.android.logic.model.*
import com.sunnyweather.android.tool.FileUtil
import com.sunnyweather.android.ui.menu.MenuFragment
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityWeatherBinding

    val viewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.NewInstanceFactory()
        ).get(WeatherViewModel::class.java)
    }

    private val dbHelper by lazy {
        viewModel.CreateMenuSqlDao(this, "MenuSqlStore.db", 1)
    }

    private val Menufragment by lazy {
        supportFragmentManager.findFragmentById(R.id.menuFragment) as MenuFragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemUI() //沉浸式状态栏

        mBinding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        /*
        * 如果为空就先从Intent中取出经纬度坐标和地区名称
        * 并复制到WeatherViewModel相应的变量中，然后对weatherLiveData进行观察
        * 当获取到服务器返回的天气数据时，就调用showWeatherInfo()方法进行解析和展示
        * */
        if (viewModel.location.isEmpty()) {
            viewModel.location = intent.getStringExtra("location_lnglat") ?: ""
        }
        if (viewModel.cityName.isEmpty()) {
            viewModel.cityName = intent.getStringExtra("city_name") ?: ""
        }
        if (viewModel.districtName.isEmpty()) {
            viewModel.districtName = intent.getStringExtra("district_name") ?: ""
        }
        if (viewModel.collectTag == 0) {
            viewModel.collectTag = intent.getIntExtra("collect_tag", 0)
            changeCheckBox() //改变收藏按钮的状态checkbox
        }
        if (viewModel.homecity == "false") {
            viewModel.homecity = intent.getStringExtra("home_city") ?: "false"
            changefabBtn() //悬浮按钮fabBtn是否可见,如果是主页城市就不可见
        }

        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()
            if (weather != null) {
                viewModel.realtime = Gson().toJson(weather.realtime)
                viewModel.daily = Gson().toJson(weather.daily)
                showWeatherInfo(weather) //刷新界面
                HomeCityWeatherData(weather) //更新sql中的数据
                Menufragment.refreshAdapter() //刷新recyclerview
            } else {
                "无法成功获取天气信息".showToast()
                result.exceptionOrNull()?.printStackTrace()
            }
            mBinding.swipeRefresh.isRefreshing = false //下拉刷新结束,隐藏进度条
        })

        //三级城市选择器选择城市后触发
        viewModel.queryLngLatLiveData.observe(this, Observer { result ->
            val lnglat = result.getOrNull()
            if (lnglat != null) {
                viewModel.location = lnglat //存入新的lnglat
                refreshWeather() //刷新天气
            } else {
                "无法成功获取天气信息".showToast()
                result.exceptionOrNull()?.printStackTrace()
            }
            mBinding.swipeRefresh.isRefreshing = false //下拉刷新结束,隐藏进度条
        })

        //三级城市选项器
        mBinding.now.selectcityLinear.setOnClickListener {
            initAddressPicker()
        }


        //收藏按钮,点击之后刷新Fragment里的adapter
        mBinding.now.collectBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && viewModel.collectTag == 0) {
                "收藏成功".showToast()
                //保存数据
                val countBoolean = QueryCountMenuSql() //查询数据库内是否有数据，没有的话就将收藏的这个页面设置为主页城市
                if (countBoolean) {
                    viewModel.homecity = "true"
                    changefabBtn() //刷新悬浮的主页城市按钮
                }
                saveMenuSql(
                    viewModel.location,
                    viewModel.cityName,
                    viewModel.districtName,
                    viewModel.realtime,
                    viewModel.daily,
                    countBoolean.toString()
                )
                Menufragment.refreshAdapter() //刷新收藏栏
            } else if (!isChecked && viewModel.collectTag == 0) {
                "取消收藏成功".showToast()
                deleteMenuSql(viewModel.cityName, viewModel.districtName) //删除数据
                Menufragment.refreshAdapter() //刷新收藏栏
                viewModel.homecity = "false"
                changefabBtn() //刷新悬浮的主页城市按钮
            } else {
                LogUtil.d("不触发点击事件")
            }
        }

        //打开DrawerLayout
        mBinding.now.navBtn.setOnClickListener {
            mBinding.drawerLayout.openDrawer(GravityCompat.START)
        }

        //DrawerLayout点击事件
        mBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) { //在drawerLayout状态发生切换的时候执行，一次时状态刚发生改变的时候，一次是状态改变彻底完成的时候
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) { //在状态发生改变的时候一直执行
            }

            override fun onDrawerOpened(drawerView: View) { //drawer打开的时候执行
                refreshWeather()
            }

            override fun onDrawerClosed(drawerView: View) { //drawer关闭的时候执行
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(
                    drawerView.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        })

        //悬浮按钮
        //点击之后将现在这个地址收藏并设为主页
        mBinding.fabBtn.setOnClickListener {
            //判断是否收藏过相同的城市
            val iscollect = isCollectMenuSql(viewModel.cityName, viewModel.districtName)
            if (iscollect) { //收藏过相同的城市，更改主页城市
                setHomeCityMenuSql(viewModel.cityName, viewModel.districtName)
                "已成功将${viewModel.cityName}${viewModel.districtName}设为主页城市".showToast()
                //刷新收藏栏
                Menufragment.refreshAdapter()
                viewModel.collectTag = 1
                changeCheckBox() //刷新收藏按钮状态
                viewModel.homecity = "true"
                //修改悬浮按钮为隐藏
                changefabBtn()
            } else { //没有收藏过相同的城市，收藏并更改主页城市
                saveMenuSql(
                    viewModel.location,
                    viewModel.cityName,
                    viewModel.districtName,
                    viewModel.realtime,
                    viewModel.daily,
                    "false"
                )
                setHomeCityMenuSql(viewModel.cityName, viewModel.districtName)
                "已成功将${viewModel.cityName}${viewModel.districtName}设为主页城市".showToast()
                //刷新收藏栏
                Menufragment.refreshAdapter()
                viewModel.collectTag = 1
                changeCheckBox() //刷新收藏按钮状态
                viewModel.homecity = "true"
                //修改悬浮按钮为隐藏
                changefabBtn()
            }
        }


        mBinding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary) //设置下拉刷新进度条显示颜色

        refreshWeather() //执行一次网络请求并显示下拉控件进度条

        mBinding.swipeRefresh.setOnRefreshListener { refreshWeather() }
    }

    private fun showWeatherInfo(weather: Weather) {

        mBinding.now.cityName.text = viewModel.cityName //城市名数据
        mBinding.now.districtName.text = viewModel.districtName //城市名数据

        val realtime = weather.realtime  //RealtimeResponse.Realtime数据
        val daily = weather.daily  //DailyResponse.Daily数据

        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()} °C" //当前温度
        mBinding.now.currentTemp.text = currentTempText
        mBinding.now.currentSky.text = getSky(realtime.skycon).info //getSky返回一个Sky类型的数据 取出对应的info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}" //当前温度
        mBinding.now.currentAQI.text = currentPM25Text
        mBinding.now.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg) //getSky返回一个Sky类型的数据 取出对应的bg,用setBackgroundResource()方法将背景替换

        // 填充forecast.xml布局中的数据
        mBinding.forecast.forecastLayout.removeAllViews() //删除所有子视图
        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]

            val view = LayoutInflater.from(this).inflate(
                R.layout.forecast_item,
                mBinding.forecast.forecastLayout, false
            )
            val dateInfo = view.findViewById<TextView>(R.id.dateInfo)
            val skyIcon = view.findViewById<ImageView>(R.id.skyIcon)
            val skyInfo = view.findViewById<TextView>(R.id.skyInfo)
            val temperatureInfo = view.findViewById<TextView>(R.id.temperatureInfo)
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value) //返回一个Sky类型的数据
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} °C"
            temperatureInfo.text = tempText
            mBinding.forecast.forecastLayout.addView(view)
        }
        //填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        mBinding.lifeIndex.coldRiskText.text = lifeIndex.coldRisk[0].desc
        mBinding.lifeIndex.dressingText.text = lifeIndex.dressing[0].desc
        mBinding.lifeIndex.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        mBinding.lifeIndex.carWashingText.text = lifeIndex.carWashing[0].desc
        mBinding.weatherLayout.visibility = View.VISIBLE
    }

    //执行一次网络请求并显示下拉控件进度条
    fun refreshWeather() {
        viewModel.refreshWeather(viewModel.location)
        mBinding.swipeRefresh.isRefreshing = true
    }

    //刷新界面上的数据
    fun refreshShowWeather(weather: Weather) {
        showWeatherInfo(weather)
    }

    //改变checkbox的状态
    fun changeCheckBox() {
        mBinding.now.collectBox.isChecked = viewModel.collectTag == 1
        viewModel.collectTag = 0 //恢复成可点击状态
    }

    //悬浮按钮fabBtn是否可见,如果是主页城市就不可见
    fun changefabBtn() {
        if (viewModel.homecity.toBoolean())
            mBinding.fabBtn.visibility = View.GONE
        else mBinding.fabBtn.visibility = View.VISIBLE

    }

    //隐藏状态栏
    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
//            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            //沉浸式状态栏(非透明)
            window.decorView.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
        window.statusBarColor = Color.TRANSPARENT // 将状态栏设置成透明色
    }

    /**
     * 初始化地址数据
     */
    private fun initAddressPicker() {
        val provinceItems = mutableListOf<AddressInfoPO>()
        val cityItems = mutableListOf<MutableList<AddressInfoPO>>()
        val districtItems = mutableListOf<MutableList<MutableList<AddressInfoPO>>>()
        //Json2Bean
        val pcaCodeList = Gson().fromJson<MutableList<PCACodePO>>(
            FileUtil.getAssetsFileText(
                this,
                "pcacode.json"
            ), object : TypeToken<MutableList<PCACodePO>>() {}.type
        )
        //遍历省
        pcaCodeList.forEach { pcaCode ->
            //存放省内市区
            val cityList = mutableListOf<AddressInfoPO>()
            //存放省内所有辖区
            val areaList = mutableListOf<MutableList<AddressInfoPO>>()
            //遍历省内市区
            pcaCode.children.forEach { cCode ->
                //添加省内市区
                cityList.add(AddressInfoPO(cCode.code, cCode.name))
                //存放市内辖区
                val areas = mutableListOf<AddressInfoPO>()
                //添加市内辖区
                cCode.children.forEach { addressInfo ->
                    areas.add(addressInfo)
                }
                areaList.add(areas)
            }
            //添加省份
            provinceItems.add(AddressInfoPO(pcaCode.code, pcaCode.name))
            //添加市区
            cityItems.add(cityList)
            //添加辖区
            districtItems.add(areaList)
        }
        //显示选择器
        showAddressPicker(provinceItems, cityItems, districtItems)
    }


    private fun showAddressPicker(
        provinceItems: MutableList<AddressInfoPO>,
        cityItems: MutableList<MutableList<AddressInfoPO>>,
        districtItems: MutableList<MutableList<MutableList<AddressInfoPO>>>
    ) {
        val addressPv =
            OptionsPickerBuilder(this, OnOptionsSelectListener { options1, options2, options3, _ ->

                //点击确定按钮之后触发
                val province = provinceItems[options1].pickerViewText //省份
                var city = cityItems[options1][options2].pickerViewText//城市
                val district = districtItems[options1][options2][options3].pickerViewText//区
                if (city == "市辖区") {
                    city = province
                }
                viewModel.cityName = city
                mBinding.now.cityName.text = city

                viewModel.districtName = district
                mBinding.now.districtName.text = district

                if (isCollectMenuSql(city, district)) { //已收藏
                    val weather = queryMenuSqlWeather(city, district) //取出sql内的天气数据
                    refreshShowWeather(weather) //刷新界面
                    viewModel.collectTag = 1 //不允许触发点击事件
                    mBinding.now.collectBox.isChecked = true //变为已收藏按钮
                    viewModel.collectTag = 0 //点击之后允许触发点击事件
                    changefabBtn() //悬浮按钮隐藏/显示
                } else { //未收藏
                    viewModel.getQueryLngLat("$province$city$district") //传入地区名获取天气数据
                    viewModel.collectTag = 1 //不允许触发点击事件
                    mBinding.now.collectBox.isChecked = false //变为未收藏按钮
                    viewModel.collectTag = 0 //点击之后允许触发点击事件
                    mBinding.fabBtn.visibility = View.VISIBLE //悬浮按钮可见
                }

            }).setTitleText("请选择地区")
                .setDividerColor(Color.BLACK)
                .setTextColorCenter(Color.BLACK) //设置选中项文字颜色
                .setContentTextSize(20)
                .build<AddressInfoPO>()
        addressPv.setPicker(provinceItems, cityItems, districtItems)
        addressPv.show()
    }


    //收藏功能
    private fun saveMenuSql(
        location: String,
        city: String,
        district: String,
        realtime: String,
        daily: String,
        homecity: String
    ) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            //组装数据
            put("location", location)
            put("city", city)
            put("district", district)
            put("realtime", realtime)
            put("daily", daily)
            put("homecity", homecity) //是否设置为主页城市
            put("citydis", "$city$district")
        }
        db.beginTransaction() //开启事务，保证让一系列操作要么全部完成要不全都不完成
        try {
            db.insert("MenuSql", null, values)
            db.setTransactionSuccessful() //事务执行成功
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction() //结束事务
        }
    }

    //取消收藏功能
    private fun deleteMenuSql(city: String, district: String) {
        val db = dbHelper.writableDatabase
        var homecity = "false"
        db.beginTransaction() //开启事务
        try {
            //查询拿到本次取消收藏的城市的homecity，然后删除
            val cursor =
                db.query("MenuSql", null, "citydis=?", arrayOf("$city$district"), null, null, null)
            if (cursor.moveToFirst()) {
                homecity = cursor.getString(cursor.getColumnIndex("homecity"))
            }
            db.delete("MenuSql", "citydis=?", arrayOf("$city$district"))
            cursor.close()

            //如果homecity是true
            val cursor1 = db.query("MenuSql", null, null, null, null, null, null)
            if (homecity == "true" && cursor1.count != 0) {
                //将光标的位置随机 然后将他设为主页城市
                val rand = (1..cursor.count).random()
                cursor.move(rand)
                val values = ContentValues()
                values.put("homecity", "true")
                db.update("MenuSql", values, null, null)
            }
            cursor1.close()
            db.setTransactionSuccessful() //事务成功
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction() //结束事务
        }
    }

    //查询是否收藏过这个城市
    private fun isCollectMenuSql(city: String, district: String): Boolean {
        val db = dbHelper.writableDatabase
        val cursor =
            db.query("MenuSql", null, "citydis=?", arrayOf("$city$district"), null, null, null)
        if (cursor.moveToFirst()) {
            val homecity = cursor.getString(cursor.getColumnIndex("homecity"))
            viewModel.homecity = homecity //是否为主页城市，悬浮控件隐藏/显示homecity
            cursor.close()
            return true
        } else {
            cursor.close()
            return false
        }
    }

    //将天气数据从sql内取出
    private fun queryMenuSqlWeather(city: String, district: String): Weather {
        val db = dbHelper.writableDatabase
        val cursor =
            db.query("MenuSql", null, "citydis=?", arrayOf("$city$district"), null, null, null)
        var realtime = ""
        var daily = ""
        if (cursor.moveToFirst()) {
            realtime = cursor.getString(cursor.getColumnIndex("realtime"))
            daily = cursor.getString(cursor.getColumnIndex("daily"))
        }
        return Weather(
            Gson().fromJson(realtime, RealtimeResponse.Realtime::class.java),
            Gson().fromJson(daily, DailyResponse.Daily::class.java)
        )
    }

    //查询sql内是否有数据,如果没有就返回true,然后直接把返回值传给saveSQl将收藏的这个页面设置为主页城市
    private fun QueryCountMenuSql(): Boolean {
        val db = dbHelper.writableDatabase
        val cursor = db.query("MenuSql", null, null, null, null, null, null)
        val count = cursor.count
        cursor.close()
        return count == 0 //如果cursor.count == 0就返回true, 不是就返回false
    }

    //将sql内homecity列的所有数据修改为false然后将传入城市行的homecity修改为true
    private fun setHomeCityMenuSql(city: String, district: String) {
        val db = dbHelper.writableDatabase

        val values = ContentValues()
        values.put("homecity", "true")

        db.beginTransaction() //开启事务
        try {
            db.execSQL("update MenuSql set homecity = 'false' where homecity = 'true'") //将homecity列所有为true的数据修改为false
            db.update(
                "MenuSql",
                values,
                "citydis = ?",
                arrayOf("$city$district")
            ) //将传入的citydis那列数据的homecity更新为true
            db.setTransactionSuccessful() //事务执行成功
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.endTransaction() //结束事务
        }
    }

    //更新主页城市的天气情况数据
    private fun HomeCityWeatherData(weather: Weather) {
        val db = dbHelper.writableDatabase //创建数据库
        val cursor = db.query("MenuSql", null, "homecity=?", arrayOf("true"), null, null, null)
        val values = ContentValues().apply {
            put("realtime", Gson().toJson(weather.realtime))
            put("daily", Gson().toJson(weather.daily))
        }
        if (cursor.moveToFirst()) {
            db.update("MenuSql", values, null, null)
        }
        cursor.close()
    }


}
