package com.sunnyweather.android.logic.model

import com.contrarywind.interfaces.IPickerViewData

//存放省名称和所属市
data class PCACodePO(
    val code: String,
    val name: String,
    val children: MutableList<CCodePO>
)

//存放市名称和所属辖区
data class CCodePO(
    val code: String,
    val name: String,
    val children: MutableList<AddressInfoPO> //可变序列
)

//用于显示PickerView
data class AddressInfoPO(
    val code: String, //地区编码
    val name: String //地区名称
) : IPickerViewData {
    override fun getPickerViewText(): String = name
}
