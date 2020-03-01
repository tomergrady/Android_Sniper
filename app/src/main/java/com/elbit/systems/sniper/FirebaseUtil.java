package com.elbit.systems.sniper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FirebaseUtil
{
    public static FirebaseDatabase m_cFirebaseDatabase = null;
    public static DatabaseReference m_cDataCurrentRef = null;
    public static DatabaseReference m_cDataHistoryRef = null;
    public static DatabaseReference m_cDataEventsRef = null;
    public static FirebaseUtil     m_cFirebaseUtil = null;
    public static ArrayList<PlayerStateData> m_arrTravelDeals = null;

    private FirebaseUtil()
    {}

    public static void openFirebasereference(String sCurrentReference, String sHistoryReference)
    {
        if (m_cFirebaseUtil == null)
        {
            m_cFirebaseUtil = new FirebaseUtil();
            m_cFirebaseDatabase = FirebaseDatabase.getInstance();
            m_arrTravelDeals = new ArrayList<PlayerStateData>();
        }
        m_cDataCurrentRef = m_cFirebaseDatabase.getReference().child(sCurrentReference);
        m_cDataHistoryRef = m_cFirebaseDatabase.getReference().child(sHistoryReference);
    }
    private void UpdatePlayer(PlayerStateData cPlayerData)
    {
        m_cDataCurrentRef.push().setValue(cPlayerData);
        m_cDataHistoryRef.push().setValue(cPlayerData);

    }
}
