/*
 * Lunar Launcher
 * Copyright (C) 2022 Md Rasel Hossain
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package rasel.lunar.launcher.settings.childs

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.READ_MEDIA_IMAGES
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rasel.lunar.launcher.R
import rasel.lunar.launcher.databinding.ColorPickerBinding
import rasel.lunar.launcher.databinding.SettingsAppearancesBinding
import rasel.lunar.launcher.helpers.ColorPicker
import rasel.lunar.launcher.helpers.Constants.Companion.KEY_APPLICATION_THEME
import rasel.lunar.launcher.helpers.Constants.Companion.KEY_STATUS_BAR
import rasel.lunar.launcher.helpers.Constants.Companion.KEY_WINDOW_BACKGROUND
import rasel.lunar.launcher.helpers.UniUtils.Companion.getColorResId
import rasel.lunar.launcher.settings.SettingsActivity.Companion.settingsPrefs
import java.io.IOException
import java.util.*


internal class Appearances : BottomSheetDialogFragment() {

    private lateinit var binding : SettingsAppearancesBinding
    private lateinit var windowBackground : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SettingsAppearancesBinding.inflate(inflater, container, false)

        /* initialize views according to the saved values */
        when (settingsPrefs!!.getInt(KEY_APPLICATION_THEME, MODE_NIGHT_FOLLOW_SYSTEM)) {
            MODE_NIGHT_FOLLOW_SYSTEM -> binding.followSystemTheme.isChecked = true
            MODE_NIGHT_YES -> binding.selectDarkTheme.isChecked = true
            MODE_NIGHT_NO -> binding.selectLightTheme.isChecked = true
        }

        when (settingsPrefs!!.getBoolean(KEY_STATUS_BAR, false)) {
            false -> binding.hideStatusNegative.isChecked = true
            true -> binding.hideStatusPositive.isChecked = true
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireDialog() as BottomSheetDialog).dismissWithAnimation = true

        /* change theme */
        binding.themeGroup.setOnCheckedStateChangeListener { group, _ ->
            when (group.checkedChipId) {
                binding.followSystemTheme.id -> {
                    settingsPrefs!!.edit().putInt(KEY_APPLICATION_THEME, MODE_NIGHT_FOLLOW_SYSTEM).apply()
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
                }
                binding.selectDarkTheme.id -> {
                    settingsPrefs!!.edit().putInt(KEY_APPLICATION_THEME, MODE_NIGHT_YES).apply()
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
                }
                binding.selectLightTheme.id -> {
                    settingsPrefs!!.edit().putInt(KEY_APPLICATION_THEME, MODE_NIGHT_NO).apply()
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
                }
            }
        }

        binding.background.setOnClickListener { selectBackground() }
        binding.changeWallpaper.setOnClickListener { selectWallpaper() }

        binding.hideStatusGroup.setOnCheckedStateChangeListener { group, _ ->
            when (group.checkedChipId) {
                binding.hideStatusNegative.id -> settingsPrefs!!.edit().putBoolean(KEY_STATUS_BAR, false).apply()
                binding.hideStatusPositive.id -> settingsPrefs!!.edit().putBoolean(KEY_STATUS_BAR, true).apply()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        windowBackground = settingsPrefs!!.getString(KEY_WINDOW_BACKGROUND, defaultColorString).toString()
        binding.background.iconTint = ColorStateList.valueOf(Color.parseColor("#$windowBackground"))
    }

    private fun selectBackground() {
        val colorPickerBinding = ColorPickerBinding.inflate(requireActivity().layoutInflater)
        val dialogBuilder = MaterialAlertDialogBuilder(requireActivity())
            .setView(colorPickerBinding.root)
            .setNeutralButton(R.string.default_, null)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                settingsPrefs!!.edit().putString(KEY_WINDOW_BACKGROUND,
                    Objects.requireNonNull(colorPickerBinding.colorInput.text).toString().trim { it <= ' ' }).apply()
                this.onResume()
            }
            .show()

        /* set up color picker section */
        ColorPicker(windowBackground, colorPickerBinding.colorInput,
            colorPickerBinding.colorA, colorPickerBinding.colorR, colorPickerBinding.colorG,
            colorPickerBinding.colorB, colorPickerBinding.root).pickColor()

        dialogBuilder.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
            colorPickerBinding.colorInput.text =
                SpannableStringBuilder(defaultColorString)
        }
    }

    private fun selectWallpaper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // only for TIRAMISU and newer versions
            if (requireActivity().checkSelfPermission(READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                requireActivity().requestPermissions(arrayOf(READ_MEDIA_IMAGES), 1)
            } else {
                wallpaperChangeLauncher.launch(Intent(Intent.ACTION_PICK).setType("image/*"))
            }
        } else {
            if (requireActivity().checkSelfPermission(READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requireActivity().requestPermissions(arrayOf(READ_EXTERNAL_STORAGE), 1)
            } else {
                wallpaperChangeLauncher.launch(Intent(Intent.ACTION_PICK).setType("image/*"))
            }
        }
    }

    private val defaultColorString: String get() =
        requireActivity().getString(getColorResId(requireContext(), android.R.attr.colorBackground))
            .replace("#", "")

    private var wallpaperChangeLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                val uri = result.data?.data
                val projection = arrayOf(MediaStore.Images.Media.DATA)
                val cursor = requireContext().contentResolver.query(
                    uri!!, projection, null, null, null
                )
                cursor?.moveToFirst()
                val index = cursor!!.getColumnIndex(projection[0])
                val filePath = cursor.getString(index)
                cursor.close()
                val bitmap = BitmapFactory.decodeFile(filePath)
                val matrix = Matrix()
                matrix.postRotate(0F)
                try {
                    if (bitmap != null) {
                        WallpaperManager.getInstance(requireContext()).setBitmap(bitmap)
                        Toast.makeText(requireContext(),
                            requireActivity().getString(R.string.wallpaper_change_success), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(),
                            requireActivity().getString(R.string.image_pick_failed), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(requireContext(),
                        requireActivity().getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
