package com.mpvmedical.sonodrop;

public interface Constants {

    int MESSAGE_STATE_CHANGE = 1;
    int MESSAGE_READ = 2;
    int MESSAGE_DEVICE_NAME = 4;
    int MESSAGE_TOAST = 5;

    String DEVICE_NAME = "device_name";
    String TOAST = "toast";

    String GET_CONTROLER_STATUS = "%ms;";
    String GET_CONTROLER_HISTORY = "%rh;";

    String SET_CONTROLER_PROGRAM = "%wp,";
    String SET_CONTROLER_SETTINGS = "%pr,";
    String SET_CONTROLER_STATUS = "%st,";
}
