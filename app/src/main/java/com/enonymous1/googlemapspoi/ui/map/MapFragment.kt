package com.enonymous1.googlemapspoi.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.enonymous1.googlemapspoi.PoiApplication
import com.enonymous1.googlemapspoi.R
import com.enonymous1.googlemapspoi.data.Poi
import com.enonymous1.googlemapspoi.databinding.FragmentMapBinding
import com.enonymous1.googlemapspoi.ui.PoiViewModel
import com.enonymous1.googlemapspoi.ui.PoiViewModelFactory
import com.enonymous1.googlemapspoi.ui.details.DetailsFragment
import com.enonymous1.googlemapspoi.ui.form.FormFragment
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.launch

class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PoiViewModel by activityViewModels {
        PoiViewModelFactory((requireActivity().application as PoiApplication).repository)
    }

    private var googleMap: GoogleMap? = null
    private var cameraMovedToUser = false
    private var lastKnownLatLng: LatLng? = null

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) moveToUserLocation()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.mapToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_add -> {
                    val target = lastKnownLatLng ?: googleMap?.cameraPosition?.target
                    if (target != null) {
                        openForm(poiId = null, location = target)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            R.string.no_location_found,
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                    true
                }
                else -> false
            }
        }

        val searchItem = binding.mapToolbar.menu.findItem(R.id.action_search)
        (searchItem?.actionView as? SearchView)?.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.setQuery(query.orEmpty())
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.setQuery(newText.orEmpty())
                    return true
                }
            }
        )

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync { map ->
            googleMap = map
            map.setInfoWindowAdapter(PoiInfoWindowAdapter(requireContext()))
            map.setOnInfoWindowClickListener { marker ->
                (marker.tag as? Long)?.let { poiId ->
                    findNavController().navigate(
                        R.id.action_mapFragment_to_detailsFragment,
                        bundleOf(DetailsFragment.ARG_POI_ID to poiId),
                    )
                }
            }
            map.setOnMapLongClickListener { latLng ->
                openForm(poiId = null, location = latLng)
            }
            enableMyLocation()
            requestLocationPermissionIfNeeded()
            refreshMarkers(viewModel.pois.value)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.pois.collect { pois -> refreshMarkers(pois) }
            }
        }
    }

    private fun openForm(poiId: Long?, location: LatLng) {
        findNavController().navigate(
            R.id.action_mapFragment_to_formFragment,
            bundleOf(
                FormFragment.ARG_POI_ID to (poiId ?: -1L),
                FormFragment.ARG_LATITUDE to location.latitude,
                FormFragment.ARG_LONGITUDE to location.longitude,
            ),
        )
    }

    private fun refreshMarkers(pois: List<Poi>) {
        val map = googleMap ?: return
        map.clear()
        for (poi in pois) {
            val marker = map.addMarker(
                MarkerOptions()
                    .position(LatLng(poi.latitude, poi.longitude))
                    .title(poi.title)
                    .snippet(poi.description),
            )
            marker?.tag = poi.id
        }
    }

    private fun requestLocationPermissionIfNeeded() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            moveToUserLocation()
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                googleMap?.isMyLocationEnabled = true
            } catch (_: SecurityException) {
                // Permission was revoked between the check and this call; the
                // map still works, it just has no blue dot.
            }
        }
    }

    private fun moveToUserLocation() {
        if (cameraMovedToUser) return
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        enableMyLocation()
        LocationServices.getFusedLocationProviderClient(requireActivity())
            .lastLocation
            .addOnSuccessListener { location ->
                if (location == null) return@addOnSuccessListener
                val latLng = LatLng(location.latitude, location.longitude)
                lastKnownLatLng = latLng
                if (!cameraMovedToUser) {
                    googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                    cameraMovedToUser = true
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

/** Info window shows the POI's own title/description — no EXIF read at render time. */
private class PoiInfoWindowAdapter(
    private val context: android.content.Context,
) : GoogleMap.InfoWindowAdapter {
    override fun getInfoWindow(marker: Marker): View? = null

    override fun getInfoContents(marker: Marker): View {
        val binding = com.enonymous1.googlemapspoi.databinding.PhotoInfoWindowBinding
            .inflate(LayoutInflater.from(context))
        binding.photoTitleView.text = marker.title
        binding.photoDescView.text = marker.snippet
        return binding.root
    }
}
