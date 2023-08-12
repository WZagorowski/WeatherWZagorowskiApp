package com.wzagorowski.weatherwzagorowskiapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.wzagorowski.weatherwzagorowskiapp.MainViewModel
import com.wzagorowski.weatherwzagorowskiapp.adapter.WeatherAdapter
import com.wzagorowski.weatherwzagorowskiapp.adapter.WeatherModel
import com.wzagorowski.weatherwzagorowskiapp.databinding.FragmentDaysBinding

class DaysFragment : Fragment(), WeatherAdapter.Listener {
    private lateinit var binding: FragmentDaysBinding
    private lateinit var adapter: WeatherAdapter
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDaysBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        model.liveDataList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
    }

    private fun init() = with(binding) {
        adapter = WeatherAdapter(this@DaysFragment)
        rcViewDays.layoutManager = LinearLayoutManager(activity)
        rcViewDays.adapter = adapter

    }

    companion object {
        @JvmStatic
        fun newInstance() = DaysFragment()
    }

    override fun onClick(item: WeatherModel) {
        model.liveDataCurrent.value = item
    }
}