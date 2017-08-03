package com.dimka.expsample;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.net.Uri;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import android.util.Log;
import android.widget.Toast;


@SuppressWarnings("ResultOfMethodCallIgnored")
public class MainActivity extends Activity implements OnClickListener {

    private EditText numToCode,
            numToDecode;

    private TextView codedNum,
            decodedNum;

    double activeTab;

    String SCodeRes,
            SCodeRes2,
            NormalText,
            NormalText2,
            Slat,
            Slon,
            afin,
            bfin;

    final String FILENAME = "fileGPS",
            LOG_TAG = "myLogsGPS";

    Double DoubStr,
            DoubStr2,
            CodeRes,
            DecodeRes;

    double latD,
            lonD,
            latD10x,
            lonD10x;

    Button btnMap;

    TextView tvEnabledGPS,
            tvStatusGPS,
            tvLocationGPS,
            tvEnabledNet,
            tvStatusNet,
            tvLocationNet;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvEnabledGPS = (TextView) findViewById(R.id.tvEnabledGPS);
        tvStatusGPS = (TextView) findViewById(R.id.tvStatusGPS);
        tvLocationGPS = (TextView) findViewById(R.id.tvLocationGPS);

        tvEnabledNet = (TextView) findViewById(R.id.tvEnabledNet);
        tvStatusNet = (TextView) findViewById(R.id.tvStatusNet);
        tvLocationNet = (TextView) findViewById(R.id.tvLocationNet);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(this);

        numToCode = (EditText) findViewById(R.id.NumToCode);
        codedNum = (TextView) findViewById(R.id.codedNum);
        numToDecode = (EditText) findViewById(R.id.NumToDecode);
        decodedNum = (TextView) findViewById(R.id.decodedNum);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec tabSpec;

        tabSpec = tabHost.newTabSpec("GPS");
        tabSpec.setIndicator("GPS");
        tabSpec.setContent(R.id.GPSTab);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Settings");
        tabSpec.setIndicator("settings");
        tabSpec.setContent(R.id.tab2);
        tabHost.addTab(tabSpec);

