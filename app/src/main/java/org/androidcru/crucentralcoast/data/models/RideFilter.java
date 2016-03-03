package org.androidcru.crucentralcoast.data.models;

import com.google.android.gms.maps.model.LatLng;

import org.threeten.bp.ZonedDateTime;

public class RideFilter
{
    public Ride.Direction direction;
    public ZonedDateTime dateTime;
    public LatLng location;
    public String gender;
}