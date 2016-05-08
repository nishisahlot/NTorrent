package com.nikki.torrents;

import android.app.Application;

import com.activeandroid.ActiveAndroid;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender;

/**
 * Created by Nishi Sahlot on 3/6/2016.
 */
@ReportsCrashes(
        formUri = "https://nishisahlot.cloudant.com/acra-ntorrent/_design/acra-storage/_update/report",
        reportType = HttpSender.Type.JSON,
        httpMethod = HttpSender.Method.POST,
        formUriBasicAuthLogin = "ghtseediervingensedisubj",
        formUriBasicAuthPassword = "cd11bd3a71578d8612b3e0e96c96e82d71335a7a",
        customReportContent = {
                ReportField.APP_VERSION_CODE,
                ReportField.APP_VERSION_NAME,
                ReportField.ANDROID_VERSION,
                ReportField.PACKAGE_NAME,
                ReportField.REPORT_ID,
                ReportField.BUILD,
                ReportField.STACK_TRACE
        }

)
public class MyApplication extends Application{


    @Override
    public void onCreate() {
        super.onCreate();
        ActiveAndroid.initialize(this);
        ACRA.init(this);
//        CookieManager cookieManager=new CookieManager();
//        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
//        CookieHandler.setDefault(cookieManager);
    }
}