        tabSpec = tabHost.newTabSpec("Network");
        tabSpec.setIndicator("Network");
        tabSpec.setContent(R.id.NetTab);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTabByTag("Settings");

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            public void onTabChanged(String tabId) {
                setActiveTab(tabId);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (activeTab == 1) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 10, locationListener);
            checkEnabled();
        }

        if (activeTab == 3) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, locationListener);
            checkEnabled();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    private LocationListener locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            showLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            checkEnabled();
        }

        @Override
        public void onProviderEnabled(String provider) {
            checkEnabled();
            showLocation(locationManager.getLastKnownLocation(provider));
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if (provider.equals(LocationManager.GPS_PROVIDER)) {
                tvStatusGPS.setText("Status: " + String.valueOf(status));
            }
            if (provider.equals(LocationManager.NETWORK_PROVIDER)) {
                tvStatusNet.setText("Status: " + String.valueOf(status));
            }
        }
    };

    private void showLocation(Location location) {
        if (location == null)
            return;

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            tvLocationGPS.setText(formatLocation(location));
        }
        if (location.getProvider().equals(LocationManager.NETWORK_PROVIDER)) {
            tvLocationNet.setText(formatLocation(location));
        }
        latD10x = location.getLatitude() * 10000;
        lonD10x = location.getLongitude() * 10000;
        latD = (int) latD10x;
        lonD = (int) lonD10x;
    }


    private String formatLocation(Location location) {
        if (location == null)
            return "";
        Slat = location.convert(location.getLatitude(), location.FORMAT_DEGREES);
        Slon = location.convert(location.getLongitude(), location.FORMAT_DEGREES);
        afin = Slat.replace(',', '.');
        bfin = Slon.replace(',', '.');
        return String.format("Decimal coordinates: latitude = %s1, longitude = %s2", afin, bfin);
    }


    private void checkEnabled() {
        if (activeTab == 1) {
            tvEnabledGPS.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        }

        if (activeTab == 3) {
            tvEnabledNet.setText("Enabled: " + locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        }

    }

    public void onClickLocationSettings(View view) {
        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    @Override
    public void onClick(View v) {
        if (afin == null) {
            Toast toast = Toast.makeText(getApplicationContext(), "No location's data", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            if (v.getId() == R.id.btnMap) {
                String geoUri = String.format("geo:%s,%s?z=15", afin, bfin);
                Intent geoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                startActivity(geoIntent);
            }
        }
    }

    public void OnClickCode(View view) {
        if (numToCode.getText().length() != 0) {
            NormalText = numToCode.getText().toString();
            DoubStr = Double.parseDouble(NormalText);
            if (lonD != 0) {
                CodeRes = DoubStr * 3 + (latD - (2 * lonD + 30000)); // CodeRes - Закодированное число, DoubStr - Данное число.
                SCodeRes = Double.toString(CodeRes);
                codedNum.setText(SCodeRes);
            } else {
                if (activeTab == 1 && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "GPS isn't working", Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (activeTab == 3 && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Network isn't working", Toast.LENGTH_SHORT);
                    toast.show();
                }
                codedNum.setText("");
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Enter number", Toast.LENGTH_SHORT);
            toast.show();
            codedNum.setText("");
        }
    }

    public void OnClickDecode(View view) {
        if (numToDecode.getText().length() != 0) {
            NormalText2 = numToDecode.getText().toString();
            DoubStr2 = Double.parseDouble(NormalText2);
            if (lonD != 0) {
                DecodeRes = (DoubStr2 - (latD - (2 * lonD + 30000))) / 3; // DecodeRes - Данное число, DoubStr2 - Закодированное число.
                SCodeRes2 = Double.toString(DecodeRes);
                decodedNum.setText(SCodeRes2);
            } else {
                if (activeTab == 1) {
                    CharSequence text = "GPS isn't working";
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                }
                if (activeTab == 3) {
                    CharSequence text = "Network isn't working";
                    Toast toast = Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT);
                    toast.show();
                }
                decodedNum.setText("");
            }
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Enter number", Toast.LENGTH_SHORT);
            toast.show();
            decodedNum.setText("");
        }

    }

    public void onClickBtn(View v) {
        switch (v.getId()) {
            case R.id.btnWrite:
                writeFile();
                break;
            case R.id.btnRead:
                readFile();
                break;
            case R.id.ClearBtn:
                ClearAll();
                break;
            case R.id.Developers:
                Intent intent = new Intent(getApplicationContext(), Developers.class);
                startActivity(intent);
                break;
        }
    }

    private void ClearAll() {
        numToDecode.setText("");
        numToCode.setText("");
        codedNum.setText("");
        decodedNum.setText("");

        Toast toast = Toast.makeText(getApplicationContext(), "All clear", Toast.LENGTH_SHORT);
        toast.show();
    }

    void writeFile() {
        if (codedNum.getText().length() == 0) {
            Toast toast = Toast.makeText(getApplicationContext(), "Enter number", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            try {
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        openFileOutput(FILENAME, MODE_PRIVATE)));
                bw.write(SCodeRes);
                bw.close();
                Log.d(LOG_TAG, "File have been recorded");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void readFile() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    openFileInput(FILENAME)));
            String str = "";
            while ((str = br.readLine()) != null) {
                Log.d(LOG_TAG, str);
                numToDecode.setText(str);
            }
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void OpenActivity(View view) {
        if (afin != null) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("text/*");
            String Mess = String.format("I'm here: latitude = %1s, longitude = %2s", afin, bfin);
            intent.putExtra(Intent.EXTRA_TEXT, Mess);
            startActivity(Intent.createChooser(intent, getResources().getText(R.string.Share)));
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "No location's data", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public void setActiveTab(String realActiveTab) {
        if (realActiveTab.contains("GPS")) {
            activeTab = 1;
            lonD = latD = 0;
            tvLocationGPS.setText("");
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 10, locationListener);
        }
        if (realActiveTab.contains("Settings")) {
            activeTab = 2;
        }
        if (realActiveTab.contains("Network")) {
            activeTab = 3;
            lonD = latD = 0;
            tvLocationNet.setText("");
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, locationListener);
        }
        checkEnabled();
    }
}





