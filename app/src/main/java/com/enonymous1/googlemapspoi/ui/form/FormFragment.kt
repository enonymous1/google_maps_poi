package com.enonymous1.googlemapspoi.ui.form

import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.enonymous1.googlemapspoi.PoiApplication
import com.enonymous1.googlemapspoi.R
import com.enonymous1.googlemapspoi.data.Poi
import com.enonymous1.googlemapspoi.data.PoiSearch
import com.enonymous1.googlemapspoi.databinding.FragmentFormBinding
import com.enonymous1.googlemapspoi.ui.PoiViewModel
import com.enonymous1.googlemapspoi.ui.PoiViewModelFactory
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FormFragment : Fragment() {

    companion object {
        const val ARG_POI_ID = "poi_id"
        const val ARG_LATITUDE = "latitude"
        const val ARG_LONGITUDE = "longitude"
    }

    private var _binding: FragmentFormBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PoiViewModel by activityViewModels {
        PoiViewModelFactory((requireActivity().application as PoiApplication).repository)
    }

    private var existingPoi: Poi? = null
    private var currentPhotoPath: String? = null

    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            currentPhotoPath?.let { path ->
                binding.formImage.load(File(path))
            }
        } else if (existingPoi?.photoPath == null) {
            // Cancelled capture on a brand-new POI: don't keep a dangling path.
            currentPhotoPath = null
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val poiId = requireArguments().getLong(ARG_POI_ID, -1L)
        val latitude = requireArguments().getDouble(ARG_LATITUDE, Double.NaN)
        val longitude = requireArguments().getDouble(ARG_LONGITUDE, Double.NaN)
        if (!latitude.isNaN() && !longitude.isNaN()) {
            binding.formLocation.text = getString(
                R.string.location_format,
                latitude,
                longitude,
            )
        }

        if (poiId != -1L) {
            viewLifecycleOwner.lifecycleScope.launch {
                val poi = viewModel.getPoi(poiId)
                existingPoi = poi
                poi?.let {
                    binding.formTitle.setText(it.title)
                    binding.formDesc.setText(it.description)
                    binding.formCategory.setText(it.category)
                    currentPhotoPath = it.photoPath
                    it.photoPath?.let { path ->
                        val file = File(path)
                        if (file.exists()) binding.formImage.load(file)
                    }
                }
            }
        }

        binding.formToolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_camera -> {
                    dispatchTakePicture()
                    true
                }
                R.id.action_save -> {
                    savePoi(latitude, longitude)
                    true
                }
                else -> false
            }
        }
    }

    private fun dispatchTakePicture() {
        val photoFile = runCatching { createImageFile() }.getOrNull()
        if (photoFile == null) {
            Toast.makeText(requireContext(), R.string.photo_create_failed, Toast.LENGTH_SHORT).show()
            return
        }
        currentPhotoPath = photoFile.absolutePath
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            photoFile,
        )
        takePictureLauncher.launch(uri)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val folder = File(
            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "poi_photos",
        )
        // v1 never created its folder, so capture failed silently when the
        // folder did not exist yet. Create it explicitly here.
        if (!folder.exists()) folder.mkdirs()
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", folder)
    }

    private fun savePoi(latitude: Double, longitude: Double) {
        val title = binding.formTitle.text?.toString().orEmpty().trim()
        val description = binding.formDesc.text?.toString().orEmpty().trim()
        val category = PoiSearch.normaliseCategory(
            binding.formCategory.text?.toString().orEmpty()
        )
        when {
            title.isEmpty() || description.isEmpty() -> {
                Toast.makeText(requireContext(), R.string.fill_all_fields, Toast.LENGTH_SHORT).show()
                return
            }
            latitude.isNaN() || longitude.isNaN() -> {
                Toast.makeText(requireContext(), R.string.no_location_found, Toast.LENGTH_SHORT).show()
                return
            }
        }
        val existing = existingPoi
        val poi = Poi(
            id = existing?.id ?: 0,
            title = title,
            description = description,
            category = category,
            latitude = latitude,
            longitude = longitude,
            photoPath = currentPhotoPath,
            createdAt = existing?.createdAt ?: System.currentTimeMillis(),
        )
        viewModel.savePoi(poi) {
            findNavController().popBackStack()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
