package bobko.todomail.model

import bobko.todomail.util.IndexedPrefKey
import bobko.todomail.util.PrefReaderContext
import bobko.todomail.util.PrefWriterContext

data class SendReceiveRoute(
    val label: String,
    val sendTo: String,
    val credential: SmtpCredential
) {
    companion object {
        private val srrLabel: IndexedPrefKey<String> by IndexedPrefKey.delegate()
        private val srrSendTo: IndexedPrefKey<String> by IndexedPrefKey.delegate()

        fun read(readerContext: PrefReaderContext, index: Int): SendReceiveRoute? =
            with(readerContext) {
                SendReceiveRoute(
                    srrLabel[index] ?: return null,
                    srrSendTo[index] ?: return null,
                    SmtpCredential.read(this, index) ?: return null
                )
            }

        fun write(writerContext: PrefWriterContext, index: Int, sendReceiveRoute: SendReceiveRoute?) {
            with(writerContext) {
                srrLabel[index] = sendReceiveRoute?.label
                srrSendTo[index] = sendReceiveRoute?.sendTo
                SmtpCredential.write(this, index, sendReceiveRoute?.credential)
            }
        }
    }
}
