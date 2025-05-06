package app.issue.compose.epoxy

import android.view.View
import androidx.compose.runtime.Composition
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

//region Debugging tools
// Simple instance info.
val Any.info: String
    get() = "${this::class.java.simpleName}@${this.hashCode()}"

// Debug info for a View, used when it is part of a RecyclerView. View must be attached to Window.
val View.d: String
    get() = "\n- ${this.info},\n" +
            "- Lifecycle=${this.findViewTreeLifecycleOwner()?.info},\n" +
            "- ViewHolder=${this.vh?.info},\n" +
            "- Composition=${this.composition?.info}"

// Extract the RecyclerView that hosts this View (after it is attached to the Window).
private val View.rv: RecyclerView?
    get() {
        var parent = this.parent
        val recyclerView = when (parent) {
            is RecyclerView -> parent
            is View -> parent.rv
            else -> null
        }
        return recyclerView
    }

// Extract the ViewHolder that holds this View (after it is attached to the Window).
private val View.vh: RecyclerView.ViewHolder?
    get() = rv?.findContainingViewHolder(this)

// FragmentViewLifecycleOwner.mFragment
private val mFragmentField by lazy {
    try {
        Class.forName("androidx.fragment.app.FragmentViewLifecycleOwner")
            .getDeclaredField("mFragment")
    } catch (_: Exception) {
        null
    }
}

// Get the Fragment from the LifecycleOwner when this is a FragmentViewLifecycleOwner.
val LifecycleOwner.fragment: Fragment?
    get() = mFragmentField?.let {
        return try {
            it.isAccessible = true
            it.get(this) as? Fragment
        } catch (_: Exception) {
            null
        } finally {
            it.isAccessible = false
        }
    }

// WrappedComposition.addedToLifecycle
private val addedToLifecycleField by lazy {
    try {
        Class.forName("androidx.compose.ui.platform.WrappedComposition")
            .getDeclaredField("addedToLifecycle")
    } catch (_: Exception) {
        null
    }
}

// Get the Lifecycle from the Composition when this is a WrappedComposition.
val Composition.addedToLifecycle: Lifecycle?
    get() = addedToLifecycleField?.let {
        return try {
            it.isAccessible = true
            it.get(this) as? Lifecycle
        } catch (_: Exception) {
            null
        } finally {
            it.isAccessible = false
        }
    }

// LifecycleRegistry.lifecycleOwner
private val lifecycleOwnerField by lazy {
    try {
        Class.forName("androidx.lifecycle.LifecycleRegistry")
            .getDeclaredField("lifecycleOwner")
    } catch (_: Exception) {
        null
    }
}

private val LifecycleRegistry.internalLifecycleOwner: LifecycleOwner?
    get() = lifecycleOwnerField?.let {
        return try {
            it.isAccessible = true
            (it.get(this) as? WeakReference<*>)?.get() as? LifecycleOwner
        } catch (_: Exception) {
            null
        } finally {
            it.isAccessible = false
        }
    }

// Get the cached LifecycleOwner from the Lifecycle when this is a LifecycleRegistry.
val Lifecycle.internalLifecycleOwner: LifecycleOwner?
    get() = (this as? LifecycleRegistry)?.internalLifecycleOwner

// Get the cached Composition from the View.
val View.composition: Composition?
    get() = try {
        getTag(androidx.compose.ui.R.id.wrapped_composition_tag) as? Composition
    } catch (_: Exception) {
        null
    }

// The Fragment that this Composition is attached to.
val Composition.fragment: Any?
    get() = ((addedToLifecycle as? LifecycleRegistry)?.internalLifecycleOwner)?.fragment
//endregion
