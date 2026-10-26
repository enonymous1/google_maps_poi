package com.enonymous1.googlemapspoi.ui.details

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.enonymous1.googlemapspoi.PoiApplication
import com.enonymous1.googlemapspoi.R
import com.enonymous1.googlemapspoi.data.Directions
import com.enonymous1.googlemapspoi.data.Poi
import com.enonymous1.googlemapspoi.databinding.FragmentDetailsBinding
import com.enonymous1.googlemapspoi.ui.PoiViewModel
import com.enonymous1.googlemapspoi.ui.PoiViewModelFactory
import com.enonymous1.googlemapspoi.ui.form.FormFragment
import kotlinx.coroutines.launch
import java.io.File

class DetailsFragment : Fragment() {

    companion object {
        const val ARG_POI_ID = "poi_id"
    }

    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PoiViewModel by activityViewModels {
        PoiViewModelFactory((requireActivity().application as PoiApplication).repository)
    }

    private var poi: Poi? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val poiId = requireArguments().getLong(ARG_POI_ID, -1L)
        viewLifecycleOwner.lifecycleScope.launch {
            val loaded = viewModel.getPoi(poiId)
            poi = loaded
            if (loaded == null) {
                Toast.makeText(requireContext(), R.string.poi_not_found, Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                return@launch
            }
            binding.detailsTitleView.text = loaded.title
            binding.detailsDescView.text = loaded.description
            binding.detailsCategoryView.text = loaded.category.ifBlank {
                getString(R.string.category_none)
            }
            val photoFile = loaded.photoPath?.let { File(it) }?.takeIf { it.exists() }
            if (photoFile != null) {
                binding.detailsImageView.visibility = View.VISIBLE
                binding.detailsImageView.load(photoFile)
            } else {
                binding.detailsImageView.visibility = View.GONE
            }
        }

        binding.directionsButton.setOnClickListener {
            val current = poi ?: return@setOnClickListener
            openDirections(current.latitude, current.longitude)
        }

        binding.detailsToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit -> {
                    poi?.let {
                        findNavController().navigate(
                            R.id.action_detailsFragment_to_formFragment,
                            bundleOf(
                                FormFragment.ARG_POI_ID to it.id,
                                FormFragment.ARG_LATITUDE to it.latitude,
                                FormFragment.ARG_LONGITUDE to it.longitude,
                            ),
                        )
                    }
                    true
                }
                R.id.action_delete -> {
                    confirmDelete()
                    true
                }
                else -> false
            }
        }
    }

    private fun openDirections(latitude: Double, longitude: Double) {
        val navigation = Intent(
            Intent.ACTION_VIEW,
            Directions.navigationUri(latitude, longitude),
        ).setPackage("com.google.android.apps.maps")
        try {
            startActivity(navigation)
        } catch (_: ActivityNotFoundException) {
            // No Google Maps app: fall back to a generic geo: intent.
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Directions.mapsUri(latitude, longitude)))
            } catch (_: ActivityNotFoundException) {
                Toast.makeText(requireContext(), R.string.no_maps_app, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun confirmDelete() {
        val current = poi ?: return
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_marker_title)
            .setMessage(R.string.delete_marker_message)
            .setPositiveButton(R.string.yes) { _, _ ->
                // Deletes the POI row only. The photo file itself is left
                // untouched, so deleting a POI can never destroy a photo
                // that predates the Room database (e.g. a migrated v1 photo
                // in Pictures/map_images).
                viewModel.deletePoi(current) {
                    findNavController().popBackStack()
                }
            }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
