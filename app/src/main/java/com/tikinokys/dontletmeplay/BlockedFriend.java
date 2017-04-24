package com.tikinokys.dontletmeplay;

import android.text.format.DateFormat;

import java.sql.Date;
import java.sql.Timestamp;

public class BlockedFriend {
    long unBlockDate;
    String login;

    BlockedFriend(String l, long ubd){
        this.unBlockDate = ubd;
        this.login = l;
    }

    public String getLogin() {
        return login;
    }

    public String getUnBlockDate(){
        long a = unBlockDate*1000;
        Timestamp stamp = new Timestamp(a);
        Date date = new Date(stamp.getTime());
        String s = String.valueOf(DateFormat.format("dd-MM-yyyy (HH:mm)", date));
        return s;
    }

    public void setUnBlockDate(long unBlockDate) {
        this.unBlockDate = unBlockDate;
    }

    public void setLogin(String login) {
        this.login = login;
    }

}
