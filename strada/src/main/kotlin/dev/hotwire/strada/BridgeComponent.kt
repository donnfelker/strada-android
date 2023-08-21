package dev.hotwire.strada

abstract class BridgeComponent<in D : BridgeDestination>(
    val name: String,
    private val delegate: BridgeDelegate<D>
) {
    private val receivedMessages = hashMapOf<String, Message>()

    internal fun didReceive(message: Message) {
        receivedMessages[message.event] = message
        onReceive(message)
    }

    internal fun didStart() {
        onStart()
    }

    internal fun didStop() {
        onStop()
    }

    /**
     * Returns the last received message for a given `event`, if available.
     */
    protected fun receivedMessageFor(event: String): Message? {
        return receivedMessages[event]
    }

    /**
     * Called when a message is received from the web bridge. Handle the
     * message for its `event` type for the custom component's behavior.
     */
    abstract fun onReceive(message: Message)

    /**
     * Called when the component's destination starts (and is active)
     * based on its lifecycle events. You can use this as an opportunity
     * to update the component's state/view.
     */
    open fun onStart() {}

    /**
     * Called when the component's destination stops (and is inactive)
     * based on its lifecycle events. You can use this as an opportunity
     * to update the component's state/view.
     */
    open fun onStop() {}

    /**
     * Reply to the web with a received message, optionally replacing its
     * `event` or `jsonData`.
     */
    fun replyWith(message: Message): Boolean {
        return reply(message)
    }

    /**
     * Reply to the web with the last received message for a given `event`
     * with its original `jsonData`.
     *
     * NOTE: If a message has not been received for the given `event`, the
     * reply will be ignored.
     */
    fun replyTo(event: String): Boolean {
        val message = receivedMessageFor(event) ?: run {
            logEvent("bridgeMessageFailedToReply", "message for event '$event' was not received")
            return false
        }

        return reply(message)
    }

    /**
     * Reply to the web with the last received message for a given `event`,
     * replacing its `jsonData`.
     *
     * NOTE: If a message has not been received for the given `event`, the
     * reply will be ignored.
     */
    fun replyTo(event: String, jsonData: String): Boolean {
        val message = receivedMessageFor(event) ?: run {
            logEvent("bridgeMessageFailedToReply", "message for event '$event' was not received")
            return false
        }

        return reply(message.replacing(jsonData = jsonData))
    }

    /**
     * Reply to the web with the last received message for a given `event`,
     * replacing its `jsonData` with encoded json from the provided `data`
     * object.
     *
     * NOTE: If a message has not been received for the given `event`, the
     * reply will be ignored.
     */
    inline fun <reified T> replyTo(event: String, data: T): Boolean {
        return replyTo(event, jsonData = StradaJsonConverter.toJson(data))
    }

    private fun reply(message: Message): Boolean {
        return delegate.replyWith(message)
    }
}
