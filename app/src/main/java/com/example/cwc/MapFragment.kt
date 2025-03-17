package com.example.mymyko

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        etSearch = view.findViewById(R.id.et_search)
        btnSearch = view.findViewById(R.id.btn_search)

        btnSearch.setOnClickListener {
            val query = etSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                searchCoffeeShops(query)
            } else {
                Toast.makeText(requireContext(), "Enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Center the map on Israel
        val israelCenter = LatLng(31.0461, 34.8516)
        mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(israelCenter, 7f))

        // Set a marker click listener to open more details
        mMap?.setOnMarkerClickListener { marker ->
            openCoffeeShopDetails(marker)
            true  // Indicates we have consumed the event.
        }
    }

    // Helper function to add a marker with a description
    private fun addCoffeeMarker(location: LatLng, title: String, description: String) {
        val marker = mMap?.addMarker(
            MarkerOptions().position(location).title(title)
        )
        marker?.tag = description
    }

    private fun searchCoffeeShops(query: String) {
        mMap?.clear()

        when {
            query.equals("aroma", ignoreCase = true) -> {
                val telAviv = LatLng(32.0853, 34.7818)
                val jerusalem = LatLng(31.7683, 35.2137)
                val haifa = LatLng(32.7940, 34.9896)
                val rishonLezion = LatLng(31.9714, 34.7925)

                addCoffeeMarker(telAviv, "Aroma Tel Aviv", "A cozy café with a vibrant atmosphere in Tel Aviv.")
                addCoffeeMarker(jerusalem, "Aroma Jerusalem", "Known for its artisan brews in the heart of Jerusalem.")
                addCoffeeMarker(haifa, "Aroma Haifa", "Enjoy sea views with your coffee in Haifa.")
                addCoffeeMarker(rishonLezion, "Aroma Rishon LeZion", "A popular spot for both locals and visitors.")
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(telAviv, 10f))
            }
            query.equals("cafe nero", ignoreCase = true) -> {
                val telAviv = LatLng(32.0853, 34.7818)
                val netanya = LatLng(32.3214, 34.8535)
                val beerSheva = LatLng(31.2518, 34.7913)
                val haifa = LatLng(32.7940, 34.9896)

                addCoffeeMarker(telAviv, "Cafe Nero Tel Aviv", "Stylish ambiance with modern decor in Tel Aviv.")
                addCoffeeMarker(netanya, "Cafe Nero Netanya", "A favorite meeting spot in Netanya.")
                addCoffeeMarker(beerSheva, "Cafe Nero Beer Sheva", "Comfortable seating and quality brews in Beer Sheva.")
                addCoffeeMarker(haifa, "Cafe Nero Haifa", "Relax and enjoy your coffee with a view in Haifa.")
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(telAviv, 10f))
            }
            query.equals("coffee bean", ignoreCase = true) -> {
                val telAviv = LatLng(32.0853, 34.7818)
                val jerusalem = LatLng(31.7683, 35.2137)
                val ramatGan = LatLng(32.0684, 34.8245)
                val haifa = LatLng(32.7940, 34.9896)

                addCoffeeMarker(telAviv, "Coffee Bean Tel Aviv", "A trendy café known for its unique blends in Tel Aviv.")
                addCoffeeMarker(jerusalem, "Coffee Bean Jerusalem", "Experience a blend of tradition and innovation.")
                addCoffeeMarker(ramatGan, "Coffee Bean Ramat Gan", "Popular for its lively vibe and specialty coffees.")
                addCoffeeMarker(haifa, "Coffee Bean Haifa", "Offers a scenic view along with excellent coffee.")
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(telAviv, 10f))
            }
            query.equals("cafe cafe", ignoreCase = true) -> {
                val telAviv = LatLng(32.0853, 34.7818)
                val rishonLezion = LatLng(31.9714, 34.7925)
                val netanya = LatLng(32.3214, 34.8535)
                val jerusalem = LatLng(31.7683, 35.2137)

                addCoffeeMarker(telAviv, "Cafe Cafe Tel Aviv", "Bright and modern space in Tel Aviv.")
                addCoffeeMarker(rishonLezion, "Cafe Cafe Rishon LeZion", "A casual spot for great coffee in Rishon LeZion.")
                addCoffeeMarker(netanya, "Cafe Cafe Netanya", "Relax in a friendly atmosphere in Netanya.")
                addCoffeeMarker(jerusalem, "Cafe Cafe Jerusalem", "A must-visit spot in Jerusalem for coffee lovers.")
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(telAviv, 10f))
            }
            query.equals("cofix", ignoreCase = true) -> {
                val telAviv = LatLng(32.0853, 34.7818)
                val jerusalem = LatLng(31.7683, 35.2137)
                val netanya = LatLng(32.3214, 34.8535)
                val beerSheva = LatLng(31.2518, 34.7913)

                addCoffeeMarker(telAviv, "Cofix Tel Aviv", "Enjoy a modern twist on classic coffee drinks in Tel Aviv.")
                addCoffeeMarker(jerusalem, "Cofix Jerusalem", "A trendy café with a comfortable setting in Jerusalem.")
                addCoffeeMarker(netanya, "Cofix Netanya", "A hotspot in Netanya for coffee enthusiasts.")
                addCoffeeMarker(beerSheva, "Cofix Beer Sheva", "Known for its quick service and quality coffee in Beer Sheva.")
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(telAviv, 10f))
            }
            query.equals("gansipur", ignoreCase = true) -> {
                val telAviv = LatLng(32.0853, 34.7818)
                val jerusalem = LatLng(31.7683, 35.2137)
                val haifa = LatLng(32.7940, 34.9896)
                val netanya = LatLng(32.3214, 34.8535)

                addCoffeeMarker(telAviv, "Gansipur Tel Aviv", "A vibrant café with a unique menu in Tel Aviv.")
                addCoffeeMarker(jerusalem, "Gansipur Jerusalem", "Mixing tradition with modern coffee culture in Jerusalem.")
                addCoffeeMarker(haifa, "Gansipur Haifa", "Offers an extensive coffee selection in Haifa.")
                addCoffeeMarker(netanya, "Gansipur Netanya", "A friendly spot with cozy seating in Netanya.")
                mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(telAviv, 10f))
            }
            else -> {
                Toast.makeText(requireContext(), "No data available for \"$query\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Opens a dialog displaying more information about the selected coffee shop.
    private fun openCoffeeShopDetails(marker: Marker) {
        val title = marker.title ?: "Coffee Shop"
        val description = marker.tag as? String ?: "No additional info available."

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(description)
            .setPositiveButton("Close", null)
            .show()
    }
}
