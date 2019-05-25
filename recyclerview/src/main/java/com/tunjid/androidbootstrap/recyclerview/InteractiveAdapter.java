package com.tunjid.androidbootstrap.recyclerview;

import android.view.View;
import android.view.ViewGroup;

import com.tunjid.androidbootstrap.view.util.ViewUtil;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;


public abstract class InteractiveAdapter<VH extends InteractiveViewHolder, T>
        extends RecyclerView.Adapter<VH> {

    protected T adapterListener;

    public InteractiveAdapter(T adapterListener) {
        this();
        this.adapterListener = adapterListener;
    }

    protected InteractiveAdapter() { }

    protected View getItemView(@LayoutRes int res, ViewGroup parent) {
        return ViewUtil.getItemView(res, parent);
    }

    /**
     * An interface for any interaction this adapter carries
     */
    @Deprecated // Use your own type definition for this
    public interface AdapterListener {}
}
