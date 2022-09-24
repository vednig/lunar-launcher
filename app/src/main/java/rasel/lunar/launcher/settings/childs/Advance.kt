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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import rasel.lunar.launcher.R
import rasel.lunar.launcher.databinding.SettingsAdvanceBinding
import kotlin.system.exitProcess


internal class Advance : BottomSheetDialogFragment() {
    private lateinit var binding : SettingsAdvanceBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = SettingsAdvanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.advanceSettings.addOnButtonCheckedListener { _: MaterialButtonToggleGroup?, checkedId: Int, isChecked: Boolean ->
            if (isChecked) {
                when (checkedId) {
                    binding.reset.id -> {
                        MaterialAlertDialogBuilder(requireActivity())
                            .setTitle(R.string.reset)
                            .setMessage(R.string.reset_message)
                            .setPositiveButton(R.string.proceed) { dialog, _ ->
                                dialog.dismiss()
                                Runtime.getRuntime().exec("pm clear " + requireContext().packageName)
                            }
                            .setNeutralButton(android.R.string.cancel) {dialog, _ -> dialog.dismiss() }
                            .show()
                    }
                    binding.restart.id -> {
                        val intent = (requireContext().packageManager).getLaunchIntentForPackage(requireContext().packageName)
                        requireContext().startActivity(Intent.makeRestartActivityTask(intent?.component))
                        exitProcess(0)
                    }
                }
            }
        }
    }
}