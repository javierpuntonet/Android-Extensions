package com.tunjid.androidx.fragments

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.view.postDelayed
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.ChipGroup
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.drawableAt
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.core.graphics.drawable.withTint
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.material.animator.FabExtensionAnimator
import com.tunjid.androidx.uidrivers.SpeedDialClickListener
import com.tunjid.androidx.view.util.withOneShotEndListener
import com.tunjid.androidx.viewmodels.routeName

/**
 * Fragment demonstrating hiding views
 *
 *
 * Created by tj.dahunsi on 5/6/17.
 */

class SpeedDialFragment : AppBaseFragment(R.layout.fragment_speed_dial) {

    private val color
        get() = if (requireContext().isDarkTheme) Color.BLACK else Color.WHITE

    private val speedDialItems: List<Pair<CharSequence?, Drawable>>
            by lazy {
                requireActivity().run {
                    listOf(
                            getString(R.string.expand_fab).color(color) to drawableAt(R.drawable.ic_expand_24dp)
                                    ?.withTint(color)!!,
                            getString(R.string.option_1).color(color) to drawableAt(R.drawable.ic_numeric_1_outline_24dp)
                                    ?.withTint(color)!!,
                            getString(R.string.option_2).color(color) to drawableAt(R.drawable.ic_numeric_2_outline_24dp)
                                    ?.withTint(color)!!,
                            getString(R.string.option_3).color(color) to drawableAt(R.drawable.ic_numeric_3_outline_24dp)
                                    ?.withTint(color)!!,
                            getString(R.string.option_4).color(color) to drawableAt(R.drawable.ic_numeric_4_outline_24dp)
                                    ?.withTint(color)!!
                    )
                }
            }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val context = view.context

        val fab = view.findViewById<MaterialButton>(R.id.expandable_fab)
        val extender = FabExtensionAnimator(fab).apply {
            speedDialItems[1].run { updateGlyphs(first ?: "", second) }
        }

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolbarShows = true,
                toolBarMenu = 0,
                fabShows = true,
                fabExtended = true,
                fabText = getString(R.string.speed_dial),
                fabIcon = R.drawable.ic_unfold_more_24dp,
                showsBottomNav = false,
                lightStatusBar = !context.isDarkTheme,
                navBarColor = context.themeColorAt(R.attr.nav_bar_color),
                fabClickListener = SpeedDialClickListener(
                        tint = context.themeColorAt(R.attr.colorAccent),
                        items = speedDialItems,
                        runGuard = this@SpeedDialFragment::fabExtensionGuard,
                        dismissListener = {
                            when (it) {
                                null -> Unit
                                0 -> uiState = uiState.copy(fabExtended = true)
                                else -> speedDialItems[it].run {
                                    extender.updateGlyphs(first ?: "", second)
                                }
                            }
                        }
                )
        )

        fab.setOnClickListener {
            extender.isExtended = !extender.isExtended
        }


        view.findViewById<ChipGroup>(R.id.spring_stiffness).setOnCheckedChangeListener { _, checkedId ->
            extender.configureSpring {
                spring.stiffness = when (checkedId) {
                    R.id.stiffness_very_low -> SpringForce.STIFFNESS_VERY_LOW
                    R.id.stiffness_low -> SpringForce.STIFFNESS_LOW
                    R.id.stiffness_medium -> SpringForce.STIFFNESS_MEDIUM
                    R.id.stiffness_high -> SpringForce.STIFFNESS_HIGH
                    else -> SpringForce.STIFFNESS_VERY_LOW
                }
            }
        }

        view.findViewById<ChipGroup>(R.id.spring_damping).setOnCheckedChangeListener { _, checkedId ->
            extender.configureSpring {
                spring.dampingRatio = when (checkedId) {
                    R.id.damping_none -> SpringForce.DAMPING_RATIO_NO_BOUNCY
                    R.id.damping_low -> SpringForce.DAMPING_RATIO_LOW_BOUNCY
                    R.id.damping_medium -> SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY
                    R.id.damping_high -> SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                    else -> SpringForce.DAMPING_RATIO_HIGH_BOUNCY
                }
            }
        }

        view.postDelayed(2000) { if (isResumed) uiState = uiState.copy(fabExtended = false) }
    }

    private fun fabExtensionGuard(view: View): Boolean {
        if (!uiState.fabExtended) return true
        uiState = uiState.copy(
                fabExtended = false,
                fabTransitionOptions = { speedDialRecall(view) }
        )
        return false
    }

    private fun SpringAnimation.speedDialRecall(view: View) = withOneShotEndListener {
        if (uiState.fabExtended) return@withOneShotEndListener
        uiState.fabClickListener?.onClick(view)
        uiState = uiState.copy(fabTransitionOptions = null)
    }

    companion object {
        fun newInstance(): SpeedDialFragment = SpeedDialFragment().apply { arguments = Bundle() }
    }
}