package com.example.wander

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.example.wander.databinding.ActivityMapsBinding
import com.google.android.gms.maps.model.*
import java.util.*

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val TAG = MapsActivity::class.java.simpleName

    private lateinit var map: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val REQUEST_PERMISSION_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {

        map = googleMap

        // Add a marker in home and move the camera. Also provide zoom level/
        val home = LatLng(53.40107, -2.58115)
        val zoom = 16f
        map.addMarker(MarkerOptions().position(home).title("Marker in home"))
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom))

        // Add a ground overlay
        val overlaySize = 100f
        val androidOverlay = GroundOverlayOptions()
            .image(BitmapDescriptorFactory.fromResource(R.drawable.android))
            .position(home, overlaySize)
        map.addGroundOverlay(androidOverlay)

        enableLocation()

        setMapLongClick(map)
        setPoiClick(map)
        setMapStyle(map)
    }

    private fun enableLocation() {
        if ( isPermissionGranted()) {
            map.setMyLocationEnabled(true)
        } else {
            ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSION_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if( requestCode == REQUEST_PERMISSION_LOCATION ) {
           if( grantResults.isNotEmpty() && (grantResults[0] ==PackageManager.PERMISSION_GRANTED)){
               enableLocation()
           }
       }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
       val inflater = menuInflater
        inflater.inflate(R.menu.map_options, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun isPermissionGranted(): Boolean {
        return ContextCompat.
        checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            // A Snippet is Additional text that's displayed below the title.
            val snippet = String.format(
                Locale.getDefault(),
                "Lat: %1$.5f, Long: %2$.5f",
                latLng.latitude,
                latLng.longitude
            )

            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            )
        }
    }

    private fun setPoiClick(map: GoogleMap){
        map.setOnPoiClickListener { poi ->
            val poiMarker = map.addMarker(
                MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name)
            )
            poiMarker!!.showInfoWindow()
        }
    }

    private fun setMapStyle(map: GoogleMap){
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style))
            if(!success){
                Log.e(TAG, "Style parsing failed")
            }
        } catch (e:Resources.NotFoundException){
            Log.e(TAG, "Unable to find style. Error: ", e)
        }
    }
}