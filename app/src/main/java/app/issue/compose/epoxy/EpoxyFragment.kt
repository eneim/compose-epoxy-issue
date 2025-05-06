package app.issue.compose.epoxy

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composition
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.ExperimentalComposeRuntimeApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import app.issue.compose.epoxy.databinding.FragmentEpoxyBinding
import app.issue.compose.epoxy.ui.theme.ComposeEpoxyIssueTheme
import com.airbnb.epoxy.composeEpoxyModel

class EpoxyFragment : Fragment(R.layout.fragment_epoxy) {

    @ExperimentalComposeRuntimeApi
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

        binding.items.addOnChildAttachStateChangeListener(object :
            RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
                // ComposeView.children[0] is an AndroidComposeView.
                val composition = (view as? ViewGroup)?.getChildAt(0)?.composition
                // Store the `composition.addedToLifecycle` in the view tag for debugging.
                if (composition is Composition) {
                    (view.getTag(R.id.model_tag) as? Lifecycle)
                        ?: composition.addedToLifecycle?.also {
                            view.setTag(R.id.model_tag, it)
                            it.addObserver(object : LifecycleEventObserver {
                                override fun onStateChanged(
                                    source: LifecycleOwner,
                                    event: Lifecycle.Event
                                ) {
                                    Log.d(
                                        "EpoxyFragment",
                                        "[${composition}] onStateChanged(${source.fragment} / $event)"
                                    )
                                }
                            })
                        }
                }
            }

            override fun onChildViewDetachedFromWindow(view: View) = Unit
        })

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
                            debugInfoLiveData.postValue("LocalView: ${currentView.d}")

                            onDispose {
                                debugInfoLiveData.postValue("(Disposed!) LocalView: ${currentView.d}\n\n(Last seen) Composition:${composition?.info}")
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

                                    val data by remember(localLifecycle, lifecycleState) {
                                        derivedStateOf {
                                            "Item[$index]\n-----------\n\nLocalView:${currentView.d}\n\n-----------\n\nLocalLifecycleOwner:\n- [${localLifecycle.info}]"
                                        }
                                    }

                                    Text(
                                        text = data,
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
