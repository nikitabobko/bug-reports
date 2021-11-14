//package bobko.todomail.login
//
//import com.google.api.client.auth.oauth2.Credential
//import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
//import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
//import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
//import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
//import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
//import com.google.api.client.http.javanet.NetHttpTransport
//import com.google.api.client.json.JsonFactory
//import com.google.api.client.json.jackson2.JacksonFactory
//import com.google.api.client.util.store.FileDataStoreFactory
//import com.google.api.services.gmail.Gmail
//import com.google.api.services.gmail.GmailScopes
//import com.google.api.services.gmail.model.Label
//import java.io.File
//import java.io.FileNotFoundException
//import java.io.InputStreamReader
//import java.util.*
//
//class GmailViaGoogleLogin {
//}
//
//object GmailQuickstart {
//    private const val APPLICATION_NAME = "Gmail API Java Quickstart"
//    private val JSON_FACTORY: JsonFactory = JacksonFactory.getDefaultInstance()
//    private const val TOKENS_DIRECTORY_PATH = "tokens"
//
//    /**
//     * Global instance of the scopes required by this quickstart.
//     * If modifying these scopes, delete your previously saved tokens/ folder.
//     */
//    private val SCOPES: List<String> = Collections.singletonList(GmailScopes.GMAIL_LABELS)
//    private const val CREDENTIALS_FILE_PATH = "/google-services.json"
//
//    /**
//     * Creates an authorized Credential object.
//     * @param HTTP_TRANSPORT The network HTTP Transport.
//     * @return An authorized Credential object.
//     * @throws IOException If the credentials.json file cannot be found.
//     */
//    private fun getCredentials(HTTP_TRANSPORT: NetHttpTransport): Credential {
//        // Load client secrets.
//        val `in` = GmailQuickstart::class.java.getResourceAsStream(CREDENTIALS_FILE_PATH)
//            ?: throw FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH)
//        val clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, InputStreamReader(`in`))
//
//        // Build flow and trigger user authorization request.
//        val flow = GoogleAuthorizationCodeFlow.Builder(
//            HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES
//        )
//            .setDataStoreFactory(FileDataStoreFactory(File(TOKENS_DIRECTORY_PATH)))
//            .setAccessType("offline")
//            .build()
//        val receiver = LocalServerReceiver.Builder().setPort(8888).build()
//        return AuthorizationCodeInstalledApp(flow, receiver).authorize("user")
//    }
//
//    fun foo() {
//        // Build a new authorized API client service.
//        val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
//        val service = Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
//            .setApplicationName(APPLICATION_NAME)
//            .build()
//
//        // Print the labels in the user's account.
//        val user = "me"
//        val listResponse = service.users().labels().list(user).execute()
//        val labels: List<Label> = listResponse.labels
//        if (labels.isEmpty()) {
//            println("No labels found.")
//        } else {
//            println("Labels:")
//            for (label in labels) {
//                System.out.printf("- %s\n", label.getName())
//            }
//        }
//    }
//}
