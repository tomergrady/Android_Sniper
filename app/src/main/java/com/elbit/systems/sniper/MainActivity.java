package com.elbit.systems.sniper;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;

//import com.felhr.usbserial.*;
import com.elbit.systems.sniper.R;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.location.Location;


import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import android.content.Context;

import android.hardware.usb.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

// =============================================================================================
// CLASS MAIN ACTIVITY
// =============================================================================================
public class MainActivity extends AppCompatActivity
{
    // =============================================================================================
    // General Status Data Members
    // =============================================================================================
    boolean m_bGeneralError = false;
    String  m_sGeneralErrorString = "";

    // =============================================================================================
    // Data Message From CCU
    // =============================================================================================
    private static int m_nMessageDataFromCCU_MsgCnt                 = 0;     // Header Message Counter
    private static int m_nMessageDataFromCCU_MsgTypeID 		        = 0; 	 // 0 = Reply for Status update request, 1 = Event status – Hit, 2 = Event status – Fire, 3 = Periodic Update
    private static int m_nMessageDataFromCCU_ValidityBitMask        = 0;     // Bit 0 - Location + Time Valid, Bit 1 - Last Hit Present, Bits 2-15 - Spare
    private static int m_nMessageDataFromCCU_HarnessID 		        = 0;     // For demo own harness ID
    private static int m_nMessageDataFromCCU_ExerciseID 		    = 0;     // TBD (For demo, keep ‘00’hex)
    private static int m_nMessageDataFromCCU_LocCoordX 		        = 0;     // DDDMMSSsss. Own harness location GEOTime format. To be used if ValidityBitMask & 0x01.
    private static int m_nMessageDataFromCCU_LocCoordY 		        = 0;     // DDDMMSSsss. Own harness location GEOTime format. To be used if ValidityBitMask & 0x01.
    private static int m_nMessageDataFromCCU_LocCoordZ 		        = 0;     // Height from sea level. Own harness altitude m. To be used if ValidityBitMask & 0x01.
    private static int m_nMessageDataFromCCU_EventTime 		        = 0;     // Seconds since Jan 1, 1970 (EPOCH time)	Seconds. To be used if ValidityBitMask & 0x01.
    private static int m_eMessageDataFromCCU_SenderType 		    = 0;     // 1 = IOS, 2 = Field instructor, 3 = Trainee / Mobile device (Use for Demo)
    private static int m_eMessageDataFromCCU_RoleID 			    = 0;     // TBD (For demo, keep ‘00000000’hex)
    private static int m_eMessageDataFromCCU_HealthState 	        = 0;     // 1 = Health / Revive (Default value), 2 = Damage / Injured, 3 = Destroyed / Killed
    private static int m_nMessageDataFromCCU_ThresholdDistance      = 0;     // TBD (For demo, keep ‘0000’hex)
    private static int m_nMessageDataFromCCU_ThresholdTime 	        = 0;     // TBD (For demo, keep ‘0000’hex)
    private static int m_eMessageDataFromCCU_WeaponType 		    = 0;     // Own harness weapon type
    private static int m_eMessageDataFromCCU_AmmoType 		        = 0;     // Own harness munition type
    private static int m_nMessageDataFromCCU_CurrentAmmoVal 	    = 0;     // Own harness current munition count
    private static int m_nMessageDataFromCCU_LastAttackerHarnessID  = 0;     // HarnessID of the attacking entity received via laser – To be used in case of Hit event (MsgTypeID = 3)
    private static int m_nMessageDataFromCCU_LastHitMunitionTypeID  = 0;     // Munition type of the attacking entity received via laser – To be used in case of Hit event (MsgTypeID = 3)
    private static int m_nMessageDataFromCCU_LastAttackerLocX       = 0;     // Attacker harness location GEOTime format. To be used in case of Hit event (MsgTypeID = 1) or ValidityBitMask & 0x02.
    private static int m_nMessageDataFromCCU_LastAttackerLocY       = 0;     // Attacker harness location GEOTime format. To be used in case of Hit event (MsgTypeID = 1) or ValidityBitMask & 0x02.
    private static int m_nMessageDataFromCCU_LastAttackerLocZ       = 0;     // Attacker harness location GEOTime format. To be used in case of Hit event (MsgTypeID = 1) or ValidityBitMask & 0x02.
    private static int m_eMessageDataFromCCU_HitLocation 	        = 0;     // 1 = Front, 2 = Back, 3 = Left side, 4 = Right Sidem, 5 = Head, 6 = Left leg, 7 = Right leg. To be used in case of Hit event (MsgTypeID = 1) or ValidityBitMask & 0x02.
    private static int m_nMessageDataFromCCU_PU_BatteryPower        = 0;     // Percent
    private static int m_nMessageDataFromCCU_HB_BatteryPower        = 0;     // Percent
    private static int m_nMessageDataFromCCU_LE1_BatteryPower       = 0;     // Percent
    private static int m_eMessageDataFromCCU_HitType 		        = 0;     // To be used in case of Hit event (MsgTypeID = 3). 1 = Miss (Default value), 2 = Near hit, 3 = Hit
    private static int m_nMessageDataFromCCU_LE2_BatteryPower       = 0;     // Percent

