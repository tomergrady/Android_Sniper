package com.elbit.systems.sniper;

public class CMessageHandlerUtil
{
    public static int byteToUnsignedInt(byte b)
    {
        return 0x00 << 24 | b & 0xff;
    }

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


    public static byte GetS8(byte[] byteArr, int bytePos)
    {
        byte Ret = 0;
        Ret = byteArr[bytePos];
        return Ret;
    }

    public static char GetU8(byte[] byteArr, int bytePos)
    {
        return (char)byteToUnsignedInt(GetS8(byteArr, bytePos));
    }

    public static int GetU16(byte[] byteArr, int bytePos)
    {
        int Ret = 0;
        Ret |= ((int)byteToUnsignedInt(byteArr[bytePos+0])) <<  0;
        Ret |= ((int)byteToUnsignedInt(byteArr[bytePos+1])) <<  8;
        return Ret;
    }

    public static int GetS32(byte[] byteArr, int bytePos)
    {
        long Ret = 0;
        Ret |= ((long)byteToUnsignedInt(byteArr[bytePos+0])) <<  0;
        Ret |= ((long)byteToUnsignedInt(byteArr[bytePos+1])) <<  8;
        Ret |= ((long)byteToUnsignedInt(byteArr[bytePos+2])) << 16;
        Ret |= ((long)byteToUnsignedInt(byteArr[bytePos+3])) << 24;

        return (int)Ret;
    }

}
