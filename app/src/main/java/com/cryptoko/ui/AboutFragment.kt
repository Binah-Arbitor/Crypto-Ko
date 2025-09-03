package com.cryptoko.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.cryptoko.R

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
    }
}