    // =============================================================================================
    // Message Help Enums
    // =============================================================================================
    // PLAYER TYPE ENUM
    public static final int PLAYER_TYPE_UNKOWN        = 0;
    public static final int PLAYER_TYPE_AS_IOS        = 1;
    public static final int PLAYER_TYPE_AS_INSTRACTOR = 2;
    public static final int PLAYER_TYPE_AS_TRAINEE    = 3;

    // PLAYER HEALTH STATE ENUM
    public static final int PLAYER_HEALTH_STATE_UNKOWN     = 0;
    public static final int PLAYER_HEALTH_STATE_AS_ALIVE   = 1;
    public static final int PLAYER_HEALTH_STATE_AS_INJURED = 2;
    public static final int PLAYER_HEALTH_STATE_AS_KILLED  = 3;

    // PLAYER_HIT_LOCATION_ENUM
    public static final int PLAYER_HIT_LOCATION_UNKOWN       = 0;
    public static final int PLAYER_HIT_LOCATION_IN_FRONT     = 1;
    public static final int PLAYER_HIT_LOCATION_IN_BACK      = 2;
    public static final int PLAYER_HIT_LOCATION_IN_LEFT      = 3;
    public static final int PLAYER_HIT_LOCATION_IN_RIGHT     = 4;
    public static final int PLAYER_HIT_LOCATION_IN_HEAD      = 5;
    public static final int PLAYER_HIT_LOCATION_IN_LEFT_LEG  = 6;
    public static final int PLAYER_HIT_LOCATION_IN_RIGHT_LEG = 7;

    // Message Help Data
    private static String m_sPlayerType        = "Trainee";
    private static String m_sPlayerHealthState = "Alive";
    private FirebaseUtil m_FirebaseUtil;

    // =============================================================================================
    // Location Data Members
    // =============================================================================================
    private LocationManager  m_cLocationManager   = null;
    private LocationListener m_cLocationListener  = null;
    double                   m_cLocationLatitude  = 0;
    double                   m_cLocationLongitude = 0;
    long                     m_cLocationTime      = 0;

    // =============================================================================================
    // Serial Comm Port Main Class
    // =============================================================================================
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private CSimpleSerialCommPort m_cSerialCommPort = null;

    // =============================================================================================
    // Serial Comm Port Read Data
    // =============================================================================================
    private int  m_nCCUMsgBufferHandlerIndex = 0;
    byte[]       m_byteSerialCommPortReadDataBufferFromCCU = new byte[73];
    private byte m_byteSerialCommPortReadDataBuffer[]      = new byte[73];
    private byte m_byteSerialCommPortReadDataMinimumSlot[] = new byte[73];
    private int  m_cSerialCommPortReadGoodMessageFromCCUCounter   = 0;

    // =============================================================================================
    // Serial Comm Message From Mobile
    // =============================================================================================
    CMessageFromMobile m_cMessageFromMobile = new CMessageFromMobile();
    private int  m_cSerialCommPortSendGoodMessageToCCUCounter   = 0;

    // =============================================================================================
    // Help Data
    // =============================================================================================
    private HandlerThread m_cHandlerThread = new HandlerThread("MyNetThread");
    private Handler m_cNetHandler          = null;
    private Handler m_cHandler             = null;

