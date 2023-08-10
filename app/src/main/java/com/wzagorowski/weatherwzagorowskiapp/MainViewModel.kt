package com.wzagorowski.weatherwzagorowskiapp

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wzagorowski.weatherwzagorowskiapp.Adapters.WeatherModel

class MainViewModel : ViewModel() {
    val liveDataCurrent = MutableLiveData<WeatherModel>()
    val liveDataList = MutableLiveData<List<WeatherModel>>()
}