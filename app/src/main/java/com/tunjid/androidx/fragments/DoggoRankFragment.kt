package com.tunjid.androidx.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.util.Pair
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.tunjid.androidx.PlaceHolder
import com.tunjid.androidx.R
import com.tunjid.androidx.adapters.DoggoInteractionListener
import com.tunjid.androidx.adapters.withPaddedAdapter
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.content.themeColorAt
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.model.Doggo
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.recyclerview.ListManager
import com.tunjid.androidx.recyclerview.ListManager.Companion.SWIPE_DRAG_ALL_DIRECTIONS
import com.tunjid.androidx.recyclerview.ListManagerBuilder
import com.tunjid.androidx.recyclerview.SwipeDragOptions
import com.tunjid.androidx.recyclerview.adapterOf
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.view.util.hashTransitionName
import com.tunjid.androidx.view.util.inflate
import com.tunjid.androidx.viewholders.DoggoRankViewHolder
import com.tunjid.androidx.viewholders.DoggoViewHolder
import com.tunjid.androidx.viewmodels.DoggoRankViewModel
import com.tunjid.androidx.viewmodels.routeName
import kotlin.math.abs

class DoggoRankFragment : AppBaseFragment(R.layout.fragment_simple_list),
        Navigator.TransactionModifier,
        DoggoInteractionListener {

    override val insetFlags: InsetFlags = InsetFlags.ALL

    private val viewModel by viewModels<DoggoRankViewModel>()

    private lateinit var listManager: ListManager<DoggoRankViewHolder, PlaceHolder.State>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.watchDoggos().observe(this) { listManager.onDiff(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName,
                toolBarMenu = 0,
                toolbarShows = true,
                fabText = getString(R.string.reset_doggos),
                fabIcon = R.drawable.ic_restore_24dp,
                fabShows = true,
                showsBottomNav = true,
                lightStatusBar = !requireContext().isDarkTheme,
                fabExtended = if (savedInstanceState == null) true else uiState.fabExtended,
                navBarColor = requireContext().themeColorAt(R.attr.nav_bar_color),
                fabClickListener = View.OnClickListener { viewModel.resetList() }
        )

        val placeHolder = PlaceHolder(view.findViewById(R.id.placeholder_container))

        listManager = ListManagerBuilder<DoggoRankViewHolder, PlaceHolder.State>()
                .withRecyclerView(view.findViewById(R.id.recycler_view))
                .withPaddedAdapter(
                        adapterOf(
                                itemsSource = viewModel::doggos,
                                viewHolderCreator = { parent, _ -> DoggoRankViewHolder(parent.inflate(R.layout.viewholder_doggo_rank), this) },
                                viewHolderBinder = { viewHolder, doggo, _ -> viewHolder.bind(doggo) },
                                itemIdFunction = { it.hashCode().toLong() }
                        )
                )
                .addScrollListener { _, dy -> if (abs(dy) > 4) uiState = uiState.copy(fabExtended = dy < 0) }
                .withPlaceholder(placeHolder)
                .withLinearLayoutManager()
                .withSwipeDragOptions(
                        SwipeDragOptions(
                                itemViewSwipeSupplier = { true },
                                longPressDragSupplier = { true },
                                swipeConsumer = { holder, _ -> removeDoggo(holder) },
                                dragConsumer = this::moveDoggo,
                                dragHandleFunction = DoggoRankViewHolder::dragView,
                                movementFlagFunction = { SWIPE_DRAG_ALL_DIRECTIONS },
                                swipeDragStartConsumer = { holder, actionState -> this.onSwipeOrDragStarted(holder, actionState) },
                                swipeDragEndConsumer = { viewHolder, actionState -> this.onSwipeOrDragEnded(viewHolder, actionState) }
                        )
                )
                .build()

        postponeEnterTransition()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listManager.clear()
    }

    override fun onDoggoClicked(doggo: Doggo) {
        Doggo.transitionDoggo = doggo
        navigator.push(AdoptDoggoFragment.newInstance(doggo))
    }

    override fun onDoggoImageLoaded(doggo: Doggo) {
        if (doggo == Doggo.transitionDoggo) startPostponedEnterTransition()
    }

    @SuppressLint("CommitTransaction")
    override fun augmentTransaction(transaction: FragmentTransaction, incomingFragment: Fragment) {
        if (incomingFragment !is AdoptDoggoFragment) return

        val doggo = incomingFragment.doggo
        val holder = listManager.findViewHolderForItemId(doggo.hashCode().toLong()) ?: return

        transaction.addSharedElement(holder.thumbnail, holder.thumbnail.hashTransitionName(doggo))
    }

    private fun moveDoggo(start: DoggoViewHolder, end: DoggoViewHolder) {
        val from = start.adapterPosition
        val to = end.adapterPosition

        viewModel.swap(from, to)
        listManager.notifyItemMoved(from, to)
        listManager.notifyItemChanged(from)
        listManager.notifyItemChanged(to)
    }

    private fun removeDoggo(viewHolder: DoggoViewHolder) {
        val position = viewHolder.adapterPosition
        val minMax = viewModel.remove(position)

        listManager.notifyItemRemoved(position)
        // Only necessary to rebind views lower so they have the right position
        listManager.notifyItemRangeChanged(minMax.first, minMax.second)
    }

    private fun onSwipeOrDragStarted(holder: DoggoRankViewHolder, actionState: Int) =
            viewModel.onActionStarted(Pair(holder.itemId, actionState))

    private fun onSwipeOrDragEnded(viewHolder: DoggoViewHolder, actionState: Int) {
        val message = viewModel.onActionEnded(Pair(viewHolder.itemId, actionState))
        if (!TextUtils.isEmpty(message)) uiState = uiState.copy(snackbarText = message)

    }

    companion object {
        fun newInstance(): DoggoRankFragment = DoggoRankFragment().apply { arguments = Bundle() }
    }
}
