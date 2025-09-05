# Crypto-Ko

A powerful, extensible Android encryption/decryption application built with Kotlin, featuring GitHub-inspired UI design and support for all OpenSSL symmetric encryption algorithms.

## Features

### 🔐 Enhanced Cryptographic Algorithm Support (Beyond OpenSSL)
- **Comprehensive Algorithm Database**: 40+ algorithm families including AES, ChaCha20, XSalsa20, ARIA, Camellia, Twofish, Serpent, HC128/256, ZUC-128/256, Blowfish, SEED, IDEA, CAST5/6, SM4, DSTU7624, GOST3412, NOEKEON, SHACAL-2, Grain128, VMPC, Threefish, and more
- **International Standards**: Chinese (ZUC, SM4), Korean (ARIA, SEED), Japanese (Camellia), Ukrainian (DSTU7624), Russian (GOST3412), and other national standards
- **Modern Stream Ciphers**: XSalsa20, HC128/256, ZUC-128/256, Grain128, VMPC extending beyond traditional OpenSSL offerings
- **Lightweight/IoT Ciphers**: NOEKEON, Grain128, VMPC optimized for resource-constrained environments
- **Research & Advanced Ciphers**: SHACAL-2 (SHA-256 based), Tnepres (Serpent variant), DSTU7624 (large block sizes)
- **Dynamic Key Size Detection**: Supports all key lengths offered by each algorithm (40 to 1024+ bits)
- **All Encryption Modes**: CBC, CFB, OFB, ECB, GCM, CTR, and stream modes
- **Security-Based Ranking**: Algorithms sorted by cryptographic strength with detailed security assessments
- **Provider Information**: Real-time display of available cryptographic providers
- **Bouncy Castle Integration**: Leverages BC's extensive algorithm support far beyond OpenSSL capabilities

### ⚡ High-Performance Multithreading
- **Intelligent Thread Management**: Up to 2x CPU core count with user adjustment
- **Algorithm-Aware Processing**: Automatic detection of parallelizable modes
- **Block-Based Progress Tracking**: Real-time progress with block and byte information
- **Adaptive Chunking**: Smart file splitting for optimal performance
- **Thread-Safe Operations**: Secure coordination of parallel encryption/decryption

### 🎨 GitHub-Inspired UI
- **Dark Theme**: Authentic GitHub color scheme and styling
- **Material Design 3**: Modern Android design patterns
- **Intuitive Interface**: Clean, developer-friendly layout
- **Enhanced Progress Indicators**: Real-time operation feedback with multithreading status
- **Thread Count Control**: Interactive seekbar for performance tuning
- **Algorithm Information Display**: Real-time algorithm discovery and security ranking

### 🔍 Dynamic Algorithm Discovery
- **Real-time Detection**: Automatically discovers all available cryptographic algorithms at runtime
- **Provider Integration**: Seamlessly integrates with multiple security providers (Bouncy Castle, AndroidOpenSSL, SunJCE)
- **Security Assessment**: Real-time security ranking and recommendations for each algorithm
- **Capability Testing**: Tests each algorithm/mode/key-size combination for actual availability
- **Refresh Capability**: Manual algorithm refresh to detect newly installed providers
- **Detailed Information**: Shows supported key sizes, modes, and provider information for each algorithm

### 🏗️ Extensible Architecture
- **Modular Design**: Clean separation of concerns
- **Multithreaded Engine**: Built-in support for parallel processing
- **Interface-Based**: Easy to extend with new crypto engines
- **Coroutines Support**: Async operations with proper lifecycle management
- **Configuration System**: Persistent settings for algorithms, modes, and threading

### 🔒 Security & Safety
- **Memory Safety**: Secure key handling and disposal
- **Permission Management**: Proper file access controls
- **Error Handling**: Comprehensive validation and user feedback
- **No Data Retention**: Passwords and keys are not stored

## Architecture

```
com.cryptoko/
├── crypto/
│   ├── AlgorithmDiscovery.kt     # Dynamic algorithm discovery system
│   ├── CipherAlgorithm.kt        # Algorithm definitions with dynamic loading
│   ├── CryptoEngine.kt           # Core encryption interface
│   └── BouncyCastleCryptoEngine.kt  # Bouncy Castle implementation
├── ui/
│   └── EncryptionFragment.kt     # Enhanced UI with algorithm information display
├── utils/                        # Utility classes for key derivation and file handling
└── MainActivity.kt               # Main activity and navigation
```

