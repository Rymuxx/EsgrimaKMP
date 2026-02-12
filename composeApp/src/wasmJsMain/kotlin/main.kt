import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.example.esgrima.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(
        title = "Esgrima",
        canvasElementId = "ComposeTarget"
    ) {
        App()
    }
}
