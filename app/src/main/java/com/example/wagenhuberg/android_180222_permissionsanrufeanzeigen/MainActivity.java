package com.example.wagenhuberg.android_180222_permissionsanrufeanzeigen;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.support.annotation.NonNull;

//public class MainActivity extends Activity {
public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RQ_ANRUF = 4711;

    private TextView textView;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermission();
            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        
        //View.GONE This view is invisible, and it doesn't take any space for layout purposes.
        //View.INVISIBLE This view is invisible, but it still takes up space for layout purposes.
        button.setVisibility(View.GONE);
        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            //App hat nicht die geforderte Permission
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALL_LOG)) {
                textView.setText(R.string.explain1);
                button.setVisibility(View.VISIBLE);
            } else {
                //App hat noch keine Permission => Permission muss angefordert werden
                requestPermission();
            }
        } else {
            //App hat Permission => weiter im Programm
            zeigeVerpassteAnrufe();
        }

    }

    private void zeigeVerpassteAnrufe() {
        textView.setText(getString(R.string.template, gibAnzahlVerpassteAnrufe()) );
    }

    private int gibAnzahlVerpassteAnrufe() {
        int verpassteAnrufe = 0;
        String[] projection = {CallLog.Calls._ID};
        String selection = CallLog.Calls.TYPE + " = ?";
        String[] selectionArgs = {Integer.toString(CallLog.Calls.MISSED_TYPE)};
        ContentResolver contentResolver = getContentResolver();
        try {
            Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, projection, selection, selectionArgs, null);
            if (cursor != null) {
                verpassteAnrufe = cursor.getCount();
                cursor.close();
            }
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException", e);
        }
        return verpassteAnrufe;
    }

    private void requestPermission() {
        String[] permission = new String[]{Manifest.permission.READ_CALL_LOG};
        requestPermissions(permission, RQ_ANRUF);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RQ_ANRUF) {
            //View.GONE: Nachfolgende Elemente rutschen nach oben nach / View.invisible: Platz wird weiter blockiert
            button.setVisibility(View.GONE);
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                zeigeVerpassteAnrufe();
            } else {
                textView.setText(R.string.explain2);
            }
        }
    }
}
