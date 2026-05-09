# Contributing to ProJson

Thank you for your interest in contributing to ProJson! This document provides guidelines and information for contributors.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [Code Style and Standards](#code-style-and-standards)
- [Contributing Guidelines](#contributing-guidelines)
  - [Bug Reports](#bug-reports)
  - [Feature Requests](#feature-requests)
  - [Pull Requests](#pull-requests)
- [Testing](#testing)
- [Documentation](#documentation)
- [Release Process](#release-process)

---

## Getting Started

### Prerequisites

- **Kotlin**: 2.3.10 or higher
- **JDK**: Java 25 or higher
- **Gradle**: 8.0 or higher
- **Git**: For version control

### Development Tools

Recommended IDEs and tools:
- **IntelliJ IDEA**: With Kotlin plugin
- **VS Code**: With Kotlin extension
- **Gradle**: For build management

---

## Development Setup

### 1. Fork and Clone

```bash
# Fork the repository on GitHub, then clone your fork
git clone https://github.com/YOUR_USERNAME/gojson.git
cd gojson

# Add the original repository as upstream
git remote add upstream https://github.com/original-org/gojson.git
```

### 2. Build the Project

```bash
# Build the project
./gradlew build

# Run tests to ensure everything works
./gradlew test

# Run the main application to verify
./gradlew run
```

### 3. Set up IDE

#### IntelliJ IDEA

1. Open the project directory
2. Let IntelliJ import the Gradle project
3. Ensure the Kotlin plugin is enabled
4. Set up code formatting to match project standards

#### VS Code

1. Install the Kotlin extension
2. Open the project directory
3. VS Code will automatically detect the Gradle project

### 4. Verify Setup

```bash
# Run all tests
./gradlew test

# Check code formatting
./gradlew ktlintCheck

# Run a quick example
./gradlew run
```

---

## Code Style and Standards

### Kotlin Coding Conventions

Follow the official [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html) with these additional rules:

#### Naming

```kotlin
// Classes use PascalCase
class JsonSerializer
class JsonObject

// Functions and properties use camelCase
fun toJsonString()
val propertyName

// Constants use UPPER_SNAKE_CASE
const val MAX_DEPTH = 100

// Package names use lowercase
package projson.mapper
```

#### File Organization

```kotlin
// File structure
package projson.core

// Imports (grouped: stdlib, third-party, project)
import kotlin.reflect.*
import projson.context.SerializationContext
import projson.plugin.JsonPlugin

// Class definition
class JsonObject : JsonElement {
    // Properties
    private val properties = mutableMapOf<String, JsonElement>()
    
    // Constructor
    constructor() : super()
    
    // Public methods
    fun getProperty(key: String): JsonElement? = properties[key]
    
    // Private methods
    private fun validateKey(key: String) { /* ... */ }
}
```

#### Documentation

```kotlin
/**
 * Converts a Kotlin object to JSON representation.
 *
 * @param obj The object to serialize (can be null)
 * @return JsonElement representing the object
 * @throws SerializationException if serialization fails
 * @see JsonElement
 * @since 1.0
 */
fun toJson(obj: Any?): JsonElement {
    // Implementation
}
```

### Code Formatting

Use the project's `.editorconfig` and ktlint configuration:

```bash
# Format code
./gradlew ktlintFormat

# Check formatting
./gradlew ktlintCheck
```

### Git Hooks (Optional)

Set up pre-commit hooks for code quality:

```bash
# Install pre-commit hook
echo "./gradlew ktlintCheck && ./gradlew test" > .git/hooks/pre-commit
chmod +x .git/hooks/pre-commit
```

---

## Contributing Guidelines

### Bug Reports

#### Before Creating a Bug Report

1. **Search existing issues** to avoid duplicates
2. **Check if the bug is reproducible** in the latest version
3. **Create a minimal reproduction case**
4. **Gather system information** (Kotlin version, JDK version, OS)

#### Bug Report Template

```markdown
## Bug Description
Brief description of the bug

## Steps to Reproduce
1. Create a class with...
2. Call ProJson().toJson()
3. Observe the error

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- Kotlin version: 2.3.10
- JDK version: 25
- OS: Linux/macOS/Windows
- ProJson version: 1.0-SNAPSHOT

## Additional Context
Any other relevant information
```

### Feature Requests

#### Before Requesting a Feature

1. **Check the roadmap** for planned features
2. **Search existing issues** for similar requests
3. **Consider the use case** and its broader applicability
4. **Think about the API design** and backward compatibility

#### Feature Request Template

```markdown
## Feature Description
Clear description of the feature

## Use Case
Why this feature is needed

## Proposed Solution
How you envision the implementation

## API Design
Example API usage

## Alternatives Considered
Other approaches you thought about

## Additional Context
Any other relevant information
```

### Pull Requests

#### Before Creating a Pull Request

1. **Fork the repository** and create a feature branch
2. **Write tests** for your changes
3. **Update documentation** if needed
4. **Ensure all tests pass**
5. **Follow code style guidelines**

#### Branch Naming

```bash
# Feature branches
feature/json-deserialization
feature/custom-serializer-interface

# Bug fix branches
fix/circular-reference-memory-leak
fix/null-pointer-exception

# Documentation branches
docs/api-reference-update
docs/examples-guide
```

#### Pull Request Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Added tests for new functionality
- [ ] All existing tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
- [ ] CHANGELOG.md updated (if applicable)

## Related Issues
Closes #123
Related to #124
```

#### Pull Request Process

1. **Create the PR** against the `main` branch
2. **Wait for CI checks** to complete
3. **Address reviewer feedback**
4. **Keep the PR updated** with any changes
5. **Merge after approval**

---

## Testing

### Test Structure

```
src/test/kotlin/
├── projson/
│   ├── ProJsonTest.kt
│   ├── AnnotationTest.kt
│   ├── PluginTest.kt
│   ├── ReferenceTest.kt
│   └── serializer/
│       ├── PrimitiveSerializerTest.kt
│       ├── CollectionSerializerTest.kt
│       └── ObjectSerializerTest.kt
```

### Writing Tests

#### Unit Tests

```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import projson.ProJson

class ProJsonTest {
    
    @Test
    fun `should serialize simple object`() {
        // Given
        val user = User("Alice", 30)
        val proJson = ProJson()
        
        // When
        val result = proJson.toJson(user)
        
        // Then
        assertNotNull(result)
        assertTrue(result.toJsonString().contains("Alice"))
        assertTrue(result.toJsonString().contains("30"))
    }
    
    @Test
    fun `should handle null values`() {
        // Given
        val proJson = ProJson()
        
        // When
        val result = proJson.toJson(null)
        
        // Then
        assertEquals("null", result.toJsonString())
    }
}
```

#### Integration Tests

```kotlin
@Test
fun `should serialize complex object graph with circular references`() {
    // Given
    val parent = Node("parent")
    val child = Node("child")
    parent.children.add(child)
    child.children.add(parent)
    
    val proJson = ProJson()
    
    // When
    val result = proJson.toJson(parent)
    
    // Then
    val json = result.toJsonString()
    assertTrue(json.contains("\"$id\""))
    assertTrue(json.contains("\"$ref\""))
}
```

### Running Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests ProJsonTest

# Run specific test method
./gradlew test --tests "ProJsonTest.should serialize simple object"

# Run tests with coverage
./gradlew test jacocoTestReport

# Run integration tests only
./gradlew test --tests "*IntegrationTest"
```

### Test Coverage

Aim for:
- **Unit tests**: 90%+ coverage
- **Integration tests**: Core functionality
- **Edge cases**: Null handling, empty collections, circular references

---

## Documentation

### Types of Documentation

1. **API Documentation** (`docs/api.md`)
2. **Usage Examples** (`docs/examples.md`)
3. **README.md** (Project overview)
4. **Code Comments** (KDoc)
5. **CHANGELOG.md** (Version history)

### Documentation Standards

#### KDoc Comments

```kotlin
/**
 * Serializes Kotlin objects to JSON format.
 *
 * This class provides the main entry point for JSON serialization.
 * It supports custom plugins, circular reference handling, and various
 * data types including primitives, collections, and complex objects.
 *
 * @constructor Creates a new ProJson instance with default configuration
 * @see JsonPlugin For custom serialization plugins
 * @see JsonObject For JSON object manipulation
 * @since 1.0
 *
 * @example
 * ```kotlin
 * val proJson = ProJson()
 * val user = User("Alice", 30)
 * val json = proJson.toJson(user)
 * println(json.toJsonString())
 * ```
 */
class ProJson {
    // Implementation
}
```

#### README Updates

When adding features:
1. Update the features list
2. Add usage examples
3. Update installation instructions if needed
4. Update the comparison table

#### API Documentation

Keep `docs/api.md` synchronized with code changes:
- Add new classes and methods
- Update parameter descriptions
- Add deprecation notices
- Update version compatibility

---

## Release Process

### Versioning

ProJson follows [Semantic Versioning](https://semver.org/):

- **MAJOR**: Breaking changes
- **MINOR**: New features (backward compatible)
- **PATCH**: Bug fixes (backward compatible)

### Release Checklist

#### Before Release

1. **Update version** in `build.gradle.kts`
2. **Update CHANGELOG.md** with all changes
3. **Ensure all tests pass**
4. **Update documentation**
5. **Verify examples work**

#### Release Steps

```bash
# 1. Update version
./gradlew versionUpdate  # Custom task to update version

# 2. Run full test suite
./gradlew test

# 3. Build release artifacts
./gradlew build

# 4. Create Git tag
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0

# 5. Deploy to repository (if applicable)
./gradlew publish
```

#### Post-Release

1. **Update documentation website**
2. **Create GitHub release**
3. **Announce in community channels**
4. **Monitor for issues**

### Change Log Format

```markdown
# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2024-02-01

### Added
- JSON deserialization support
- Streaming API for large datasets
- Schema validation features

### Changed
- Improved performance of object serialization
- Updated plugin interface for better extensibility

### Deprecated
- Old annotation-based configuration (will be removed in 2.0)

### Fixed
- Memory leak in circular reference handling
- Null pointer exception with empty collections

### Security
- Fixed potential injection vulnerability in custom plugins

## [1.0.0] - 2024-01-15

### Added
- Initial release of ProJson
- Object to JSON serialization
- Custom plugin system
- Circular reference handling
- Comprehensive annotation support
```

---

## Community Guidelines

### Code of Conduct

Be respectful and inclusive:
- Welcome contributors of all experience levels
- Provide constructive feedback
- Help others learn and grow
- Focus on what is best for the community

### Getting Help

- **GitHub Issues**: For bug reports and feature requests
- **GitHub Discussions**: For questions and general discussion
- **Documentation**: Check existing docs first
- **Examples**: Review usage examples

### Recognition

Contributors are recognized in:
- README.md contributors section
- Release notes
- GitHub contributor statistics

---

## Development Tips

### Performance Considerations

When working on performance:

```kotlin
// Use benchmarks for performance testing
@Test
fun benchmarkSerialization() {
    val data = generateTestData(10000)
    val proJson = ProJson()
    
    val startTime = System.nanoTime()
    repeat(100) {
        proJson.toJson(data)
    }
    val endTime = System.nanoTime()
    
    val avgTime = (endTime - startTime) / 100 / 1_000_000 // ms
    println("Average serialization time: ${avgTime}ms")
}
```

### Debugging Tips

```kotlin
// Use logging for debugging
class DebugSerializer : JsonSerializer {
    override fun canSerialize(obj: Any?): Boolean {
        println("Checking if ${obj?.let { it::class.simpleName }} can be serialized")
        return obj is String
    }
    
    override fun serialize(obj: Any?, context: SerializationContext): JsonElement {
        println("Serializing: $obj")
        return JsonPrimitive(obj.toString())
    }
}
```

### Plugin Development

```kotlin
// Template for new plugins
class TemplatePlugin : JsonPlugin {
    override fun supports(clazz: Class<*>): Boolean {
        // Check if plugin supports the class
        return YourClass::class.java.isAssignableFrom(clazz)
    }
    
    override fun serialize(obj: Any): String {
        // Implement serialization logic
        return "\"${obj.toString()}\""
    }
}
```

---

## Frequently Asked Questions

### Q: How do I add a new serializer?

A: Implement the `JsonSerializer` interface and register it in the `SerializationContext`.

### Q: Can I contribute without coding?

A: Yes! We need help with documentation, examples, testing, and issue triage.

### Q: What about backward compatibility?

A: We follow semantic versioning. Breaking changes only happen in major versions.

### Q: How do I report a security issue?

A: Please report security issues privately to security@gojson.org.

---

Thank you for contributing to ProJson! Your help makes this project better for everyone.

---

*For questions not covered here, please open an issue or discussion on GitHub.*
