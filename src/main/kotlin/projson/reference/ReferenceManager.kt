package projson.reference

import java.util.UUID

class ReferenceManager {

    private val objectToId = mutableMapOf<Any, String>()
    private val serializedObjects = mutableSetOf<Any>()

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