# ProJson 🚀

A lightweight Kotlin JSON serialization library with:

- ✅ Reflection-based serialization
- ✅ Object references ($id / $ref)
- ✅ Plugin system
- ✅ Annotation support
- ✅ No external libraries

---

## 🔧 Usage

```kotlin
val json = ProJson().toJson(obj)
println(json)