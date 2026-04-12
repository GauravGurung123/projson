package projson.reference

import java.util.Collections
import java.util.IdentityHashMap
import java.util.UUID

class ReferenceManager {
    // ✅ Identity-based (no hashCode recursion)
    private val objectToId = IdentityHashMap<Any, String>()

    private val serializedObjects = Collections.newSetFromMap(
        IdentityHashMap<Any, Boolean>()
    )
    fun getOrCreateId(obj: Any): String {
        return objectToId.getOrPut(obj) {
            UUID.randomUUID().toString()
        }
    }

    fun isSerialized(obj: Any): Boolean {
        return serializedObjects.contains(obj)
    }

    fun markSerialized(obj: Any) {
        serializedObjects.add(obj)
    }

    fun clear() {
        objectToId.clear()
        serializedObjects.clear()
    }
}