### Dynamic Algorithm Discovery System

The core innovation of this upgrade is the `AlgorithmDiscovery` class that:

- **Runtime Detection**: Queries all installed security providers for available cipher algorithms
- **Capability Testing**: Tests each algorithm/mode/key-size combination for actual functionality  
- **Security Assessment**: Provides cryptographic strength rankings based on current standards
- **Provider Integration**: Works seamlessly with Bouncy Castle, AndroidOpenSSL, SunJCE, and other providers
- **Caching**: Intelligent caching system to avoid repeated expensive discovery operations
- **Refresh Capability**: Allows manual refresh when providers are updated

## Enhanced Cryptographic Algorithm Support

The application now provides **significantly enhanced cryptographic diversity** beyond traditional OpenSSL capabilities through Bouncy Castle integration:

### Supported Algorithm Categories

**Modern High-Security Algorithms (8-10/10 security ranking):**
- AES (128/192/256-bit) - Modern standard
- ChaCha20 (256-bit) - Google's stream cipher
- ARIA (128/192/256-bit) - Korean standard
- Camellia (128/192/256-bit) - Japanese standard
- Twofish (128/192/256-bit) - AES finalist
- Serpent (128/192/256-bit) - AES finalist
- XSalsa20 (256-bit) - Extended Salsa20

**International Standards:**
- **Chinese**: ZUC-128/256, SM4
- **Korean**: ARIA, SEED  
- **Japanese**: Camellia
- **Ukrainian**: DSTU7624 (128/256/512-bit)
- **Russian**: GOST3412 (modern), GOST28147 (legacy)

**Modern Stream Ciphers:**
- XSalsa20 - Extended nonce Salsa20
- HC128/HC256 - eSTREAM finalists
- ZUC-128/256 - Chinese standard
- Grain128 - Lightweight stream cipher
- VMPC - Variable Memory Parameter Cipher

**Lightweight/IoT Optimized:**
- NOEKEON - Compact block cipher
- Grain128 - Hardware-efficient stream cipher
- VMPC - Low resource usage

**Research & Advanced Algorithms:**
- SHACAL-2 - Based on SHA-256 compression function
- DSTU7624 - Ukrainian standard with large block sizes
- Tnepres - Serpent variant
- Threefish - Part of Skein hash family

### Security-Based Algorithm Ranking

All algorithms are ranked on a 1-10 security scale:
- **10/10**: AES (industry standard)
- **9/10**: ChaCha20 (modern stream cipher)
- **8/10**: ARIA, Camellia (international standards)
- **7/10**: Twofish, Serpent, XSalsa20 (AES finalists & modern)
- **6/10**: SM4, Blowfish, HC256, ZUC-256, SHACAL-2, DSTU7624
- **5/10**: SEED, IDEA, GOST standards, lightweight ciphers
- **≤4/10**: Legacy algorithms (use caution)

### Multithreading Support
- **AES Family**: All key sizes (128, 192, 224, 256-bit) with all modes (CBC, CFB, OFB, ECB, GCM, CTR)
- **Modern Ciphers**: ChaCha20, ARIA, Camellia, Twofish with detected key size variants
- **International Standards**: SM4 (Chinese), SEED (Korean), GOST28147 (Russian)  
- **Legacy Support**: DES, 3DES, RC2, RC4, Blowfish, IDEA, CAST5
- **Advanced Ciphers**: Serpent, Threefish, TEA/XTEA variants (if available)

### Security-Based Algorithm Ranking
- **Excellent (8-10/10)**: AES, ChaCha20, ARIA, Camellia, Serpent
- **Good (6-7/10)**: Twofish, SM4, Blowfish, Threefish  
- **Fair (4-5/10)**: SEED, IDEA, CAST5, 3DES, RC5, RC6
- **Weak (≤3/10)**: RC2, RC4, TEA, DES (avoid for new applications)

