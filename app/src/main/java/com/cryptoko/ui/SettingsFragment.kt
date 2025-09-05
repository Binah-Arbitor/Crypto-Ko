package com.cryptoko.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.cryptoko.R
import com.cryptoko.crypto.CipherAlgorithm
import com.cryptoko.utils.AppConfig
import com.google.android.material.switchmaterial.SwitchMaterial
import java.util.Locale

class SettingsFragment : Fragment() {
    
    private lateinit var languageSpinner: Spinner
    private lateinit var defaultAlgorithmSpinner: Spinner
    private lateinit var defaultModeSpinner: Spinner
    private lateinit var defaultPaddingSpinner: Spinner
    private lateinit var multithreadingSwitch: SwitchMaterial
    private lateinit var bufferSizeSeekBar: SeekBar
    private lateinit var bufferSizeText: TextView
    
    private var appConfig: AppConfig = AppConfig()
    
    companion object {
        private val SUPPORTED_LANGUAGES = listOf(
            LanguageOption("en", "English"),
            LanguageOption("ko", "한국어"),
            LanguageOption("de", "Deutsch"),
            LanguageOption("ru", "Русский")
        )
        
        private val PADDING_MODES = listOf(
            "PKCS7Padding",
            "PKCS5Padding", 
            "ISO10126Padding",
            "NoPadding"
        )
        
        private val BUFFER_SIZES = listOf(4, 8, 16, 32, 64) // KB
    }
    
    data class LanguageOption(val code: String, val displayName: String) {
        override fun toString(): String = displayName
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        loadCurrentConfig()
        setupSpinners()
        setupControls()
        setupListeners()
    }
    
    private fun initializeViews(view: View) {
        languageSpinner = view.findViewById(R.id.language_spinner)
        defaultAlgorithmSpinner = view.findViewById(R.id.default_algorithm_spinner)
        defaultModeSpinner = view.findViewById(R.id.default_mode_spinner)
        defaultPaddingSpinner = view.findViewById(R.id.default_padding_spinner)
        multithreadingSwitch = view.findViewById(R.id.multithreading_switch)
        bufferSizeSeekBar = view.findViewById(R.id.buffer_size_seekbar)
        bufferSizeText = view.findViewById(R.id.buffer_size_text)
    }
    
    private fun loadCurrentConfig() {
        appConfig = AppConfig.load(requireContext())
    }
    
    private fun setupSpinners() {
        // Language spinner
        val languageAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            SUPPORTED_LANGUAGES
        )
        languageAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        languageSpinner.adapter = languageAdapter
        
        // Set current language
        val currentLocale = getCurrentLocale()
        val languageIndex = SUPPORTED_LANGUAGES.indexOfFirst { it.code == currentLocale }
        if (languageIndex >= 0) {
            languageSpinner.setSelection(languageIndex)
        }
        
        // Algorithm spinner
        val algorithmAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            CipherAlgorithm.getAlgorithmNames()
        )
        algorithmAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        defaultAlgorithmSpinner.adapter = algorithmAdapter
        
        // Set default algorithm
        val algorithmIndex = CipherAlgorithm.getAlgorithmNames().indexOf(appConfig.defaultAlgorithm)
        if (algorithmIndex >= 0) {
            defaultAlgorithmSpinner.setSelection(algorithmIndex)
        }
        
        // Mode spinner - initially populated based on selected algorithm
        updateModeSpinner()
        
        // Padding spinner
        val paddingAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            PADDING_MODES
        )
        paddingAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        defaultPaddingSpinner.adapter = paddingAdapter
        
        // Set default padding if it exists in config
        val paddingIndex = PADDING_MODES.indexOf(getDefaultPadding())
        if (paddingIndex >= 0) {
            defaultPaddingSpinner.setSelection(paddingIndex)
        }
    }
    
    private fun updateModeSpinner() {
        val selectedAlgorithmName = defaultAlgorithmSpinner.selectedItem as? String
        val algorithm = CipherAlgorithm.getAlgorithmByName(selectedAlgorithmName ?: appConfig.defaultAlgorithm)
        val modes = algorithm?.supportedModes ?: listOf("CBC")
        
        val modeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            modes
        )
        modeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        defaultModeSpinner.adapter = modeAdapter
        
        // Set default mode
        val modeIndex = modes.indexOf(appConfig.defaultMode)
        if (modeIndex >= 0) {
            defaultModeSpinner.setSelection(modeIndex)
        }
    }
    
    private fun setupControls() {
        // Multithreading switch
        multithreadingSwitch.isChecked = appConfig.enableMultithreading
        
        // Buffer size
        val bufferIndex = BUFFER_SIZES.indexOf(appConfig.bufferSize / 1024)
        if (bufferIndex >= 0) {
            bufferSizeSeekBar.progress = bufferIndex
        }
        updateBufferSizeText()
    }
    
    private fun setupListeners() {
        // Language change listener
        languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = SUPPORTED_LANGUAGES[position]
                changeLanguage(selectedLanguage.code)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Algorithm change listener
        defaultAlgorithmSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateModeSpinner()
                saveCurrentConfig()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Mode change listener
        defaultModeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                saveCurrentConfig()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Padding change listener
        defaultPaddingSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                saveCurrentConfig()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Multithreading switch
        multithreadingSwitch.setOnCheckedChangeListener { _, isChecked ->
            appConfig = appConfig.copy(enableMultithreading = isChecked)
            saveCurrentConfig()
        }
        
        // Buffer size seekbar
        bufferSizeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                updateBufferSizeText()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                val newBufferSize = BUFFER_SIZES[bufferSizeSeekBar.progress] * 1024
                appConfig = appConfig.copy(bufferSize = newBufferSize)
                saveCurrentConfig()
            }
        })
    }
    
    private fun updateBufferSizeText() {
        val bufferSizeKB = BUFFER_SIZES[bufferSizeSeekBar.progress]
        bufferSizeText.text = "$bufferSizeKB KB"
    }
    
    private fun saveCurrentConfig() {
        val selectedAlgorithm = defaultAlgorithmSpinner.selectedItem as? String ?: appConfig.defaultAlgorithm
        val selectedMode = defaultModeSpinner.selectedItem as? String ?: appConfig.defaultMode
        
        appConfig = appConfig.copy(
            defaultAlgorithm = selectedAlgorithm,
            defaultMode = selectedMode
        )
        
        AppConfig.save(requireContext(), appConfig)
    }
    
    private fun changeLanguage(languageCode: String) {
        if (getCurrentLocale() == languageCode) return
        
        // Save language preference
        requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
            .edit()
            .putString("language", languageCode)
            .apply()
        
        // Update locale
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(requireContext().resources.configuration)
        configuration.setLocale(locale)
        
        requireContext().resources.updateConfiguration(
            configuration,
            requireContext().resources.displayMetrics
        )
        
        // Restart activity to apply language changes
        requireActivity().recreate()
    }
    
    private fun getCurrentLocale(): String {
        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        return prefs.getString("language", "en") ?: "en"
    }
    
    private fun getDefaultPadding(): String {
        // For now, return PKCS7Padding as default
        // This could be extended to be configurable in AppConfig
        return "PKCS7Padding"
    }
}