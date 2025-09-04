# GitHub Actions Workflows for Crypto-Ko

This directory contains optimized GitHub Actions workflows for building, testing, and releasing the Crypto-Ko Android application with enhanced caching and manual APK builds.

## Workflows

### 1. `build-apk.yml` - Manual APK Build Workflow
**Purpose**: Builds debug and/or release APKs for the application (manual execution only for speed optimization).

**Triggers**:
- Manual dispatch only (workflow_dispatch) with build type selection

**Features**:
- Manual-only execution for optimized build speed
- Enhanced caching for Gradle dependencies and Android SDK
- Uses GitHub Actions v4
- Supports JDK 17 and Android SDK API 34
- Advanced Gradle and Android build caching
- Configurable build type (debug, release, both)
- Artifact upload with retention
- Comprehensive build reporting

**Usage**:
```bash
# Manual trigger via GitHub UI only:
# Go to Actions tab → Build Android APK → Run workflow
# Select build type: debug, release, or both
```

### 2. `release.yml` - Signed Release Build Workflow
**Purpose**: Builds signed release APKs for distribution.

**Triggers**:
- Git tags starting with 'v' (e.g., v1.0.0)
- Manual dispatch

**Features**:
- Enhanced caching for faster builds (Gradle + Android SDK)
- Automatic keystore setup from secrets
- APK signing for release builds
- GitHub Release creation with APK attachment
- Extended artifact retention (90 days)
- Optimized build performance

**Setup Required**:
Add these secrets in your GitHub repository settings:
- `KEYSTORE_BASE64`: Base64 encoded keystore file
- `KEYSTORE_PASSWORD`: Keystore password
- `KEY_ALIAS`: Key alias
- `KEY_PASSWORD`: Key password

**Usage**:
```bash
# Create a release tag
git tag v1.0.0
git push origin v1.0.0

# Or run manually from GitHub Actions UI
```

### 3. `test.yml` - Testing and Quality Checks
**Purpose**: Runs unit tests and code quality checks.

**Triggers**:
- Push to `main` or `develop` branches
- Pull requests to `main`

**Features**:
- Unit test execution
- Android Lint checks
- Test report uploads
- Quality gate validation

## Requirements

### System Requirements
- **JDK**: 17 (Temurin distribution)
- **Android SDK**: API 34
- **Build Tools**: 34.0.0
- **Gradle**: 8.3 (via wrapper)

### Build Configuration
The workflows are configured to work with:
- Target SDK: 34
- Minimum SDK: 24
- Kotlin: 1.9.10
- Android Gradle Plugin: 8.1.4

## Caching Strategy

The workflows now implement an enhanced caching strategy to optimize build speeds:

### Gradle Caching
- Gradle caches (`~/.gradle/caches`)
- Gradle wrapper (`~/.gradle/wrapper`) 
- Gradle daemon (`~/.gradle/daemon`)
- Build cache (`~/.android/build-cache`)
- Project-specific Gradle files

### Android SDK Caching
- Android SDK components (`$ANDROID_HOME`)
- AVD configurations (`~/.android/avd/*`)
- Build tools and platform tools

### Cache Keys
- **Debug/Release builds**: Uses enhanced keys based on Gradle files and properties
- **Fallback strategy**: Multiple restore-keys for maximum cache hit rate
- **SDK versioning**: Separate cache keys for different SDK versions

## Manual Execution Strategy

### Why Manual-Only APK Builds?

The APK build workflows (`build-apk.yml` and `release.yml`) are configured for **manual execution only** to:

1. **Optimize Build Speed**: Enhanced caching works best with manual triggers
2. **Resource Efficiency**: Avoid unnecessary builds on every commit
3. **Cost Control**: Reduce GitHub Actions minute usage
4. **Quality Control**: Deliberate APK generation process

### Automatic vs Manual Workflows

- **Automatic (CI)**: `test.yml` - Runs tests and quality checks on push/PR
- **Manual (APK Builds)**: `build-apk.yml` and `release.yml` - Manual APK generation only

This separation ensures fast feedback for code quality while keeping APK builds optimized and intentional.

## Keystore Setup (for Release Builds)

To enable signed release builds, you need to set up a keystore:

1. **Generate a keystore** (if you don't have one):
```bash
keytool -genkey -v -keystore crypto-ko.keystore -alias crypto-ko -keyalg RSA -keysize 2048 -validity 10000
```

2. **Encode keystore to base64**:
```bash
base64 crypto-ko.keystore | tr -d '\n' > keystore.base64
```

3. **Add secrets to GitHub repository**:
   - Go to repository Settings → Secrets and variables → Actions
   - Add the following secrets:
     - `KEYSTORE_BASE64`: Content of keystore.base64 file
     - `KEYSTORE_PASSWORD`: Your keystore password
     - `KEY_ALIAS`: Your key alias (e.g., crypto-ko)
     - `KEY_PASSWORD`: Your key password

## Troubleshooting

### Common Issues

**Build fails with "No address associated with hostname"**:
- This is a network connectivity issue in CI
- Usually resolves itself on retry
- Check GitHub Actions status page

**Gradle daemon issues**:
- Workflows disable Gradle daemon for stability
- Memory limits are set to prevent OOM errors

**Missing SDK components**:
- All required SDK components are automatically installed
- Licenses are accepted automatically

### Debugging Tips

1. **Check build logs**: Look at the detailed logs in GitHub Actions
2. **Artifact downloads**: Download and inspect generated APKs
3. **Test reports**: Review uploaded test reports for failures
4. **Gradle cache**: Clear cache if builds become inconsistent

## Customization

### Modifying Build Types
Edit `build-apk.yml` to add custom build variants:

```yaml
- name: Build Custom Variant
  run: ./gradlew assembleCustom --stacktrace --no-daemon
```

### Changing Android SDK Version
Update the `api-level` and `build-tools` in all workflow files:

```yaml
- name: Setup Android SDK
  uses: android-actions/setup-android@v3
  with:
    api-level: 34  # Change this
    build-tools: 35.0.0  # And this
```

### Adding Custom Steps
You can add additional steps like:
- Code coverage reporting
- Security scanning
- Performance testing
- APK analysis

## Support

For issues related to:
- **Workflow configuration**: Check this documentation and GitHub Actions logs
- **Build errors**: Review Gradle build logs and project configuration
- **Android-specific issues**: Check Android SDK and build tools compatibility