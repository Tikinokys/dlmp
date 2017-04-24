package com.tikinokys.dontletmeplay;

import android.text.format.DateFormat;

import java.sql.Date;
import java.sql.Timestamp;

public class BlockedFriend {
    private String unBlockDate;
    private String login;

    public BlockedFriend(String login, String unBlockDate){
        super();
        this.unBlockDate = unBlockDate;
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public String getUnBlockDate(){
        return unBlockDate;
    }

    public void setUnBlockDate(String unBlockDate) {
        this.unBlockDate = unBlockDate;
    }

    public void setLogin(String login) {
        this.login = login;
    }

}
