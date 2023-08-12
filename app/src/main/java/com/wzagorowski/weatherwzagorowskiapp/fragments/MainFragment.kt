package com.wzagorowski.weatherwzagorowskiapp.fragments

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.tabs.TabLayoutMediator
import com.squareup.picasso.Picasso
import com.wzagorowski.weatherwzagorowskiapp.adapter.VpAdapter
import com.wzagorowski.weatherwzagorowskiapp.adapter.WeatherModel
import com.wzagorowski.weatherwzagorowskiapp.MainViewModel
import com.wzagorowski.weatherwzagorowskiapp.databinding.FragmentMainBinding
import org.json.JSONObject

class MainFragment : Fragment() {
    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance(),
    )
    private val tList = listOf("HOURS", "DAYS")
    private lateinit var binding: FragmentMainBinding
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
        requestWeatherData("Wroclaw")
    }

    private fun init() = with(binding) {
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vP.adapter = adapter
        TabLayoutMediator(tabLayout, vP) { tab, pos ->
            tab.text = tList[pos]
        }.attach()
    }

    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemperature = "${it.maxTemp}°C/${it.minTemp}°C"
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = it.currentTemp.ifEmpty { maxMinTemperature }
            tvCondition.text = it.condition
            tvMaxMin.text = if (it.currentTemp.isEmpty()) "" else maxMinTemperature
            Picasso.get().load("https:${it.imageUrl}").into(imWeather)
        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String) {
        val url = "$API_LINK?key=$API_KEY&q=$city&days=3&aqi=no&alerts=no"

        val queue: RequestQueue = Volley.newRequestQueue(context)
        val request = StringRequest(
            Request.Method.GET,
            url,
            { response ->
                parseWeatherData(response)
            },
            { error ->
                Log.d("MyLog", "Volley error: $error")
            }
        )
        queue.add(request)
    }

    private fun parseWeatherData(result: String) {
        val mainObject = JSONObject(result)
        val list = parseDays(mainObject)
        parseCurrentData(mainObject, list[0])
    }

    private fun parseDays(mainObject: JSONObject): List<WeatherModel> {
        val listWeatherItems = ArrayList<WeatherModel>()
        val cityName = mainObject.getJSONObject("location").getString("name")
        val hoursArray = mainObject.getJSONObject("forecast")
            .getJSONArray("forecastday")

        for (i in 0 until hoursArray.length()) {
            val day = hoursArray[i] as JSONObject
            val item = WeatherModel(
                city = cityName,
                time = day.getString("date"),
                condition = day.getJSONObject("day").getJSONObject("condition").getString("text"),
                currentTemp = "",
                maxTemp = day.getJSONObject("day").getString("maxtemp_c").substringBefore('.'),
                minTemp = day.getJSONObject("day").getString("mintemp_c").substringBefore('.'),
                imageUrl = day.getJSONObject("day").getJSONObject("condition")
                    .getString("icon"),
                hours = day.getJSONArray("hour").toString(),
            )
            listWeatherItems.add(item)
        }
        model.liveDataList.value = listWeatherItems
        return listWeatherItems
    }

    private fun parseCurrentData(mainObject: JSONObject, weatherItem: WeatherModel) {
        val item = WeatherModel(
            city = mainObject.getJSONObject("location").getString("name"),
            time = mainObject.getJSONObject("current").getString("last_updated"),
            condition = mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("text"),
            currentTemp = "${mainObject.getJSONObject("current").getString("temp_c")
                .substringBefore('.')}°C",
            maxTemp = weatherItem.maxTemp.substringBefore('.'),
            minTemp = weatherItem.minTemp.substringBefore('.'),
            imageUrl = mainObject.getJSONObject("current")
                .getJSONObject("condition").getString("icon"),
            hours = weatherItem.hours,
        )

        model.liveDataCurrent.value = item
        Log.d("MyLog", "Max: ${item.maxTemp}")
        Log.d("MyLog", "Min: ${item.minTemp}")
        Log.d("MyLog", "Time: ${item.hours}")
    }

    companion object {
        const val API_KEY = "9ee7be6758134214861200311230407"
        const val API_LINK = "https://api.weatherapi.com/v1/forecast.json"
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}