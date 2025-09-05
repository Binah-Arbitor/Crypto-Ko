package com.cryptoko.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cryptoko.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AboutFragment : Fragment() {
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val versionText = view.findViewById<TextView>(R.id.version_text)
        val packageInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
        versionText.text = "Version ${packageInfo.versionName}"
        
        // Make license clickable
        val licenseText = view.findViewById<TextView>(R.id.license_text)
        licenseText.setOnClickListener {
            showFullLicense()
        }
    }
    
    private fun showFullLicense() {
        val fullLicenseText = """
            MIT License

            Copyright (c) 2025 Binah-Arbitor

            Permission is hereby granted, free of charge, to any person obtaining a copy
            of this software and associated documentation files (the "Software"), to deal
            in the Software without restriction, including without limitation the rights
            to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
            copies of the Software, and to permit persons to whom the Software is
            furnished to do so, subject to the following conditions:

            The above copyright notice and this permission notice shall be included in all
            copies or substantial portions of the Software.

            THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
            IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
            FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
            AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
            LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
            OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
            SOFTWARE.
        """.trimIndent()
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("MIT License")
            .setMessage(fullLicenseText)
            .setPositiveButton("Close", null)
            .create()
            .apply {
                show()
                // Make the license text scrollable in the dialog
                findViewById<TextView>(android.R.id.message)?.apply {
                    textSize = 12f
                    setTextIsSelectable(true)
                }
            }
    }
}