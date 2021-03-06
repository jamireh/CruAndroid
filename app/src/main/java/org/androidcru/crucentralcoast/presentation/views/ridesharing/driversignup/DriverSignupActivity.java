package org.androidcru.crucentralcoast.presentation.views.ridesharing.driversignup;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.maps.MapFragment;

import org.androidcru.crucentralcoast.AppConstants;
import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.CruEvent;
import org.androidcru.crucentralcoast.data.models.CruUser;
import org.androidcru.crucentralcoast.data.models.Ride;
import org.androidcru.crucentralcoast.data.providers.RideProvider;
import org.androidcru.crucentralcoast.data.providers.UserProvider;
import org.androidcru.crucentralcoast.data.providers.observer.ObserverUtil;
import org.androidcru.crucentralcoast.presentation.customviews.CruSupportPlaceAutocompleteFragment;
import org.androidcru.crucentralcoast.presentation.util.DrawableUtil;
import org.androidcru.crucentralcoast.presentation.viewmodels.ridesharing.DriverSignupEditingVM;
import org.androidcru.crucentralcoast.presentation.viewmodels.ridesharing.DriverSignupVM;
import org.androidcru.crucentralcoast.presentation.views.base.BaseAppCompatActivity;
import org.androidcru.crucentralcoast.util.SharedPreferencesUtil;
import org.parceler.Parcels;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observer;
import rx.observers.Observers;
import timber.log.Timber;

public class DriverSignupActivity extends BaseAppCompatActivity {
    private DriverSignupVM driverSignupVM;

    @BindView(R.id.fab)
    FloatingActionButton fab;
    private CruSupportPlaceAutocompleteFragment autocompleteFragment;

    @BindView(com.google.android.gms.R.id.place_autocomplete_search_input)
    EditText searchInput;
    private MapFragment mapFragment;

    private CruEvent event;
    private Observer<CruUser> userObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_form);
        //get event from bundle
        Bundle bundle = getIntent().getExtras();
        event = (CruEvent) Parcels.unwrap(bundle.getParcelable(AppConstants.EVENT_KEY));
        if (bundle == null || event == null) {
            Timber.e("DriverSignupActivity requires that you pass an event");
            Timber.e("Finishing activity...");
            finish();
            return;
        }

        unbinder = ButterKnife.bind(this);

        setupFab();

        autocompleteFragment = (CruSupportPlaceAutocompleteFragment) getSupportFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_fragment);

        String rideId = bundle.getString(AppConstants.RIDE_KEY, "");

        setupUserObserver();

        if (!rideId.isEmpty())
            requestRides(rideId);
        else
            bindNewRideVM(null);
    }

    @OnClick(R.id.autocomplete_layout)
    public void onAutocompleteTouched(View v) {
        if (getCurrentFocus() != null)
            getCurrentFocus().clearFocus();
        searchInput.callOnClick();
    }

    //fill in fields that only the DriverSignupActivity has access to but DriverSignupVM doesn't
    private Ride completeRide(Ride r) {
        r.gcmID = SharedPreferencesUtil.getGCMID();
        r.eventId = event.id;
        return r;
    }

    private void createDriver() {
        RideProvider.createRide(Observers.empty(), completeRide(driverSignupVM.getRide()));
    }

    private void updateDriver() {
        RideProvider.updateRide(Observers.empty(), completeRide(driverSignupVM.getRide()));
    }

    private void setupPlacesAutocomplete() {
        AutocompleteFilter typeFilter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .build();
        autocompleteFragment.setFilter(typeFilter);
        autocompleteFragment.setHint(getString(R.string.autocomplete_hint_driver));
        autocompleteFragment.setOnPlaceSelectedListener(driverSignupVM.createPlaceSelectionListener());
    }

    private void requestRides(String rideId) {
        RideProvider.requestRideByID(this,
                Observers.create(
                        ride -> {
                            bindNewRideVM(ride);
                        }
                ), rideId);
    }

    private void bindNewRideVM(Ride r) {
        //new ride
        if (r == null)
            driverSignupVM = new DriverSignupVM(this, getFragmentManager(), event.id, event.startDate, event.endDate);
        //editing an existing ride
        else
            driverSignupVM = new DriverSignupEditingVM(this, getFragmentManager(), r, event.endDate);
        mapFragment.getMapAsync(driverSignupVM.onMapReady());
        setupPlacesAutocomplete();
    }

    private void setupFab()
    {
        fab.setImageDrawable(DrawableUtil.getTintedDrawable(this, R.drawable.ic_check_grey600, android.R.color.white));
        fab.setOnClickListener(v -> {
            String number = convString(driverSignupVM.phoneField.getText().toString());
            if (driverSignupVM.validator.validate() && autocompleteFragment.validate())
                if (SharedPreferencesUtil.getAuthorizedDriver(number))
                    sendRide();
                else
                    UserProvider.requestCruUser(this, userObserver, number);
        });
    }

    private void sendRide() {
        SharedPreferencesUtil.writeBasicInfo(driverSignupVM.nameField.getText().toString(), null, driverSignupVM.phoneField.getText().toString());

        if (driverSignupVM instanceof DriverSignupEditingVM)
            updateDriver();
        else
            createDriver();

        setResult(RESULT_OK);
        finish();
    }

    //remove anything that isn't a digit
    private String convString(String phone) {
        return phone.replaceAll("\\D", "");
    }

    private void setupUserObserver() {
        userObserver = ObserverUtil.create(Observers.create(t -> {
                    if (t != null) {
                        //update shared preferences with the number
                        SharedPreferencesUtil.setAuthoriziedDriver(convString(driverSignupVM.phoneField.getText().toString()));

                        sendRide();
                    }
                },
                e -> Timber.e(e, "Failed to retrieve User.")),
                () -> {
                    displayFailure();
                });
    }

    private void displayFailure()
    {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.driver_authorization_title)
                .setMessage(R.string.driver_authorization_msg)
                .setNegativeButton(R.string.driver_authorization_dismiss, (dialog1, which) -> {
                    dialog1.dismiss();
                })
                .create();
        dialog.show();
    }
}
