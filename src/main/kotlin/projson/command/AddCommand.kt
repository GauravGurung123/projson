package projson.command

import projson.core.JsonObject

class AddPropertyCommand(
    private val obj: JsonObject,
    private val key: String,
    private val value: Any?
) : JsonCommand {
    override fun execute() {
        obj.setProperty(key, value)
    }
}