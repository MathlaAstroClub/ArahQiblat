package org.mathla.qiblat1;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    LocationTrack locationTrack;

    private TextView LatitudeTv;
    private TextView LongitudeTv;
    private TextView hasil;
    private final Double lintangKabah = 21.422472;
    private final Double bujurKabah = 39.82652778;
    private TextView hsl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LatitudeTv = (TextView) findViewById(R.id.latitudeEdit);
        LongitudeTv = (TextView) findViewById(R.id.longitudeEdit);

        hsl = (TextView) findViewById(R.id.infoText2);

        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);

        permissionsToRequest = findUnAskedPermissions(permissions);
        //get the permissions we have asked for before but are not granted..
        //we will store this in a global list to access later.


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


            if (permissionsToRequest.size() > 0)
                requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
        }


        Button btn = (Button) findViewById(R.id.btn);


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                locationTrack = new LocationTrack(MainActivity.this);


                if (locationTrack.canGetLocation()) {


                    double longitude = locationTrack.getLongitude();
                    double latitude = locationTrack.getLatitude();

                    Toast.makeText(getApplicationContext(), "Longitude : " + Double.toString(longitude)
                            + "\nLatitude : " + Double.toString(latitude), Toast.LENGTH_SHORT).show();

                    hsl.setText("Longitude : " + Double.toString(longitude)
                            + "\nLatitude : " + Double.toString(latitude));


                } else {

                    locationTrack.showSettingsAlert();
                }

            }
        });

    }


    public void onButtonClicked(View view) {
        EditText lati = (EditText) findViewById(R.id.latitudeEdit);
        EditText longi = (EditText) findViewById(R.id.longitudeEdit);
        TextView hasil = (TextView) findViewById(R.id.hasilEdit);
        TextView BackAz = (TextView) findViewById(R.id.infoText);

        Double bujurT = Double.parseDouble(longi.getText().toString());
        Double linT = Double.parseDouble(lati.getText().toString());

        Double A = (360 - bujurKabah + bujurT) % 360;

        Double h = Math.asin(Math.sin(Math.toRadians(linT)) * Math.sin(Math.toRadians(lintangKabah))
                + Math.cos(Math.toRadians(linT)) * Math.cos(Math.toRadians(lintangKabah)) * Math.cos(Math.toRadians(A)));

        Double d = Math.toDegrees(h);
        Double Az = Math.acos((Math.sin(Math.toRadians(lintangKabah)) - Math.sin(Math.toRadians(linT))
                * Math.sin(Math.toRadians(h)))
                / Math.cos(Math.toRadians(linT)) / Math.cos(Math.toRadians(h)));

        Double de = Math.toDegrees(Az);

        Double AQ = 360 - Az;


        Double a = 90 - linT;
        Double b = 90 - lintangKabah;
        Double c = bujurT - bujurKabah;

        Double Aq2 = Math.atan(1 / Math.tan(Math.toRadians(b)) * Math.sin(Math.toRadians(a))
                / Math.sin((Math.toRadians(c))) - Math.cos(Math.toRadians(a)) * 1 / Math.tan(Math.toRadians(c)));

        Double AqD = Math.toDegrees(Aq2);

        Double AzQ;

        if (c < 0) {
            AzQ = 90 + AqD;
        } else {
            AzQ = 270 + AqD;
        }

//        Double AzQD

        hasil.setText("Hasilnya : \nA : " + A + "\nh : " + h + "\nD : " + d + "\nAz : " + Az + "\nAQ : " + AQ + "\nDe : " + de
                + "\nAq2 : " + Aq2 + "\nAqD : " + AqD + "\nAzQ : " + AzQ);

        // 295 dari UTB
        String ba = String.valueOf(BackAzimut(bujurT, linT));
        BackAz.setText("Back Azimut : " + ba);


    }

    public Double BackAzimut(Double bujurT, Double linT) {
        int tgl = 17;
        int bln = 8;
        int thn = 2017;
        int jam = 8;
        int menit = 12;
        int detik = 45;
        int tz = 7;


        int Y;
        if (bln < 3) {
            Y = thn - 1;
        } else {
            Y = thn;
        }
        int M = 0;

        if (bln < 3) {
            M = bln + 12;
        } else {
            M = bln;
        }

        int A = (thn / 100);
        int B = 2 - A + (A / 4);

        Double JD = (int) (365.25 * (Y + 4716)) + (int) (30.6001 * (M + 1)) + tgl + B - 1524.5 + ((jam * 3600 + menit * 60 + detik) / 86400) - tz / 24;

        Double deltaT = 0.000814085;
        Double JDE = JD + deltaT;
        Double T = (JDE - 2451545) / 36525;

        Double L = (280.46607 + 36000.7698 * T) % 360;
        Double m = (357.5291 + 35999.0503 * T) % 360;
        Double C = (1.9146 - 0.0048 * T) * Math.sin(Math.toRadians(m)) + (0.02 - 0.0001)
                * Math.sin(Math.toRadians(2 * M)) + 0.0003 * Math.sin(Math.toRadians(3 * M));
        Double E = 0.0167086 - 0.000042 * T;

        Double L1 = L + C;
        Double M1 = m + C;

        Double omega = (125.04452 - 1934.13626 * T % 360) + 360;

//        Double epsilon0 = (Math.toRadians(23.43929111) - (0.01300417 * T)) * 57.2958;
        Double epsilon0 = Math.toRadians(23.43929111) - (0.01300417 * T);
        Double deltaE = 9.2 * Math.cos(Math.toRadians(omega)) / 3600 + 0.57 * Math.cos(Math.toRadians(2 * L)) / 3600;
        Double epsilon = Math.toRadians(23.43699912) + deltaE;
        Double JD0 = JDE.intValue() + 0.5;
        Double T1 = (JD0 - 2451545) / 36525;

        Double GST0 = (6.6973745583 + (2400.0513369072 * T1) + (0.0000258622 * T1 * T1)) % 24;
        Double GSTL = (GST0 + (jam + menit / 60 + detik / 3600 - tz) * 1.00273790935) % 24;
        Double LST = (GSTL + bujurT / 15) % 24;
        Double LM = L1 - 0.00569 - 0.00478 * Math.sin(Math.toRadians(omega));
        Double alpha0 = Math.toDegrees(Math.atan(Math.cos(epsilon)) * Math.tan(Math.toRadians(LM)));
        Double alpha; // = (α0 + 180)/15;
        if (LM < 90) {
            alpha = alpha0 / 15;
        } else if (LM < 270) {
            alpha = (alpha0 + 180) / 15;
        } else {
            alpha = (alpha0 + 360) / 15;
        }

        Double dec = Math.asin(Math.sin(epsilon) * Math.sin(Math.toRadians(LM))); // nepika dieu mah geus bener

//        Double HA = ((LST - alpha) % 24) * 15;
//        Double HA = 15 * ((LST - alpha) + 24);
        Double HA = 15 * (LST - alpha + 24);
//        Az0 = tan-1 (-sin φT : tan HA + cos φT . tan δ : sin HA) + 90
//        Double Az0 = Math.atan(- (Math.sin(Math.toRadians(linT)) / Math.tan(Math.toRadians(HA)))
//                + (Math.cos(Math.toRadians(linT)) * Math.tan(Math.toRadians(dec))
//                / Math.sin(Math.toRadians(HA))));

        Double Az0 = Math.atan2((Math.cos(Math.toRadians(HA) * Math.sin(Math.toRadians(linT))
                - Math.tan(dec)) * Math.cos(Math.toRadians(linT))), Math.sin(Math.toRadians(HA)));

        Double AzD = Math.toDegrees(Az0);

        Double Az;

        if (Az0 < 90) {
            Az = Az0 + 90;
        } else {
            Az = Az0 + 270;
        }


        Double BAz;

        if (Az > 180) {
            BAz = Az - 180;
        } else {
            BAz = Az + 180;
        }

        Double Altitude = Math.asin(Math.sin(Math.toRadians(linT)) * Math.sin(Math.toRadians(dec))
                + Math.cos(Math.toRadians(linT)) * Math.cos(Math.toRadians(dec)) * Math.cos(Math.toRadians(HA)));

        System.out.println("Y : " + Y);
        System.out.println("M : " + M);
        System.out.println("A : " + A);
        System.out.println("B : " + B);
        System.out.println("JD : " + JD);
        System.out.println("JDE : " + JDE);
        System.out.println("T : " + T);
        System.out.println("L : " + L);
        System.out.println("m : " + m);
        System.out.println("C : " + C);
        System.out.println("E : " + E);
        System.out.println("L1 : " + L1);
        System.out.println("M1 : " + M1);
        System.out.println("omega : " + omega);
        System.out.println("epsilon : " + epsilon);
        System.out.println("epsilon0 : " + epsilon0);
        System.out.println("deltaE : " + deltaE);
        System.out.println("JD0 : " + JD0);
        System.out.println("T1 : " + T1);
        System.out.println("GST0 : " + GST0);
        System.out.println("GSTL : " + GSTL);
        System.out.println("LST : " + LST);
        System.out.println("LM : " + LM);
        System.out.println("alpha0 : " + alpha0);
        System.out.println("alpha : " + alpha);
        System.out.println("dec : " + dec);
        System.out.println("HA : " + HA);
        System.out.println("Az0 : " + Az0);
        System.out.println("AzD : " + AzD);
        System.out.println("Az : " + Az);
        System.out.println("BAz : " + BAz);
        System.out.println("Altitude : " + Altitude);

        return BAz;

    }


    private ArrayList<String> findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList<String> result = new ArrayList<String>();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canMakeSmores()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canMakeSmores() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        switch (requestCode) {

            case ALL_PERMISSIONS_RESULT:
                for (String perms : permissionsToRequest) {
                    if (!hasPermission(perms)) {
                        permissionsRejected.add(perms);
                    }
                }

                if (permissionsRejected.size() > 0) {


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    });
                            return;
                        }
                    }

                }

                break;
        }

    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationTrack.stopListener();
    }
}
