package app.issue.compose.epoxy

import android.os.Bundle
import android.view.View
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import app.issue.compose.epoxy.databinding.FragmentEpoxyBinding
import app.issue.compose.epoxy.ui.theme.ComposeEpoxyIssueTheme
import com.airbnb.epoxy.composeEpoxyModel

class EpoxyFragment : Fragment(R.layout.fragment_epoxy) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentEpoxyBinding.bind(view)

        val debugInfoLiveData = MutableLiveData<String>("---")

        binding.debug.setContent {
            ComposeEpoxyIssueTheme {
                val debugInfo by debugInfoLiveData.observeAsState()
                Surface {
                    Text(
                        text = "RecyclerView item info:\n\n$debugInfo",
                        style = TextStyle.Default.copy(fontSize = 12.sp),
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .padding(8.dp)
                            .wrapContentHeight(),
                    )
                }
            }
        }

        binding.items.withModels {
            repeat(1) { index ->
                composeEpoxyModel(
                    id = "item-$index",
                    keys = arrayOf("item-$index"),
                    modelAction = ::add,
                    composeFunction = {
                        val currentView = LocalView.current
                        val composition = currentView.composition
                        DisposableEffect(Unit) {
                            debugInfoLiveData.postValue("LocalView: ${currentView.d}\n\nComposition:\n- ${composition?.info}\n\nComposition's container:\n- Fragment=${composition?.fragment},\n- Lifecycle[${composition?.addedToLifecycle?.internalLifecycleOwner?.info}]")

                            onDispose {
                                debugInfoLiveData.postValue("(Disposed!)\nLocalView: ${currentView.d}\n\n(Before disposal)\nComposition:\n- ${composition?.info}\n\nComposition's container:\n- Fragment=${composition?.fragment},\n- Lifecycle[${composition?.addedToLifecycle?.internalLifecycleOwner?.info}]")
                            }
                        }

                        ComposeEpoxyIssueTheme {
                            Surface {
                                Card(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .fillMaxWidth()
                                        .sizeIn(minHeight = 160.dp),
                                    shape = RoundedCornerShape(4.dp),
                                ) {

                                    val localLifecycle = LocalLifecycleOwner.current
                                    val lifecycleState by localLifecycle.lifecycle.currentStateFlow
                                        .collectAsState()

                                    Text(
                                        text = "Item[$index]\n-----------\n${currentView.d}\n\n-----------\n\nLocalLifecycleOwner: [${localLifecycle.info} / ${lifecycleState}]",
                                        style = TextStyle.Default.copy(fontSize = 12.sp),
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier
                                            .padding(4.dp)
                                            .wrapContentHeight(),
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    override fun toString(): String {
        return "EpoxyFragment[pos=${requireArguments().getInt(ARG_POSITION)}]"
    }

    companion object {
        private const val ARG_POSITION = "position"
        fun newInstance(position: Int): EpoxyFragment {
            return EpoxyFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POSITION, position)
                }
            }
        }
    }
}