### Multithreading Support
- **Parallelizable modes**: ECB, CTR, OFB, GCM can utilize multiple CPU cores  
- **Sequential modes**: CBC, CFB require single-threaded processing due to block dependencies
- **Stream ciphers**: RC4, ChaCha20 are inherently sequential
- **Thread count**: User-adjustable from 1 to 2x CPU core count (max 16 threads)
- **Minimum chunk size**: 64KB per thread for effective parallelization

> **Note**: Specific algorithm and mode availability depends on your device's installed security providers. The application automatically detects and displays only actually available combinations.

## Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 34
- Minimum SDK API 24 (Android 7.0)
- Kotlin 1.9.10+

### Build Steps
1. Clone the repository
2. Open in Android Studio
3. Sync Gradle dependencies
4. Build and run on device/emulator

```bash
./gradlew build
./gradlew installDebug
```

## Usage

### Basic Operation
1. **Select File**: Choose any file from your device storage
2. **Choose Algorithm**: Select from the dynamically discovered list of encryption algorithms
3. **Configure Key Size**: Pick from all available key sizes for the selected algorithm
4. **Select Mode**: Choose the appropriate encryption mode for your use case
5. **Set Password**: Enter a secure password for encryption/decryption
6. **Monitor Algorithm Info**: View real-time algorithm information and security rankings
7. **Execute**: Start the encryption or decryption process
8. **Monitor Progress**: Watch real-time progress indicators with multithreading status

### Algorithm Discovery Features
1. **View Available Algorithms**: The Algorithm Information card shows all discovered algorithms
2. **Check Security Rankings**: See real-time security assessment for selected algorithms
3. **Refresh Algorithm List**: Use the Refresh button to rediscover algorithms after provider changes
4. **Provider Information**: View which cryptographic providers are available on your device
5. **Dynamic Key Sizes**: See all supported key sizes for each algorithm family

### Example Algorithm Selection
- **Maximum Security**: AES-256-GCM, ChaCha20, XSalsa20
- **International Standards**: ARIA-256-GCM, Camellia-256-CBC, ZUC-256, DSTU7624-512
- **Balanced Performance**: AES-128-CBC, Twofish-256-CBC, Serpent-192-CBC
- **Lightweight/IoT**: NOEKEON-128-CBC, Grain128 (stream), VMPC (stream)
- **Research/Advanced**: SHACAL-2-512-CBC, Threefish-1024-CBC
- **Legacy Compatibility**: 3DES-CBC, Twofish-256-CBC
- **Avoid**: DES, RC4, TEA (marked with low security rankings)

## Future Enhancements

### Planned Features
- [ ] **Multithreading**: Parallel processing for large files
- [ ] **Batch Operations**: Encrypt/decrypt multiple files
- [ ] **Key Derivation**: PBKDF2, Argon2, and scrypt support
- [ ] **Digital Signatures**: RSA, ECDSA signature verification
- [ ] **Cloud Integration**: Support for cloud storage providers
- [ ] **Settings Page**: Customizable encryption parameters
- [ ] **About Page**: Detailed information and credits
- [ ] **File Integrity**: Checksum verification
- [ ] **Export/Import**: Configuration and key management

### Technical Improvements
- [ ] **Performance Optimization**: Streaming encryption for large files
- [ ] **Memory Management**: Optimized buffer handling
- [ ] **Testing Suite**: Comprehensive unit and integration tests
- [ ] **Benchmark Tools**: Performance measurement utilities
- [ ] **Accessibility**: Enhanced screen reader support

## Security Considerations

### Best Practices Implemented
- ✅ Secure random IV generation
- ✅ Proper key derivation from passwords
- ✅ Memory cleanup after operations
- ✅ No plaintext storage of sensitive data
- ✅ Input validation and sanitization

### Recommendations
- Use strong, unique passwords
- Prefer AES-256 with GCM mode for new encryptions
- Regularly update the app for security patches
- Backup encrypted files securely

## Dependencies

- **Bouncy Castle**: Comprehensive cryptographic library
- **Material Design**: Modern Android UI components
- **Kotlin Coroutines**: Asynchronous programming
- **AndroidX Libraries**: Core Android components

## License

MIT License - See [LICENSE](LICENSE) for details.

## Contributing

Contributions are welcome! Please read our contributing guidelines and submit pull requests for any enhancements.

## Disclaimer

This software is provided for educational and legitimate use cases only. Users are responsible for compliance with local laws and regulations regarding cryptographic software.