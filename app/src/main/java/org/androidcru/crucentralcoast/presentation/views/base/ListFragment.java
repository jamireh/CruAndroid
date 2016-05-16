package org.androidcru.crucentralcoast.presentation.views.base;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.androidcru.crucentralcoast.data.providers.observer.CruObserver;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Reusable class for Fragments with just a RecyclerView and emptyView for when that RecyclerView
 * is empty.
 *
 * Takes care of inflating a ViewStub when the time is right as well as a SwipeRefreshLayout workaround (see below)
 */
public class ListFragment extends BaseSupportFragment implements ListHelper
{
    protected ListHelperImpl helper;

    public ListFragment()
    {
        this.helper = new ListHelperImpl();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        helper.onViewCreated(view, savedInstanceState);
    }

    @Override
    public View onEmpty(int layoutId)
    {
        return helper.onEmpty(layoutId);
    }

    @Override
    public void onNoNetwork()
    {
        helper.onNoNetwork();
    }

    @Override
    public void showContent()
    {
        helper.showContent();
    }

    @Override
    public <T> CruObserver<T> createListObserver(Context c, int emptyLayoutId, Action1<T> onNext)
    {
        return helper.createListObserver(c, emptyLayoutId, onNext);
    }

    @Override
    public <T> CruObserver<T> createListObserver(Context c, Action1<T> onNext, Action0 onEmpty)
    {
        return helper.createListObserver(c, onNext, onEmpty);
    }

    @Override
    public void inflateEmptyView(View v, int layoutId)
    {
        helper.inflateEmptyView(v, layoutId);
    }
}
