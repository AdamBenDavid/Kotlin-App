package com.example.mymyko

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MapFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var tvWeatherRecommendation: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        val childFragment = BottomNavFragment()
        val bundle = Bundle()
        bundle.putString("current_page", "map")
        childFragment.arguments = bundle
        childFragmentManager.beginTransaction()
            .replace(R.id.navbar_container, childFragment)
            .commit()

        tvWeatherRecommendation = view.findViewById(R.id.tvWeatherRecommendation)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fetchWeatherAndUpdateRecommendation() // Fetch weather data when the map is created

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Center the map on Mykonos
        val mykonosCenter = LatLng(37.4467, 25.3289)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(mykonosCenter, 12f))

        // Add a marker for reference
        mMap?.addMarker(
            MarkerOptions()
                .position(mykonosCenter)
                .title("Welcome to Mykonos!")
        )
    }

    private fun fetchWeatherAndUpdateRecommendation() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val jsonData = withContext(Dispatchers.IO) {
                    val client = OkHttpClient()
                    val apiKey = "200b857da17d668dbf479de6ff89c982"
                    val url =
                        "https://api.openweathermap.org/data/2.5/weather?q=Mykonos,GR&units=metric&appid=$apiKey"
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    response.body?.string()
                }

                if (jsonData != null) {
                    val jsonObject = JSONObject(jsonData)
                    val main = jsonObject.getJSONObject("main")
                    val temp = main.getDouble("temp")
                    val weatherDescription =
                        jsonObject.getJSONArray("weather").getJSONObject(0).getString("description")

                    val recommendation = when {
                        temp < 10 -> "It's a chilly ${temp}°C with ${weatherDescription}! Visit Mykonos’ museums 🏛️ or cozy up in a seaside taverna 🍷."
                        temp in 10.0..20.0 -> "The temperature is ${temp}°C with ${weatherDescription}. Enjoy a scenic walk through Mykonos Town 🌆 or visit Little Venice! 🌅"
                        temp in 20.0..30.0 -> "It's a warm ${temp}°C in Mykonos! Perfect for a beach day at Paradise Beach 🏖️ or a boat tour to Delos! ⛵"
                        else -> "It's hot ${temp}°C with ${weatherDescription}! Chill at a luxurious Mykonos beach club 🏝️ or grab a refreshing cocktail 🍹."
                    }

                    // Update the UI
                    tvWeatherRecommendation.text = recommendation
                } else {
                    tvWeatherRecommendation.text = "Weather data not available for Mykonos"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                tvWeatherRecommendation.text = "Weather data not available for Mykonos"
            }
        }
    }
}
