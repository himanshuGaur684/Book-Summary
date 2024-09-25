package gaur.himanshu.booksummary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import gaur.himanshu.booksummary.ui.theme.BookSummaryTheme

class MainActivity : ComponentActivity() {

    private val fileManager by lazy { ScopeStorageManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BookSummaryTheme {
                Surface(modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize()) {
                    MainScreen(fileManager = fileManager)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BookSummaryTheme {
        Greeting("Android")
    }
}