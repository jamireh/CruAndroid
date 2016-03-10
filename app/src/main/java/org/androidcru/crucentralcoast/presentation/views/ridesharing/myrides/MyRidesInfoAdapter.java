package org.androidcru.crucentralcoast.presentation.views.ridesharing.myrides;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import org.androidcru.crucentralcoast.R;
import org.androidcru.crucentralcoast.data.models.Passenger;
import org.androidcru.crucentralcoast.data.providers.RideProvider;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by main on 2/27/2016.
 */
public class MyRidesInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    List<Passenger> passengers;
    private Context context;
    private AlertDialog alertDialog;
    private String rideID;
    private String selectedPassengerID;

    public MyRidesInfoAdapter(Context context, List<Passenger> passengers, String rideID)
    {
        this.context = context;
        this.rideID = rideID;
        this.passengers = (passengers == null) ? new ArrayList<Passenger>() : passengers;
        selectedPassengerID = null;
        initAlertDialog();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new PassengerInfoHolder(inflater.inflate(R.layout.card_rideinfopassenger, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        PassengerInfoHolder passengerInfoHolder = (PassengerInfoHolder) holder;
        passengerInfoHolder.passengerName.setText(passengers.get(position).name);
        passengerInfoHolder.passengerPhone.setText(passengers.get(position).phone);
    }

    @Override
    public int getItemCount()
    {
        return passengers.size();
    }

    public class PassengerInfoHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.passengerName) TextView passengerName;
        @Bind(R.id.passengerPhoneNum) TextView passengerPhone;
        @Bind(R.id.kickPassenger) Button kickPassenger;

        public PassengerInfoHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
            kickPassenger.setOnClickListener(kickPassengerButton());
        }

        private View.OnClickListener kickPassengerButton() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.setMessage("Are you sure you want to kick " + passengerName.getText().toString() + "?");

                    //this tempororary until a better way is found
                    boolean found = false;
                    String temp = passengerPhone.getText().toString();
                    for (int iter = 0; iter < passengers.size(); iter++) {
                        if (temp.equals(passengers.get(iter).phone.toString()))
                            selectedPassengerID = passengers.get(iter).id;
                    }

                    alertDialog.show();
                }
            };
        }

    }
    private void initAlertDialog() {
        alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle("Kick Passenger");
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Logger.d("chose " + selectedPassengerID);
                RideProvider.dropPassengerFromRide(selectedPassengerID, rideID)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Void>() {
                            @Override
                            public void onCompleted() {
                                Logger.d("Completed the drop");
                            }

                            @Override
                            public void onError(Throwable e) {
                                Logger.d("Error on the drop");
                            }

                            @Override
                            public void onNext(Void aVoid) {
                                //TODO: mitch told me to do it
                                if (context instanceof MyRidesInfoActivity) {
                                    ((MyRidesInfoActivity)context).updateRide();
                                }
                                else {
                                    Logger.d("wasn't right");
                                }
                            }
                        });
            }
        });
        alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.hide();
            }
        });
    }
}
