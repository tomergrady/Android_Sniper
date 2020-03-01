package com.elbit.systems.sniper;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class FirebaseUtil
{
    public static FirebaseDatabase m_cFirebaseDatabase = null;
    public static DatabaseReference m_cDatabaseReference = null;
    public static FirebaseUtil     m_cFirebaseUtil = null;
    public static ArrayList<TravelDeal> m_arrTravelDeals = null;

    private FirebaseUtil()
    {}

    public static void openFirebasereference(String sReference)
    {
        if (m_cFirebaseUtil == null)
        {
            m_cFirebaseUtil = new FirebaseUtil();
            m_cFirebaseDatabase = FirebaseDatabase.getInstance();
            m_arrTravelDeals = new ArrayList<TravelDeal>();
        }
        m_cDatabaseReference = m_cFirebaseDatabase.getReference().child(sReference);
    }
}
