package com.cs478.funcenteruser;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.cs478.KeyComon.FunCenterService;

public class MainActivity extends Activity {
    protected static final String TAG = "MainActivity";
    protected static final int PERMISSION_REQUEST = 0;
    private FunCenterService funCenterServices;
    private boolean mIsBound = false;
    private boolean isPlaying = false;
    private Button song1;
    private Button song2;
    private Button song3;
    private Button picture1;
    private Button picture2;
    private Button picture3;
    private Button stop;
    private ImageButton playAndPause;
    private ImageView picture;
    private int curID;
    private boolean stopPressed = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        song1 = findViewById(R.id.song1);
        song2 = findViewById(R.id.song2);
        song3 = findViewById(R.id.song3);
        picture1 = findViewById(R.id.picture1);
        picture2 = findViewById(R.id.picture2);
        picture3 = findViewById(R.id.picture3);
        playAndPause = findViewById(R.id.playAndPause);
        stop = findViewById(R.id.stop);
        picture = findViewById(R.id.picture);

        song1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSong(0);
            }
        });

        picture1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImage(0);
            }
        });

        song2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSong(1);
            }
        });

        picture2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImage(1);
            }
        });

        song3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playSong(2);
            }
        });

        picture3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImage(2);
            }
        });
        // play/pause button is clicked so either resume or pause the song
        playAndPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // pausing the song
                    if(isPlaying) {
                        funCenterServices.pauseAudio();
                        playAndPause.setImageResource(android.R.drawable.ic_media_play);
                        isPlaying = false;
                    }
                    // resuming the song
                    else {
                        if(!stopPressed){
                            funCenterServices.resumeAudio();
                            playAndPause.setImageResource(android.R.drawable.ic_media_pause);
                            isPlaying = true;
                        }

                    }
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });
        // stopping the song from the service
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    funCenterServices.stopAudio();
                    stopPressed = true;
                    playAndPause.setImageResource(android.R.drawable.ic_media_play);
                    isPlaying = false;
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

    }
// play the song from the service
    private void playSong(int id) {
        try {
            // bound is connected
            if(mIsBound) {
                // song is not paused and stopped
                if(!isPlaying && stopPressed) {
                    funCenterServices.playAudio(id);
                    isPlaying = true;
                    playAndPause.setImageResource(android.R.drawable.ic_media_pause);
                    curID = id;
                    stopPressed = false;
                }

            }
            else {
                Log.i(TAG, "Ugo says that the service was not bound!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }
// get the image from the service and display it on the phone
    private void showImage(int id) {
        try {
            if (mIsBound) {
                picture.setImageBitmap(funCenterServices.getPicture(id));
            }
            else {
                Log.i(TAG, "Ugo says that the service was not bound!");
            }
        } catch (RemoteException e) {
            Log.e(TAG, e.toString());
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        if (checkSelfPermission("com.cs478.funcenter.GEN_ID") != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{"com.cs478.funcenter.GEN_ID"}, PERMISSION_REQUEST);
        }
        else {
            checkBindingAndBind();
        }
    }

    protected void checkBindingAndBind() {
        if (!mIsBound) {

            boolean b ;
            Intent i = new Intent(FunCenterService.class.getName());
            // here is where we get the info.
            Log.e("Key client", i.toString()) ;

            // UB:  Stoooopid Android API-21 no longer supports implicit intents
            // to bind to a service #@%^!@..&**!@
            // Must make intent explicit or lower target API level to 20.
            ResolveInfo info = getPackageManager().resolveService(i, PackageManager.MATCH_ALL);
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            b = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);
            if (b) {
                Log.i(TAG, "Ugo says bindService() succeeded!");
            } else {
                Log.i(TAG, "Ugo says bindService() failed!");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission granted, go ahead and bind to service
                    checkBindingAndBind();
                }
                else {
                    Toast.makeText(this, "BUMMER: No Permission :-(", Toast.LENGTH_LONG).show() ;
                }
            }
            default: {
                // do nothing
            }
        }
    }

    @Override
    protected void onStop() {

        super.onStop();

        if (mIsBound) {
            unbindService(this.mConnection);
        }
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder iservice) {

            funCenterServices = FunCenterService.Stub.asInterface(iservice);
            mIsBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            try {
                funCenterServices.stopAudio();
            } catch (RemoteException e) {

            }

            funCenterServices = null;
            mIsBound = false;


        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }
// when the app is closed out, it'll stop the music
    @Override
    public void onPause() {
        super.onPause();
        if (mIsBound) {
        try {
            funCenterServices.stopAudio();
        } catch (RemoteException e) {

        }
        }
    }
}