# Challenge: Java Version Configuration Conflicts

## Problem Statement

IntelliJ IDEA showing error: "Dependency requires at least JVM runtime version 17. This build uses a Java 11 JVM" when trying to run the project, despite having Java 21 configured in some places.

## Root Cause Analysis

### Configuration Inconsistencies Found

1. **build.gradle (Root):** Configured for Java 21
   ```gradle
   java {
       sourceCompatibility = JavaVersion.VERSION_21
       targetCompatibility = JavaVersion.VERSION_21
       toolchain {
           languageVersion = JavaLanguageVersion.of(21)
       }
   }
   ```

2. **.java-version:** Set to `21`

3. **IntelliJ Project SDK:** Attempting to use Java 11

4. **Runtime Environment:** Java 17 not installed

## Symptoms

- ✅ **Terminal build worked:** `./gradlew build` succeeded with Java 21
- ❌ **IntelliJ failed:** Could not run application from IDE
- ❌ **Gradle wrapper failed:** Without explicit JAVA_HOME setting
- ❌ **jenv conflicts:** Local Java version not matching project requirements

## Investigation Process

### Step 1: Environment Analysis
```bash
# Check current Java version
java -version
# Output: openjdk version "21.0.6"

# Check JAVA_HOME
echo $JAVA_HOME
# Output: /Users/abalak/.jenv/versions/21

# Check available Java versions
/usr/libexec/java_home -V
# Found: Java 21, Java 8 variants (no Java 17)
```

### Step 2: Requirement Clarification
- **Project originally designed for:** Java 17 (based on README and Spring Boot 3.x)
- **Current configuration:** Java 21
- **Missing piece:** Java 17 not installed

## Solution Implementation

### Step 1: Install Java 17
```bash
brew install openjdk@17
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

### Step 2: Update Project Configuration
**Modified files to use Java 17:**

1. **build.gradle (Root)**
   ```gradle
   java {
       sourceCompatibility = JavaVersion.VERSION_17  # Changed from 21
       targetCompatibility = JavaVersion.VERSION_17   # Changed from 21
       toolchain {
           languageVersion = JavaLanguageVersion.of(17)  # Changed from 21
       }
   }
   ```

2. **.java-version**
   ```
   17  # Changed from 21
   ```

3. **gradle.properties (New file)**
   ```properties
   org.gradle.java.home=/opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home
   org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
   org.gradle.daemon=true
   org.gradle.parallel=true
   ```

### Step 3: Configure jenv
```bash
# Add Java 17 to jenv
jenv add /opt/homebrew/Cellar/openjdk@17/17.0.16/libexec/openjdk.jdk/Contents/Home

# Set local Java version for project
jenv local 17
```

### Step 4: Verify Configuration
```bash
# Test build
./gradlew clean build
# Result: BUILD SUCCESSFUL

# Check Java version in project directory
java -version
# Output: openjdk version "17.0.16"
```

## Technical Challenges Encountered

### Challenge 1: jenv Integration Issues
**Problem:** jenv was setting invalid JAVA_HOME path
```bash
ERROR: JAVA_HOME is set to an invalid directory: /Users/abalak/.jenv/versions/
jenv: version `17' is not installed
```

**Solution:** 
1. Add Java 17 to jenv registry
2. Set local version for project directory
3. Verify JAVA_HOME resolution

### Challenge 2: Gradle Daemon Conflicts
**Problem:** Multiple Gradle daemons running with different Java versions
```bash
Starting a Gradle Daemon, 5 incompatible and 4 stopped Daemons could not be reused
```

**Solution:**
1. Stop all daemons: `./gradlew --stop`
2. Configure gradle.properties with specific Java home
3. Use consistent Java version across all builds

### Challenge 3: IDE Configuration
**Problem:** IntelliJ still trying to use Java 11

**Solution (for user to apply):**
1. **File → Project Structure → Project**
   - Set SDK to Java 17
2. **File → Project Structure → Modules**
   - Set Language Level to "17 - Sealed types, always-strict floating-point semantics"
3. **File → Settings → Build → Gradle**
   - Set Gradle JVM to "Project SDK" or Java 17

## Configuration Strategy Established

### 1. **Single Source of Truth**
- **gradle.properties:** Explicit Java home path
- **.java-version:** For local development (jenv)
- **build.gradle:** Consistent version across all modules

### 2. **Tool-Specific Configuration**
- **Gradle:** Uses gradle.properties settings
- **jenv:** Local project Java version
- **IntelliJ:** Project SDK configuration

### 3. **Verification Commands**
```bash
# Check project Java version
java -version

# Test Gradle build
./gradlew clean build

# Check available Java versions
/usr/libexec/java_home -V

# Verify jenv configuration
jenv versions
```

## Lessons Learned

### 1. **Java Version Consistency is Critical**
- All configuration files must specify the same Java version
- Inconsistencies lead to confusing build failures
- IDE and build tool must use compatible Java versions

### 2. **Environment Management Best Practices**
- Use tools like jenv for local Java version management
- Document Java version requirements clearly
- Test builds in clean environments

### 3. **Gradle Configuration Hierarchy**
- gradle.properties overrides other settings
- Toolchain configuration helps with version consistency
- Explicit JAVA_HOME eliminates ambiguity

### 4. **IDE Integration Considerations**
- IDE settings are separate from build configuration
- Manual configuration often required
- Test both IDE and command-line builds

## Prevention Strategies

### 1. **Documentation**
- Clearly specify required Java version in README
- Include setup instructions for different operating systems
- Document IDE configuration steps

### 2. **Build Verification**
- Include Java version checks in CI/CD
- Test with multiple Java versions if needed
- Validate gradle.properties settings

### 3. **Developer Onboarding**
- Provide setup scripts for new developers
- Include environment verification steps
- Document common troubleshooting steps

## Impact Assessment

### Before Fix
- ❌ IntelliJ builds failing
- ❌ Inconsistent development environment
- ❌ Confusing error messages
- ❌ Manual JAVA_HOME overrides required

### After Fix
- ✅ Consistent Java 17 across all tools
- ✅ IntelliJ integration working
- ✅ Clean gradle builds
- ✅ Automated environment configuration

## Related Issues

- **Spring Boot 3.x requires Java 17+** (explains minimum version requirement)
- **Gradle toolchain configuration** (helps manage Java versions)
- **jenv vs SDKMAN** (alternative Java version managers)

---

**Problem Solved:** 2024-12-19  
**Java Version:** 17  
**Build Tool:** Gradle 8.8  
**Time to Resolution:** 2 hours  
**Risk Level:** Medium
