package com.yishu.locationtest;

import android.app.Activity;
import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

/**
 * 谷歌总是连不上服务器
 * 所以该用高德地图或者百度地图
 *
 */
@Deprecated
public class AddressAdapter {
    private String TAG = "AddressAdapter";
    public Activity activity = null;
    private LocationManager locationManager = null;
    Location m_location = null;

    public AddressAdapter(Activity activity) {
        this.activity = activity;
        locationManager = (LocationManager) this.activity.getSystemService(Context.LOCATION_SERVICE);
    }
//这些代码主要是用来进行NETWORK定位，所以暂时注释了GPS定位的功能
//    private boolean isGPSAvaliable()
//    {
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//    }

    private boolean isNetworkAvaliable() {
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    private Location getLocation() {
        if (isNetworkAvaliable())
            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        return null;
        //if(isGPSValiable())
        //    location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    private String getAddress(double latitude, double longitude) {

        String strAddress = "";
        Geocoder geocoder = new Geocoder(this.activity);
        List<Address> places = null;
        try {
            places = geocoder.getFromLocation(latitude, longitude, 5);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (places != null && places.size() > 0) {
            strAddress = ((Address) places.get(0)).getCountryName();
            strAddress += "\t"+((Address) places.get(0)).getSubAdminArea();
            strAddress += "\t"+((Address) places.get(0)).getSubLocality();
        }
        return strAddress;
    }

    public String getCountry() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); //设置查询精度
        criteria.setAltitudeRequired(false);  //设置是否需要得到海拔高度
        criteria.setBearingRequired(false);   //设置是否需要得到方向
        criteria.setCostAllowed(true);        //设置是否允许产生费用
        criteria.setPowerRequirement(Criteria.POWER_LOW); //设置允许的电池消耗级别

        String provider = locationManager.getBestProvider(criteria, true);
        Log.d(TAG, "location provider:" + provider);

        double latitude = 0;
        double longitude = 0;

        String strAddress = "";
        if (!isNetworkAvaliable())
            return "";

        Log.d(TAG, "Enabled Networkd Location");
        m_location = getLocation();

        //用来调试时显示信息用的。实际应用中不需要写
        Log.d(TAG, "can't get location from NETWORK");
        while (m_location == null) {
            m_location = locationManager.getLastKnownLocation(provider);
        }
        Log.d(TAG, "get location from NETWORK success");

        LocationListener listener = new LocationListener() {

            public void onLocationChanged(Location location) {
                //用来调试时显示信息用的。实际应用中不需要写
                Log.d(TAG, "onLocationChanged:" + location);
                m_location = location;
            }

            public void onProviderDisabled(String provider) {
            }


            public void onProviderEnabled(String provider) {
            }


            public void onStatusChanged(String provider, int status, Bundle extras) {

            }
        };
        //隔断时间定位下
        locationManager.requestLocationUpdates(provider, 1000, 10, listener);

        if (m_location != null) {
            //用来调试时显示信息用的。实际应用中不需要写
            latitude = m_location.getLatitude();
            longitude = m_location.getLongitude();
            Log.e("ADDRESS", "location != null" + latitude+"\t" +longitude);
            strAddress = getAddress(latitude, longitude);
            //如果已经取到地址信息，就取消监听，免得浪费资源
            locationManager.removeUpdates(listener);
        }
        //用来调试时显示信息用的。实际应用中不需要写
        Log.e("ADDRESS", "country:" + strAddress);
        return strAddress;
    }
}