    // =============================================================================================
    // UI Text Help Fields
    // =============================================================================================
    private TextView m_cTextView_VestCommStatus = null;
    private TextView m_cTextView_CentralStationCommStatus = null;
    private TextView m_cTextView_Exercise_ID = null;
    private TextView m_cTextView_Player_ID = null;
    private TextView m_cTextView_GPSLat = null;
    private TextView m_cTextView_GPSLon = null;
    private TextView m_cTextView_GPAlt = null;
    private TextView m_cTextView_GPSTime = null;
    private TextView m_cTextView_HealthStatus = null;
    private TextView m_cTextView_MainWeaponStatus = null;
    private TextView m_cTextView_SlaveWeaponStatus = null;
    private TextView m_cTextView_BatteryStatus = null;
    private TextView m_cTextView_GeneralLog = null;

    // =============================================================================================
    // UI Navigation Buttons
    // =============================================================================================
    Button btnGoOnMap = null;
    Button btnShowStatus = null;
    Button btnGoToMain = null;

    // =============================================================================================
    // Class Methods
    // =============================================================================================
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseUtil.openFirebasereference("Players_Current", "Players_History");

        // Join Buttons
        btnGoOnMap    = (Button) findViewById(R.id.btnGoOnMap) ;
        btnShowStatus = (Button) findViewById(R.id.btnShowStatus) ;
        btnGoToMain   = (Button) findViewById(R.id.btnGoMain) ;

        // Join UI Fields
        m_cTextView_GeneralLog        = (TextView) findViewById(R.id.TextView_SystemViewLog) ;
        m_cTextView_VestCommStatus    = (TextView) findViewById(R.id.TextView_VestCommStatus);
        m_cTextView_CentralStationCommStatus = (TextView) findViewById(R.id.TextView_CentralStationCommStatus);
        m_cTextView_Exercise_ID       = (TextView) findViewById(R.id.TextView_Exercise_ID);
        m_cTextView_Player_ID         = (TextView) findViewById(R.id.TextView_Player_ID);
        m_cTextView_GPSLat            = (TextView) findViewById(R.id.TextView_GPSLat);
        m_cTextView_GPSLon            = (TextView) findViewById(R.id.TextView_GPSLon);
        m_cTextView_GPAlt             = (TextView) findViewById(R.id.TextView_GPSAlt);
        m_cTextView_GPSTime           = (TextView) findViewById(R.id.TextView_GPSTime);
        m_cTextView_HealthStatus      = (TextView) findViewById(R.id.TextView_HealthStatus);
        m_cTextView_MainWeaponStatus  = (TextView) findViewById(R.id.TextView_MainWeaponStatus);
        m_cTextView_SlaveWeaponStatus = (TextView) findViewById(R.id.TextView_SlaveWeaponStatus);
        m_cTextView_BatteryStatus     = (TextView) findViewById(R.id.TextView_BatteryStatus);

        // Set Color Status
        {
            m_cTextView_VestCommStatus.setTypeface(null, Typeface.BOLD);
            m_cTextView_VestCommStatus.setTextColor((Color.RED));

            m_cTextView_CentralStationCommStatus.setTypeface(null, Typeface.BOLD);
            m_cTextView_CentralStationCommStatus.setTextColor((Color.RED));
        }

        m_cSerialCommPort = new CSimpleSerialCommPort();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        m_cHandler = new Handler();

        m_cHandlerThread.start();
        Looper cLooper = m_cHandlerThread.getLooper();
        m_cNetHandler = new Handler(cLooper);

