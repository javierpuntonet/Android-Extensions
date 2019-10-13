package com.tunjid.androidx.navigation

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

interface Navigator {

    /**
     * The id of the container this [Navigator] shows [Fragment]s in
     */
    @get:IdRes
    val containerId: Int

    /**
     * Returns the current visible Fragment the User can interact with
     */
    val currentFragment: Fragment?

    /**
     * Pops the current fragment off the stack, up until the last fragment.
     *
     * @return true if a fragment was popped, false if the stack is down to the last fragment.
     */
    fun pop(): Boolean

    /**
     * Pops the stack up to the [upToTag] value. If null is passed as the value,
     * the stack will be popped to the root [Fragment].
     * By default it doesn't pop the Fragment matching the [upToTag]; to do so, pass true for the
     * [includeMatch] parameter.
     */
    fun clear(upToTag: String? = null, includeMatch: Boolean = false)

    /**
     * Show the specified fragment
     */
    fun push(fragment: Fragment, tag: String): Boolean

    /**
     * Attempts to show the fragment provided, retrieving it from the back stack
     * if an identical instance of it already exists in the [FragmentManager] under the specified
     * tag.
     *
     * This is a convenience method for showing a [Fragment] that implements the [Navigator.TagProvider]
     * interface
     * @see push
     */
    fun <T> push(fragment: T) where T : Fragment, T : TagProvider = push(fragment, fragment.stableTag)

    /**
     * An interface to provide unique tags for [Fragment]. Fragment implementers typically delegate
     * this to a hash string of their arguments.
     *
     * It's convenient to let  Fragments implement this interface, along with [TransactionModifier].
     */
    interface TagProvider {
        val stableTag: String
    }

    /**
     * An interface for augmenting the [FragmentTransaction] that will show
     * the incoming Fragment. Implementers typically configure mappings for
     * shared element transitions, or other kinds of animations.
     *
     * It's convenient to let Fragments implement this interface, along with [TagProvider].
     */
    interface TransactionModifier {
        fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment)
    }

    /**
     * Interface for a class that hosts a [Navigator]
     */
    interface NavigationController {
        val navigator: Navigator
    }
}