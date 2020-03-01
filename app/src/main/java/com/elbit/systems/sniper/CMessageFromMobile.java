package com.elbit.systems.sniper;

import java.util.Calendar;
import java.util.Date;

public class CMessageFromMobile
{
    static short MsgCnt = 0;
    public static void SetS8(byte[] byteArr, int bytePos, byte Val)
    {
        byteArr[bytePos] = Val;
    }

    public static void SetS16(byte[] byteArr, int bytePos, short Val)
    {
        byteArr[bytePos+0] = (byte)((Val >> 0) & 0xFF);
        byteArr[bytePos+1] = (byte)((Val >> 8) & 0xFF);
    }

    public static void SetS32(byte[] byteArr, int bytePos, long Val)
    {
        byteArr[bytePos+0] = (byte)((Val >>  0) & 0xFF);
        byteArr[bytePos+1] = (byte)((Val >>  8) & 0xFF);
        byteArr[bytePos+2] = (byte)((Val >> 16) & 0xFF);
        byteArr[bytePos+3] = (byte)((Val >> 24) & 0xFF);
    }

    public static byte[] CreateMsgFrPhone(Date date, double lat, double lon)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);

        int year   = c.get(Calendar.YEAR);
        int month  = c.get(Calendar.MONTH);
        int day    = c.get(Calendar.DAY_OF_MONTH);
        int hour   = c.get(Calendar.HOUR);
        int minute = c.get(Calendar.MINUTE);
        int second = c.get(Calendar.SECOND);
        int millis = c.get(Calendar.MILLISECOND);


        // ===============================================================
        //                              Set Header Message
        // ===============================================================
        int nFullMessageSize   = 49;
        int nHeaderMessageSize = 12;
        byte[] byteArr = new byte[nFullMessageSize];
        byteArr[0] = (byte)0xAA;                                     // First sync char
        byteArr[1] = (byte)0xAA;                                     // Second sync char
        byteArr[2] = (byte)'P';                                      // Sender ID
        byteArr[3] = (byte)(nFullMessageSize - nHeaderMessageSize);  // Payload size
        SetS16(byteArr, 4, MsgCnt);
        MsgCnt++;
        // byteArr[6 - 11] // Not used for now

        // ===============================================================
        //                              Set Body Message
        // ===============================================================
        // Set Validity Bit Mask
        int   nByteOffset = 12;
        short nValidityBitMask = 0x01;
        SetS16(byteArr, nByteOffset, nValidityBitMask);
        nByteOffset = nByteOffset + 16/8;

        // Set Harness ID
        long nHarnessID = 1;
        SetS32(byteArr, nByteOffset, nHarnessID);
        nByteOffset = nByteOffset + 32/8;

        // Set Exercise ID
        byte nExerciseID = 2;
        SetS8(byteArr, nByteOffset, nExerciseID);
        nByteOffset = nByteOffset + 8/8;

        // Set Loc Coord Lon
        long nLocCoordLon = Math.round(lon*10000000);
        SetS32(byteArr, nByteOffset, nLocCoordLon);
        nByteOffset = nByteOffset + 32/8;

        // Set Loc Coord Lat
        long nLocCoordLat =  Math.round(lat*10000000);
        SetS32(byteArr, nByteOffset, nLocCoordLat);
        nByteOffset = nByteOffset + 32/8;

        // Set Loc Coord Alt
        long nLocCoordAlt = 309;
        SetS32(byteArr, nByteOffset, nLocCoordAlt);
        nByteOffset = nByteOffset + 32/8;

        // Set Event Time
        long nEventTime = hour * 10000 + minute * 100 + second;
        SetS32(byteArr, nByteOffset, nEventTime);
        nByteOffset = nByteOffset + 32/8;

        // Set Sender Type
        byte nSenderType = 3;
        SetS8(byteArr, nByteOffset, nSenderType);
        nByteOffset = nByteOffset + 8/8;

        // Set Role ID
        long nRoleID = 1;
        SetS32(byteArr, nByteOffset, nRoleID);
        nByteOffset = nByteOffset + 32/8;

        // Set Health State
        byte nHealthState = 2;
        SetS8(byteArr, nByteOffset, nHealthState);
        nByteOffset = nByteOffset + 8/8;

        // Set Threshold Distance
        short nThresholdDistance = 3;
        SetS16(byteArr, nByteOffset, nThresholdDistance);
        nByteOffset = nByteOffset + 16/8;

        // Set Threshold Time
        short nThresholdTime = 4;
        SetS16(byteArr, nByteOffset, nThresholdTime);
        nByteOffset = nByteOffset + 16/8;

        // Set Weapon Type
        byte nWeaponType = 5;
        SetS8(byteArr, nByteOffset, nWeaponType);
        nByteOffset = nByteOffset + 8/8;

        // Set Ammo Type
        byte nAmmoType = 6;
        SetS8(byteArr, nByteOffset, nAmmoType);
        nByteOffset = nByteOffset + 8/8;

        // Set Current Ammo Val
        short nCurrentAmmoVal = 7;
        SetS16(byteArr, nByteOffset, nCurrentAmmoVal);
        nByteOffset = nByteOffset + 16/8;

        if (nByteOffset != nFullMessageSize)
            System.out.println("Error Size of Mobile Message !!!");
        
        return byteArr;
    }
}
