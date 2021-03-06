package org.androidcru.crucentralcoast.presentation.views.events;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidcru.crucentralcoast.AppConstants;
import org.androidcru.crucentralcoast.CruApplication;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.CruEvent;
import org.androidcru.crucentralcoast.data.providers.EventProvider;
import org.androidcru.crucentralcoast.presentation.providers.CalendarProvider;
import org.androidcru.crucentralcoast.presentation.views.MainActivity;
import org.androidcru.crucentralcoast.presentation.views.base.ListFragment;
import org.androidcru.crucentralcoast.presentation.views.subscriptions.SubscriptionActivity;
import org.androidcru.crucentralcoast.util.SharedPreferencesUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.observers.Observers;
import rx.schedulers.Schedulers;

public class EventsFragment extends ListFragment
{
    private ArrayList<CruEvent> eventList;
    private LinearLayoutManager layoutManager;
    private Observer<List<CruEvent>> eventSubscriber;

    /**
     * Invoked early on from the Android framework during rendering.
     *
     * @param inflater           Object used to inflate new views, provided by Android
     * @param container          Parent view to inflate in, provided by Android
     * @param savedInstanceState State of the application if it is being refreshed, given to Android by dev
     * @return inflated View
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.list_with_empty_view, container, false);
    }

    /**
     * Invoked after onCreateView() and deals with binding view references after the
     * view has already been inflated.
     *  @param view               Inflated View created by onCreateView()
     *
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        //Due to @OnClick, this Fragment requires that the emptyView be inflated before any ButterKnife calls
        inflateEmptyView(view, R.layout.empty_events_view);
        //parent class calls ButterKnife for view injection and setups SwipeRefreshLayout
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        eventList = new ArrayList<>();

        setupObserver();

        //setup RecyclerView
        layoutManager = new LinearLayoutManager(getContext());
        helper.recyclerView.setLayoutManager(layoutManager);

        helper.swipeRefreshLayout.setOnRefreshListener(this::getCruEvents);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        getCruEvents();
    }

    private void setupObserver()
    {
        eventSubscriber = createListObserver(R.layout.empty_events_view, cruEvents -> setEvents(cruEvents));
    }

    @OnClick(R.id.subscription_button)
    public void onManageSubscriptionsClicked()
    {
        startActivity(new Intent(getContext(), SubscriptionActivity.class));
    }

    private void getCruEvents()
    {
        helper.swipeRefreshLayout.setRefreshing(true);
        EventProvider.requestUsersEvents(this, eventSubscriber);
    }


    /**
     * Updates the UI to reflect the Events in events
     *
     * @param cruEvents List of new Events the UI should adhere to
     */
    public void setEvents(List<CruEvent> cruEvents)
    {
        eventList.clear();
        rx.Observable.from(cruEvents)
                .map(cruEvent -> {
                    if (CalendarProvider.hasCalendarPermission(getContext()))
                    {
                        CalendarProvider.updateEvent(getContext(), cruEvent, SharedPreferencesUtil.getCalendarEventId(cruEvent.id), Observers.empty());
                    }
                    return cruEvent;
                })
                .subscribeOn(Schedulers.immediate())
                .subscribe(eventList::add);
        helper.recyclerView.setAdapter(new EventsAdapter(eventList, layoutManager));
    }


    public void refreshAdapter()
    {
        helper.recyclerView.getAdapter().notifyDataSetChanged();
    }
}