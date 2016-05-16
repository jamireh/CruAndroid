package org.androidcru.crucentralcoast.presentation.views.forms;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import org.androidcru.crucentralcoast.data.providers.observer.CruObserver;
import org.androidcru.crucentralcoast.presentation.views.base.ListHelper;
import org.androidcru.crucentralcoast.presentation.views.base.ListHelperImpl;

import rx.functions.Action0;
import rx.functions.Action1;

public abstract class FormContentListFragment extends FormContentFragment implements ListHelper
{
    private FormHolder formHolder;
    protected ListHelperImpl helper;

    public FormContentListFragment()
    {
        helper = new ListHelperImpl();
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
        formHolder = (FormActivity) context;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        ((FragmentViewListener) formHolder).onFragmentViewInstantiated(this);
    }

    @Override
    public void onNext(FormHolder formHolder)
    {
        formHolder.next();
    }

    @Override
    public void onPrevious(FormHolder formHolder)
    {
        formHolder.prev();
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
