package com.tunjid.androidbootstrap.fragments

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.tunjid.androidbootstrap.GlobalUiController
import com.tunjid.androidbootstrap.PlaceHolder
import com.tunjid.androidbootstrap.R
import com.tunjid.androidbootstrap.UiState
import com.tunjid.androidbootstrap.activityGlobalUiController
import com.tunjid.androidbootstrap.adapters.RouteAdapter
import com.tunjid.androidbootstrap.adapters.withPaddedAdapter
import com.tunjid.androidbootstrap.baseclasses.AppBaseFragment
import com.tunjid.androidbootstrap.model.Route
import com.tunjid.androidbootstrap.recyclerview.ListManagerBuilder
import com.tunjid.androidbootstrap.view.util.InsetFlags
import com.tunjid.androidbootstrap.viewholders.RouteItemViewHolder
import com.tunjid.androidbootstrap.viewmodels.RouteViewModel

class RouteFragment : AppBaseFragment(R.layout.fragment_route), GlobalUiController, RouteAdapter.RouteAdapterListener {

    override var uiState: UiState by activityGlobalUiController()

    override val insetFlags: InsetFlags = NO_BOTTOM

    private lateinit var viewModel: RouteViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(RouteViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = getString(R.string.app_name),
                toolBarMenu = 0,
                showsToolbar = true,
                showsFab = false,
                navBarColor = ContextCompat.getColor(requireContext(), R.color.white_75)
        )

        ListManagerBuilder<RouteItemViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.recycler_view))
                .withLinearLayoutManager()
                .withPaddedAdapter(RouteAdapter(viewModel.routes, this))
                .build()
    }

    override fun onItemClicked(route: Route) {
        showFragment(when (route.destination) {
            DoggoListFragment::class.java.simpleName -> DoggoListFragment.newInstance()
            BleScanFragment::class.java.simpleName -> BleScanFragment.newInstance()
            NsdScanFragment::class.java.simpleName -> NsdScanFragment.newInstance()
            HidingViewFragment::class.java.simpleName -> HidingViewFragment.newInstance()
            SpanbuilderFragment::class.java.simpleName -> SpanbuilderFragment.newInstance()
            ShiftingTileFragment::class.java.simpleName -> ShiftingTileFragment.newInstance()
            EndlessTileFragment::class.java.simpleName -> EndlessTileFragment.newInstance()
            DoggoRankFragment::class.java.simpleName -> DoggoRankFragment.newInstance()
            else -> newInstance() // No-op, all RouteFragment instances have the same tag
        })
    }

    companion object {
        fun newInstance(): RouteFragment = RouteFragment().apply { arguments = Bundle() }
    }
}