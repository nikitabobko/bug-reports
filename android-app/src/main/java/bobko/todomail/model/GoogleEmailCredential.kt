package bobko.todomail.model

import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.tasks.RuntimeExecutionException
import java.util.concurrent.ThreadLocalRandom
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object GoogleEmailCredential : EmailCredential() {
    private const val serverClientId = "473994673878-hpbjfm51euanc0molpthbesm82u3eatl.apps.googleusercontent.com"

    override fun sendEmail(activity: ComponentActivity, to: String, subject: String, body: String) {
        TODO("Not yet implemented")
    }

    suspend fun signIn(activity: ComponentActivity): GoogleSignInAccount {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .setLogSessionId(ThreadLocalRandom.current().nextInt().toString())
            .requestServerAuthCode(serverClientId)
            .requestScopes(Scope("https://www.googleapis.com/auth/gmail.send"))
            .requestEmail()
            .build()

        return suspendCoroutine { continuation ->
            activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                GoogleSignIn.getSignedInAccountFromIntent(activityResult.data)
                    .addOnCompleteListener { task ->
                        try {
                            continuation.resume(task.result)
                        } catch (ex: RuntimeExecutionException) {
                            continuation.resumeWithException(ex)
                        }
                    }
            }.launch(GoogleSignIn.getClient(activity, gso).signInIntent)
        }
    }

    fun signOut() {

    }
}