        m_cLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        m_cLocationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                m_cLocationLatitude=location.getLatitude();
                m_cLocationLongitude=location.getLongitude();
                m_cLocationTime = location.getTime();
                // txtPosition.setText("New Position # Lon : " + location.getLongitude() + ", Lat : " + location.getLatitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {

            }

            @Override
            public void onProviderEnabled(String s)
            {

            }

            @Override
            public void onProviderDisabled(String s)
            {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };


        // ==============================================================
        // Go To Show On Map
        // ==============================================================
        btnShowStatus = (Button)findViewById(R.id.btnShowStatus);
        if (btnShowStatus != null)
        {
            btnShowStatus.setOnClickListener( new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(getApplicationContext(), ShowStatus.class);
                    startActivity(intent);
                }
            });
        }

        // ==============================================================
        // Go To On Map Activity
        // ==============================================================

        // ==============================================================
        // Go To Main Activity
        // ==============================================================
        btnGoToMain = (Button)findViewById(R.id.btnGoMain);
        if (btnGoToMain != null)
        {
            btnGoToMain.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            });
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch (requestCode)
        {
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    Location configure_button()
    {
        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
            }
            return null;
        }
        
        // this code won'textView execute IF permissions are not allowed, because in the line above there is return statement.
        m_cLocationManager.requestLocationUpdates("gps", 5000, 0, m_cLocationListener);
        Location location = m_cLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        m_cTextView_VestCommStatus.setText("Position : " + location.getLongitude() + " " + location.getLatitude());

        return location;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void BuildPlayerTypeString(int nPlayerType, int nPlayerID)
    {
        switch (nPlayerType)
        {
            case MainActivity.PLAYER_TYPE_AS_IOS:
            {
                m_sPlayerType = "IOS (Central Station " + nPlayerID + ")";
                break;
            }
            case MainActivity.PLAYER_TYPE_AS_INSTRACTOR:
            {
                m_sPlayerType = "Instractor " + nPlayerID;
                break;
            }
            case MainActivity.PLAYER_TYPE_AS_TRAINEE:
            {
                m_sPlayerType = "Trainee " + nPlayerID;
                break;
            }
        }
    }

    public static void BuildPlayerHealthStateString(int eHealthState, int eHitType, int eHitLocation, int nAttackerHarnessID)
    {
        switch (eHealthState)
        {
            case MainActivity.PLAYER_HEALTH_STATE_AS_ALIVE:
            {
                m_sPlayerHealthState = "Alive";
                break;
            }
            case MainActivity.PLAYER_HEALTH_STATE_AS_INJURED:
            {
                switch (eHitLocation)
                {
                    case MainActivity.PLAYER_HIT_LOCATION_IN_FRONT:
                    {
                        m_sPlayerHealthState = "Injured In Front";
                        break;
                    }
                    case MainActivity.PLAYER_HIT_LOCATION_IN_BACK:
                    {
                        m_sPlayerHealthState = "Injured In Back";
                        break;
                    }
                    case MainActivity.PLAYER_HIT_LOCATION_IN_LEFT:
                    {
                        m_sPlayerHealthState = "Injured In Left";
                        break;
                    }
                    case MainActivity.PLAYER_HIT_LOCATION_IN_RIGHT:
                    {
                        m_sPlayerHealthState = "Injured In Right";
                        break;
                    }
                    case MainActivity.PLAYER_HIT_LOCATION_IN_HEAD:
                    {
                        m_sPlayerHealthState = "Injured In Head";
                        break;
                    }
                    case MainActivity.PLAYER_HIT_LOCATION_IN_LEFT_LEG:
                    {
                        m_sPlayerHealthState = "Injured In Left Leg";
                        break;
                    }
                    case MainActivity.PLAYER_HIT_LOCATION_IN_RIGHT_LEG:
                    {
                        m_sPlayerHealthState = "Injured In Right Leg";
                        break;
                    }
                }
                break;
            }
            case MainActivity.PLAYER_HEALTH_STATE_AS_KILLED:
            {
                m_sPlayerHealthState = "Killed";
                break;
            }
        }
    }

    /**********************************************************************************************/
    /*                           INTERNAL CLASS SERIAL PORT                                       */
    /**********************************************************************************************/
    public class CSimpleSerialCommPort extends BroadcastReceiver {
        private UsbManager m_cUsbManger = null;
        private UsbDevice m_cUsbDevice = null;
        private UsbDeviceConnection m_сUsbDeviceConnection = null;
        private int m_nDeviceVID = 0;
        private int m_nDevicePID = 0;

        public CSimpleSerialCommPort()
        {
            // open the first usb device connected, excluding usb root hubs
            m_cUsbManger = (UsbManager) getSystemService(Context.USB_SERVICE);

            if (m_cTextView_GeneralLog != null)
                m_cTextView_GeneralLog.setText("No Connection With Vest");

            HashMap<String, UsbDevice> usbDevices = m_cUsbManger.getDeviceList();
            if (usbDevices.size() <= 0)
            {
                m_bGeneralError = true;
                m_sGeneralErrorString = "No Connection To USB Com Port";
            }

            try
            {
                if (!usbDevices.isEmpty()) {
                    boolean bContinueFindUsbCommPort = true;
                    for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet())
                    {
                        m_cUsbDevice = entry.getValue();

                        if (m_cTextView_GeneralLog != null)
                            m_cTextView_GeneralLog.setText("Try Connect To Vest Comm" + m_cUsbDevice.getDeviceName());

                        m_nDeviceVID = m_cUsbDevice.getVendorId();
                        m_nDevicePID = m_cUsbDevice.getProductId();

                        if (m_nDeviceVID != 0x1d6b || (m_nDevicePID != 0x0001 || m_nDevicePID != 0x0002 || m_nDevicePID != 0x0003)) {
                            // We are supposing here there is only one device connected and it is our serial device
                            bContinueFindUsbCommPort = false;

                            if (m_cTextView_GeneralLog != null) {
                                m_cTextView_GeneralLog.setText("Connected To USB, No CCU Data");
                            }

                            if (m_cTextView_VestCommStatus!= null) {
                                m_cTextView_VestCommStatus.setText("Connected To USB, No CCU Data");
                                m_cTextView_VestCommStatus.setTextColor((Color.YELLOW));
                            }

                            try
                            {
                                PendingIntent permissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                                IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                                registerReceiver(this, filter);
                                m_cUsbManger.requestPermission(m_cUsbDevice, permissionIntent);

                            }
                            catch (Exception cc)
                            {
                                if (m_cTextView_GeneralLog != null)
                                    m_cTextView_GeneralLog.setText("Error From Vest " + cc.getMessage());
                            }
                        } else {
                            m_сUsbDeviceConnection = null;
                            m_cUsbDevice = null;
                        }

                        if (!bContinueFindUsbCommPort)
                            break;
                    }
                }
            }
            catch (Exception cc)
            {
                System.out.println(cc.getMessage());
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            Toast.makeText(context, "Permision granted!", Toast.LENGTH_SHORT).show();
                            m_cSerialCommPort.connect();
                        }
                    } else {
                        Log.d("XXX", "permission denied for device " + device);
                        Toast.makeText(context, "Permision NOT granted", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        }
        // =============================================================================================
        //        Serial Comm Port => Try Connect & Send Message To CCU & Receive Message From CCU
        // =============================================================================================
        public void connect()
        {
            Thread cLocalThreadTryConnectToSerialCommPort = new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    UsbSerialPort m_cSerialCommPort = null;

                    try
                    {
                        UsbSerialDriver driver = UsbSerialProber.getDefaultProber().probeDevice(m_cUsbDevice);
                        m_сUsbDeviceConnection = m_cUsbManger.openDevice(m_cUsbDevice);
                        m_cSerialCommPort = driver.getPorts().get(0);
                        m_cSerialCommPort.open(m_сUsbDeviceConnection);
                        m_cSerialCommPort.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

                        while (true)
                        {
                            try
                            {
                                // Update General Status
                                m_cHandler.post(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        m_cTextView_VestCommStatus.setText("Vest Status : Connected (Rx=" + m_cSerialCommPortReadGoodMessageFromCCUCounter + ", Tx=" + m_cSerialCommPortSendGoodMessageToCCUCounter + ")");
                                        m_cTextView_VestCommStatus.setTextColor((Color.BLUE));
                                    }
                                });

                                // Idel State - Wait Interval
                                Thread.sleep(200);

                                // Write Data To Serial Comm Port
                                final int nRealSendDataSize = m_cSerialCommPort.write(m_cMessageFromMobile.CreateMsgFrPhone(new Date(m_cLocationTime), m_cLocationLatitude, m_cLocationLongitude), 1000);
                                if (nRealSendDataSize == 49)
                                {
                                    Log.d("Debug", "Send Success Message To CCU ...");
                                    m_cSerialCommPortSendGoodMessageToCCUCounter++;
                                }
                                else
                                {
                                    Log.d("Error", "Error in write data to serial comm port ...");
                                }

                                // Delay Wait Message From CCU
                                Thread.sleep(800);

                                // Read Data From Serial Comm Port
                                boolean bTryReadCCUMessage = true;
                                if (bTryReadCCUMessage == true)
                                {
                                    final int nRealReadDataSize = m_cSerialCommPort.read(m_byteSerialCommPortReadDataMinimumSlot, 100);
                                    if (nRealReadDataSize > 0)
                                    {
                                        int CrtChr = CMessageHandlerUtil.byteToUnsignedInt(m_byteSerialCommPortReadDataMinimumSlot[0]);
                                        if (m_nCCUMsgBufferHandlerIndex == 0 || m_nCCUMsgBufferHandlerIndex == 1)
                                        {
                                            if (CrtChr != 0xDD)
                                            {
                                                m_nCCUMsgBufferHandlerIndex = 0;
                                                continue;
                                            }
                                        }

                                        if (m_nCCUMsgBufferHandlerIndex == 2)
                                        {
                                            if (CrtChr != 'T')
                                            {
                                                m_nCCUMsgBufferHandlerIndex = 0;
                                                continue;
                                            }
                                        }

                                        if (m_nCCUMsgBufferHandlerIndex == 3)
                                        {
                                            if (CrtChr != 61) // Message Payload Size, Size Equal To 61,  [sizeof(S_MSG_TO_SMRTPHONE) - sizeof(S_MSG_STD_HEADER)]
                                            {
                                                System.out.println("\nMobile Received a wrong message from CCU!\n");
                                                m_nCCUMsgBufferHandlerIndex = 0;
                                                continue;
                                            }
                                        }

                                        for (int nNewNetBufferIndex = 0; nNewNetBufferIndex < nRealReadDataSize; nNewNetBufferIndex++)
                                        {
                                            m_byteSerialCommPortReadDataBufferFromCCU[m_nCCUMsgBufferHandlerIndex] = m_byteSerialCommPortReadDataMinimumSlot[nNewNetBufferIndex];
                                            m_nCCUMsgBufferHandlerIndex++;

                                            if (m_nCCUMsgBufferHandlerIndex == 73) // Message Size = 73, sizeof(S_MSG_TO_SMRTPHONE))
                                            {
                                                m_nCCUMsgBufferHandlerIndex = 0;

                                                int nCrtChr1 = CMessageHandlerUtil.byteToUnsignedInt(m_byteSerialCommPortReadDataBufferFromCCU[0]);
                                                int nCrtChr2 = CMessageHandlerUtil.byteToUnsignedInt(m_byteSerialCommPortReadDataBufferFromCCU[1]);
                                                int nCrtChr3 = CMessageHandlerUtil.byteToUnsignedInt(m_byteSerialCommPortReadDataBufferFromCCU[3]);

                                                if ((nCrtChr1 == 0xDD) && (nCrtChr2 == 0xDD) && (nCrtChr3 == 61))
                                                {
                                                    // ==============================
                                                    // Receive New Valid CCU Message
                                                    // ==============================
                                                    m_cSerialCommPortReadGoodMessageFromCCUCounter++;

                                                    // =======================
                                                    // Get CCU Message Counter
                                                    // =======================
                                                    m_nMessageDataFromCCU_MsgCnt = CMessageHandlerUtil.GetU16(m_byteSerialCommPortReadDataBufferFromCCU, 4);

                                                    // =======================
                                                    // Get CCU Message Data
                                                    // =======================
                                                    int CrtOffset = 12;
                                                    m_nMessageDataFromCCU_MsgTypeID 		= CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,      CrtOffset); CrtOffset+=1; 	// 0 = Reply for Status update request, 1 = Event status – Hit, 2 = Event status – Fire, 3 = Periodic Update
                                                    m_nMessageDataFromCCU_ValidityBitMask   = CMessageHandlerUtil.GetU16(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset); CrtOffset+=2;   // Bit 0 - Location + Time Valid, Bit 1 - Last Hit Present, Bits 2-15 - Spare
                                                    m_nMessageDataFromCCU_HarnessID 		= CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset);CrtOffset+=4;    // For demo own harness ID
                                                    m_nMessageDataFromCCU_ExerciseID 		= CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,      CrtOffset);CrtOffset+=1;    // TBD (For demo, keep ‘00’hex)
                                                    m_nMessageDataFromCCU_LocCoordX 		= CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset);CrtOffset+=4;    // DDDMMSSsss. Own harness location GEOTime format. To be used if ValidityBitMask & 0x01.
                                                    m_nMessageDataFromCCU_LocCoordY 		= CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset);CrtOffset+=4;    // DDDMMSSsss. Own harness location GEOTime format. To be used if ValidityBitMask & 0x01.
                                                    m_nMessageDataFromCCU_LocCoordZ 		= CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset);CrtOffset+=4;    // Height from sea level. Own harness altitude m. To be used if ValidityBitMask & 0x01.
                                                    m_nMessageDataFromCCU_EventTime 		= CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset);CrtOffset+=4;    // Seconds since Jan 1, 1970 (EPOCH time)	Seconds. To be used if ValidityBitMask & 0x01.
                                                    m_eMessageDataFromCCU_SenderType 		= CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,      CrtOffset);CrtOffset+=1;    // 1 = IOS, 2 = Field instructor, 3 = Trainee / Mobile device (Use for Demo)
                                                    m_eMessageDataFromCCU_RoleID 			= CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset);CrtOffset+=4;    // TBD (For demo, keep ‘00000000’hex)
                                                    m_eMessageDataFromCCU_HealthState 	    = CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,      CrtOffset);CrtOffset+=1;    // 1 = Health / Revive (Default value), 2 = Damage / Injured, 3 = Destroyed / Killed
                                                    m_nMessageDataFromCCU_ThresholdDistance = CMessageHandlerUtil.GetU16(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset);CrtOffset+=2;    // TBD (For demo, keep ‘0000’hex)
                                                    m_nMessageDataFromCCU_ThresholdTime 	= CMessageHandlerUtil.GetU16(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset);CrtOffset+=2;    // TBD (For demo, keep ‘0000’hex)
                                                    m_eMessageDataFromCCU_WeaponType 		= CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,      CrtOffset);CrtOffset+=1;    // Own harness weapon type
                                                    m_eMessageDataFromCCU_AmmoType 		    = CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,      CrtOffset);CrtOffset+=1;    // Own harness munition type
                                                    m_nMessageDataFromCCU_CurrentAmmoVal 	= CMessageHandlerUtil.GetU16(m_byteSerialCommPortReadDataBufferFromCCU,     CrtOffset);CrtOffset+=2;    // Own harness current munition count
                                                    m_nMessageDataFromCCU_LastAttackerHarnessID = CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU, CrtOffset);CrtOffset+=4;    // HarnessID of the attacking entity received via laser – To be used in case of Hit event (MsgTypeID = 3)
                                                    m_nMessageDataFromCCU_LastHitMunitionTypeID = CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,  CrtOffset);CrtOffset+=1;    // Munition type of the attacking entity received via laser – To be used in case of Hit event (MsgTypeID = 3)
                                                    m_eMessageDataFromCCU_HitType 		        = CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,  CrtOffset);CrtOffset+=1;    // To be used in case of Hit event (MsgTypeID = 3). 1 = Miss (Default value), 2 = Near hit, 3 = Hit
                                                    m_nMessageDataFromCCU_LE2_BatteryPower      = CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,  CrtOffset);CrtOffset+=1;    // Percent
                                                    m_nMessageDataFromCCU_LastAttackerLocX      = CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU, CrtOffset);CrtOffset+=4;    // Attacker harness location GEOTime format. To be used in case of Hit event (MsgTypeID = 1) or ValidityBitMask & 0x02.
                                                    m_nMessageDataFromCCU_LastAttackerLocY      = CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU, CrtOffset);CrtOffset+=4;    // Attacker harness location GEOTime format. To be used in case of Hit event (MsgTypeID = 1) or ValidityBitMask & 0x02.
                                                    m_nMessageDataFromCCU_LastAttackerLocZ      = CMessageHandlerUtil.GetS32(m_byteSerialCommPortReadDataBufferFromCCU, CrtOffset);CrtOffset+=4;    // Attacker harness location GEOTime format. To be used in case of Hit event (MsgTypeID = 1) or ValidityBitMask & 0x02.
                                                    m_eMessageDataFromCCU_HitLocation        	= CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,  CrtOffset);CrtOffset+=1;    // 1 = Front, 2 = Back, 3 = Left side, 4 = Right Sidem, 5 = Head, 6 = Left leg, 7 = Right leg. To be used in case of Hit event (MsgTypeID = 1) or ValidityBitMask & 0x02.
                                                    m_nMessageDataFromCCU_PU_BatteryPower       = CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,  CrtOffset);CrtOffset+=1;    // Percent
                                                    m_nMessageDataFromCCU_HB_BatteryPower       = CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,  CrtOffset);CrtOffset+=1;    // Percent
                                                    m_nMessageDataFromCCU_LE1_BatteryPower      = CMessageHandlerUtil.GetU8(m_byteSerialCommPortReadDataBufferFromCCU,  CrtOffset);CrtOffset+=1;    // Percent

                                                    BuildPlayerTypeString(m_eMessageDataFromCCU_SenderType, m_nMessageDataFromCCU_HarnessID);
                                                    BuildPlayerHealthStateString(m_eMessageDataFromCCU_HealthState, m_eMessageDataFromCCU_HitType, m_eMessageDataFromCCU_HitLocation, m_nMessageDataFromCCU_LastAttackerHarnessID);

                                                    m_cHandler.post(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            // =======================
                                                            // Update Log Status
                                                            // =======================
                                                            if (m_cTextView_GeneralLog != null)
                                                                m_cTextView_GeneralLog.setText(" => Receive CCU Msg Counter " + m_nMessageDataFromCCU_MsgCnt);

                                                            // =======================
                                                            // Update UI Status Fields
                                                            // =======================
                                                            m_cTextView_Exercise_ID.setText("Exercise ID : " + m_nMessageDataFromCCU_ExerciseID);
                                                            m_cTextView_Player_ID.setText("Player : " + m_sPlayerType);
                                                            m_cTextView_GPSLat.setText("GPS Latitude : " + m_nMessageDataFromCCU_LocCoordX);
                                                            m_cTextView_GPSLon.setText("GPS Longitude : " + m_nMessageDataFromCCU_LocCoordY);
                                                            m_cTextView_GPAlt.setText("GPS Altitude : " + m_nMessageDataFromCCU_LocCoordZ);
                                                            m_cTextView_GPSTime.setText("GPS Time : " + m_nMessageDataFromCCU_EventTime);
                                                            m_cTextView_HealthStatus.setText("Health Status : " + m_sPlayerHealthState);
                                                            m_cTextView_MainWeaponStatus.setText("Main Weapon Status : M-16 Ammo 10/29");
                                                            m_cTextView_SlaveWeaponStatus.setText("Slave Weapon Status : Gun Ammo 8/12");
                                                            m_cTextView_BatteryStatus.setText("Battery Status : " + m_nMessageDataFromCCU_PU_BatteryPower);

                                                            // =======================
                                                            // Set Health Status Color
                                                            // =======================
                                                            if (m_eMessageDataFromCCU_HealthState == PLAYER_HEALTH_STATE_AS_INJURED)
                                                            {
                                                                m_cTextView_HealthStatus.setTypeface(null, Typeface.BOLD);
                                                                m_cTextView_HealthStatus.setTextColor((Color.BLUE));
                                                            }
                                                            else if (m_eMessageDataFromCCU_HealthState == PLAYER_HEALTH_STATE_AS_KILLED)
                                                            {
                                                                m_cTextView_HealthStatus.setTypeface(null, Typeface.BOLD);
                                                                m_cTextView_HealthStatus.setTextColor((Color.RED));
                                                            }
                                                            else
                                                            {
                                                                m_cTextView_HealthStatus.setTypeface(null, Typeface.NORMAL);
                                                                m_cTextView_HealthStatus.setTextColor((Color.BLACK));
                                                            }

                                                            // =======================
                                                            // Set Battery State Color
                                                            // =======================
                                                            if (m_nMessageDataFromCCU_PU_BatteryPower >= 60)
                                                            {
                                                                m_cTextView_BatteryStatus.setTypeface(null, Typeface.BOLD);
                                                                m_cTextView_BatteryStatus.setTextColor((Color.GREEN));
                                                            }
                                                            else if (m_nMessageDataFromCCU_PU_BatteryPower >= 30)
                                                            {
                                                                m_cTextView_BatteryStatus.setTypeface(null, Typeface.BOLD);
                                                                m_cTextView_BatteryStatus.setTextColor((Color.YELLOW));
                                                            }
                                                            else
                                                            {
                                                                m_cTextView_BatteryStatus.setTypeface(null, Typeface.BOLD);
                                                                m_cTextView_BatteryStatus.setTextColor((Color.RED));
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    }
                                }
                                else
                                {
                                    final int nRealReadDataSize = m_cSerialCommPort.read(m_byteSerialCommPortReadDataBuffer, 100);
                                    if (nRealReadDataSize > 0) {
                                        m_cHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                m_cSerialCommPortReadGoodMessageFromCCUCounter++;
                                                m_cTextView_GeneralLog.setText(" => " + m_cSerialCommPortReadGoodMessageFromCCUCounter);
                                                Log.d("Serial-Com ", "Read " + nRealReadDataSize + " bytes from CCU");
                                            }
                                        });
                                    }
                                }
                            }
                            catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                            catch (IOException e)
                            {
                                e.printStackTrace();
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            if (m_cSerialCommPort != null)
                            {
                                m_cSerialCommPort.close();
                            }
                        }
                        catch (IOException e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            });
            cLocalThreadTryConnectToSerialCommPort.start();
        }
    }

    private void savePlayer()
    {
        PlayerStateData cPlayerData = new PlayerStateData();
        cPlayerData.setsAlt("5000");
        cPlayerData.setsHealth("Alive");

        FirebaseUtil.m_cDataCurrentRef.push().setValue(cPlayerData);
    }
}
