package com.cryptoko.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cryptoko.R
import com.cryptoko.crypto.*
import com.cryptoko.utils.AppConfig
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class EncryptionFragment : Fragment() {
    
    private lateinit var fileCard: MaterialCardView
    private lateinit var filePathText: TextView
    private lateinit var selectFileButton: MaterialButton
    private lateinit var algorithmSpinner: Spinner
    private lateinit var modeSpinner: Spinner
    private lateinit var keySizeSpinner: Spinner
    private lateinit var paddingSpinner: Spinner
    private lateinit var threadCountSeekBar: SeekBar
    private lateinit var threadCountText: TextView
    private lateinit var threadInfoText: TextView
    private lateinit var passwordLayout: TextInputLayout
    private lateinit var passwordEdit: TextInputEditText
    private lateinit var encryptButton: MaterialButton
    private lateinit var decryptButton: MaterialButton
    private lateinit var progressIndicator: LinearProgressIndicator
    private lateinit var progressText: TextView
    
    private var selectedFileUri: Uri? = null
    private val cryptoEngine = BouncyCastleCryptoEngine()
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                updateFileDisplay(uri)
            }
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_encryption, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        initializeViews(view)
        setupSpinners()
        setupThreadControls()
        setupClickListeners()
    }
    
    private fun initializeViews(view: View) {
        fileCard = view.findViewById(R.id.file_card)
        filePathText = view.findViewById(R.id.file_path_text)
        selectFileButton = view.findViewById(R.id.select_file_button)
        algorithmSpinner = view.findViewById(R.id.algorithm_spinner)
        modeSpinner = view.findViewById(R.id.mode_spinner)
        keySizeSpinner = view.findViewById(R.id.key_size_spinner)
        paddingSpinner = view.findViewById(R.id.padding_spinner)
        threadCountSeekBar = view.findViewById(R.id.thread_count_seekbar)
        threadCountText = view.findViewById(R.id.thread_count_text)
        threadInfoText = view.findViewById(R.id.thread_info_text)
        passwordLayout = view.findViewById(R.id.password_layout)
        passwordEdit = view.findViewById(R.id.password_edit)
        encryptButton = view.findViewById(R.id.encrypt_button)
        decryptButton = view.findViewById(R.id.decrypt_button)
        progressIndicator = view.findViewById(R.id.progress_indicator)
        progressText = view.findViewById(R.id.progress_text)
    }
    
    private fun setupSpinners() {
        // Algorithm spinner - now shows base algorithms only (AES, DES, etc.)
        val algorithmAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            CipherAlgorithm.getBaseAlgorithmNames()
        )
        algorithmAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        algorithmSpinner.adapter = algorithmAdapter
        
        algorithmSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateKeySizeSpinner()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Set default to AES (which should now be first due to performance sorting)
        algorithmSpinner.setSelection(0)
        
        // Setup padding spinner
        setupPaddingSpinner()
    }
    
    private fun updateKeySizeSpinner() {
        val algorithmName = algorithmSpinner.selectedItem as String
        val keySizes = CipherAlgorithm.getKeySizesForAlgorithm(algorithmName)
        
        // Convert to display format
        val keySizeDisplays = keySizes.map { "${it}-bit" }
        
        val keySizeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            keySizeDisplays
        )
        keySizeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        keySizeSpinner.adapter = keySizeAdapter
        
        // Set default key size (prefer 256-bit, then largest available)
        val preferredIndex = when {
            keySizes.contains(256) -> keySizeDisplays.indexOf("256-bit")
            keySizes.isNotEmpty() -> keySizeDisplays.size - 1 // largest
            else -> 0
        }
        if (preferredIndex >= 0) {
            keySizeSpinner.setSelection(preferredIndex)
        }
        
        keySizeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateModeSpinner()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        // Trigger mode update
        updateModeSpinner()
    }
    
    private fun setupPaddingSpinner() {
        val paddingModes = listOf(
            "PKCS7Padding",
            "PKCS5Padding", 
            "ISO10126Padding",
            "NoPadding"
        )
        
        val paddingAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            paddingModes
        )
        paddingAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        paddingSpinner.adapter = paddingAdapter
        
        // Select PKCS7Padding as default
        paddingSpinner.setSelection(0)
    }
    
    private fun updateModeSpinner() {
        val algorithmName = algorithmSpinner.selectedItem as? String ?: return
        val keySizeDisplay = keySizeSpinner.selectedItem as? String ?: return
        
        // Extract numeric key size from display string (e.g., "256-bit" -> 256)
        val keySize = keySizeDisplay.removeSuffix("-bit").toIntOrNull() ?: return
        
        val modes = CipherAlgorithm.getModesForAlgorithm(algorithmName, keySize)
        
        val modeAdapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            modes
        )
        modeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        modeSpinner.adapter = modeAdapter
        
        // Set default to CBC if available, otherwise first mode
        val defaultIndex = when {
            modes.contains("CBC") -> modes.indexOf("CBC")
            modes.isNotEmpty() -> 0
            else -> -1
        }
        if (defaultIndex >= 0) {
            modeSpinner.setSelection(defaultIndex)
        }
    }
    
    private fun setupThreadControls() {
        // Initialize with default thread count
        val maxThreads = AppConfig.getMaxThreadCount()
        val defaultThreads = AppConfig.getDefaultThreadCount()
        
        threadCountSeekBar.max = maxThreads - 1
        threadCountSeekBar.progress = defaultThreads - 1
        threadCountText.text = defaultThreads.toString()
        
        threadCountSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val threadCount = progress + 1
                threadCountText.text = threadCount.toString()
                updateThreadInfo()
            }
            
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Update info text when mode changes
        modeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateThreadInfo()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        
        updateThreadInfo()
    }
    
    private fun updateThreadInfo() {
        val mode = modeSpinner.selectedItem as? String ?: ""
        val threadCount = threadCountSeekBar.progress + 1
        
        val supportsMultithreading = when (mode) {
            "ECB", "CTR", "OFB", "GCM" -> true
            else -> false
        }
        
        val infoText = if (supportsMultithreading) {
            "✅ Multithreading available for $mode mode ($threadCount threads)"
        } else {
            "⚠️ $mode mode uses single-threaded processing"
        }
        
        threadInfoText.text = infoText
    }
    
    private fun setupClickListeners() {
        selectFileButton.setOnClickListener {
            openFilePicker()
        }
        
        encryptButton.setOnClickListener {
            performEncryption()
        }
        
        decryptButton.setOnClickListener {
            performDecryption()
        }
    }
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        filePickerLauncher.launch(intent)
    }
    
    private fun updateFileDisplay(uri: Uri) {
        val fileName = com.cryptoko.utils.FileUtils.getFileName(requireContext(), uri)
        val fileSize = com.cryptoko.utils.FileUtils.getFileSize(requireContext(), uri)
        val formattedSize = com.cryptoko.utils.FileUtils.formatFileSize(fileSize)
        
        filePathText.text = "${fileName ?: "Selected file"} ($formattedSize)"
        fileCard.visibility = View.VISIBLE
    }
    
    private fun getFileName(uri: Uri): String? {
        return requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        }
    }
    
    private fun validateInputs(): String? {
        if (selectedFileUri == null) {
            return getString(R.string.file_not_selected)
        }
        
        val password = passwordEdit.text.toString()
        
        if (password.isEmpty()) {
            return getString(R.string.password_empty)
        }
        
        return null
    }
    
    private fun performEncryption() {
        val validationError = validateInputs()
        if (validationError != null) {
            showError(validationError)
            return
        }
        
        // Get separate components
        val algorithmName = algorithmSpinner.selectedItem as String
        val keySizeDisplay = keySizeSpinner.selectedItem as String
        val keySize = keySizeDisplay.removeSuffix("-bit").toIntOrNull() ?: return
        val mode = modeSpinner.selectedItem as String
        val password = passwordEdit.text.toString()
        
        // Create algorithm from components
        val algorithm = CipherAlgorithm.createFromComponents(algorithmName, keySize)
        if (algorithm == null) {
            showError("Invalid algorithm configuration: $algorithmName with $keySize-bit key")
            return
        }
        
        val inputPath = getFilePathFromUri(selectedFileUri!!) ?: return
        val outputPath = "$inputPath.enc"
        
        val config = CryptoConfig(
            algorithm = algorithm,
            mode = mode,
            password = password,
            inputFile = inputPath,
            outputFile = outputPath,
            threadCount = threadCountSeekBar.progress + 1,
            enableMultithreading = true
        )
        
        startCryptoOperation(config, true)
    }
    
    private fun performDecryption() {
        val validationError = validateInputs()
        if (validationError != null) {
            showError(validationError)
            return
        }
        
        // Get separate components
        val algorithmName = algorithmSpinner.selectedItem as String
        val keySizeDisplay = keySizeSpinner.selectedItem as String
        val keySize = keySizeDisplay.removeSuffix("-bit").toIntOrNull() ?: return
        val mode = modeSpinner.selectedItem as String
        val password = passwordEdit.text.toString()
        
        // Create algorithm from components
        val algorithm = CipherAlgorithm.createFromComponents(algorithmName, keySize)
        if (algorithm == null) {
            showError("Invalid algorithm configuration: $algorithmName with $keySize-bit key")
            return
        }
        
        val inputPath = getFilePathFromUri(selectedFileUri!!) ?: return
        val outputPath = inputPath.removeSuffix(".enc")
        
        val config = CryptoConfig(
            algorithm = algorithm,
            mode = mode,
            password = password,
            inputFile = inputPath,
            outputFile = outputPath,
            threadCount = threadCountSeekBar.progress + 1,
            enableMultithreading = true
        )
        
        startCryptoOperation(config, false)
    }
    
    private fun startCryptoOperation(config: CryptoConfig, isEncryption: Boolean) {
        setOperationInProgress(true)
        
        lifecycleScope.launch {
            val result = if (isEncryption) {
                cryptoEngine.encrypt(config) { progress ->
                    requireActivity().runOnUiThread {
                        updateProgress(progress)
                    }
                }
            } else {
                cryptoEngine.decrypt(config) { progress ->
                    requireActivity().runOnUiThread {
                        updateProgress(progress)
                    }
                }
            }
            
            requireActivity().runOnUiThread {
                setOperationInProgress(false)
                handleCryptoResult(result)
            }
        }
    }
    
    private fun updateProgress(progress: CryptoResult.Progress) {
        progressIndicator.progress = progress.percentage
        
        val messageBuilder = StringBuilder(progress.message)
        
        // Add block information if available
        if (progress.totalBlocks > 0) {
            messageBuilder.append("\nBlock ${progress.currentBlock}/${progress.totalBlocks}")
        }
        
        // Add byte information if available
        if (progress.totalBytes > 0) {
            val mbProcessed = progress.bytesProcessed / (1024 * 1024)
            val mbTotal = progress.totalBytes / (1024 * 1024)
            messageBuilder.append("\n${mbProcessed}MB / ${mbTotal}MB")
        }
        
        progressText.text = messageBuilder.toString()
    }
    
    private fun setOperationInProgress(inProgress: Boolean) {
        encryptButton.isEnabled = !inProgress
        decryptButton.isEnabled = !inProgress
        selectFileButton.isEnabled = !inProgress
        algorithmSpinner.isEnabled = !inProgress
        modeSpinner.isEnabled = !inProgress
        keySizeSpinner.isEnabled = !inProgress
        paddingSpinner.isEnabled = !inProgress
        passwordEdit.isEnabled = !inProgress
        
        progressIndicator.visibility = if (inProgress) View.VISIBLE else View.GONE
        progressText.visibility = if (inProgress) View.VISIBLE else View.GONE
        
        if (!inProgress) {
            progressIndicator.progress = 0
            progressText.text = ""
        }
    }
    
    private fun handleCryptoResult(result: CryptoResult) {
        when (result) {
            is CryptoResult.Success -> {
                showSuccess(result.message)
            }
            is CryptoResult.Error -> {
                showError(result.error)
            }
            else -> {
                // Handle other cases if needed
            }
        }
    }
    
    private fun showSuccess(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Success")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun showError(message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
    
    private fun getFilePathFromUri(uri: Uri): String? {
        // Use improved file utilities for better handling
        return try {
            val fileName = com.cryptoko.utils.FileUtils.getFileName(requireContext(), uri) ?: "temp_file"
            com.cryptoko.utils.FileUtils.copyUriToInternalStorage(requireContext(), uri, fileName)
        } catch (e: Exception) {
            showError("Failed to access file: ${e.message}")
            null
        }
    }
}