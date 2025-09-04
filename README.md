# Crypto-Ko

A powerful, extensible Android encryption/decryption application built with Kotlin, featuring GitHub-inspired UI design and support for all OpenSSL symmetric encryption algorithms.

## Features

### 🔐 Comprehensive Encryption Support
- **All OpenSSL Symmetric Algorithms**: AES (128/192/256), DES, 3DES, Blowfish, Twofish, RC4, ChaCha20, Camellia
- **All Encryption Modes**: CBC, CFB, OFB, ECB, GCM, CTR, and stream modes
- **Secure Implementation**: Uses Bouncy Castle cryptographic provider
- **File-based Operations**: Encrypt/decrypt any file type

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
│   ├── CipherAlgorithm.kt      # Algorithm definitions and metadata
│   ├── CryptoEngine.kt         # Core encryption interface
│   └── BouncyCastleCryptoEngine.kt  # Bouncy Castle implementation
├── ui/
│   └── EncryptionFragment.kt   # Main UI for encryption operations
├── utils/                      # Utility classes (future expansion)
└── MainActivity.kt             # Main activity and navigation
```

## Supported Algorithms & Modes

| Algorithm | Key Size | Supported Modes | Multithreading Support |
|-----------|----------|----------------|-----------------------|
| AES-128   | 128-bit  | CBC, CFB, OFB, ECB, GCM, CTR | ECB, CTR, OFB, GCM |
| AES-192   | 192-bit  | CBC, CFB, OFB, ECB, GCM, CTR | ECB, CTR, OFB, GCM |
| AES-256   | 256-bit  | CBC, CFB, OFB, ECB, GCM, CTR | ECB, CTR, OFB, GCM |
| DES       | 56-bit   | CBC, CFB, OFB, ECB | ECB, OFB |
| 3DES      | 168-bit  | CBC, CFB, OFB, ECB | ECB, OFB |
| Blowfish  | 128-bit  | CBC, CFB, OFB, ECB | ECB, OFB |
| Twofish   | 256-bit  | CBC, CFB, OFB, ECB | ECB, OFB |
| RC4       | 128-bit  | Stream | Single-threaded only |
| ChaCha20  | 256-bit  | Stream | Single-threaded only |
| Camellia  | 128/192/256-bit | CBC, CFB, OFB, ECB | ECB, OFB |

### Multithreading Notes
- **Parallelizable modes**: ECB, CTR, OFB, GCM can utilize multiple CPU cores
- **Sequential modes**: CBC, CFB require single-threaded processing due to block dependencies
- **Stream ciphers**: RC4, ChaCha20 are inherently sequential
- **Thread count**: User-adjustable from 1 to 2x CPU core count (max 16 threads)
- **Minimum chunk size**: 64KB per thread for effective parallelization

## Building the App

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK API 33
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

1. **Select File**: Choose any file from your device storage
2. **Choose Algorithm**: Select from the comprehensive list of encryption algorithms
3. **Select Mode**: Pick the appropriate encryption mode for your use case
4. **Set Password**: Enter a secure password for encryption/decryption
5. **Execute**: Start the encryption or decryption process
6. **Monitor**: Watch real-time progress indicators

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