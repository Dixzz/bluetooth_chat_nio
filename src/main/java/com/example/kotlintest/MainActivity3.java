package com.example.kotlintest;

import android.Manifest;
import android.app.NotificationManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import okhttp3.ResponseBody;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.kotlintest.HelperKt.logit;

@AndroidEntryPoint
@EActivity(R.layout.activity_main2)
@RuntimePermissions
public class MainActivity3 extends AppCompatActivity {
    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;
    public static final String PERMISSION_AUDIO = Manifest.permission.RECORD_AUDIO;

    @Bean
    MySingleton aaao;

    @Inject
    SharedViewModel sharedViewModel;

    @Inject
    MySingleton2 makeText;

    @SystemService
    NotificationManager notificationManager;

    boolean timer = false;

    @Click(R.id.test)
    void doit() {
        //showCamera();

        timerTask();
        aa();
    }

    @NeedsPermission({PERMISSION_CAMERA, PERMISSION_AUDIO})
    protected void showCamera() {
        Toast.makeText(this, "Permission for camera granted", Toast.LENGTH_SHORT).show();

        ResultHolder resultHolder = new ResultHolder();
        computeResultA(resultHolder);
        computeResultB(resultHolder);
        longlong();
    }

    @OnPermissionDenied({PERMISSION_CAMERA, PERMISSION_AUDIO})
    protected void onPermissionDeniedCamera() {
        Toast.makeText(this, "@OnPermissionDenied for camera", Toast.LENGTH_SHORT).show();
    }

    @OnShowRationale({PERMISSION_CAMERA, PERMISSION_AUDIO})
    protected void showRationaleForCamera(PermissionRequest request) {
        Toast.makeText(this, "OnShowRationale for camera", Toast.LENGTH_SHORT).show();
        request.proceed();
    }

    @OnNeverAskAgain({PERMISSION_CAMERA, PERMISSION_AUDIO})
    protected void showNeverAskForCamera() {
        Toast.makeText(this, "OnNeverAskAgain for camera", Toast.LENGTH_SHORT).show();
    }

    void aa() {
        aaao.test();
        makeText.init().getPoData().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                logit(response.code());
                try {
                    UiUtil.INSTANCE.toast(MainActivity3.this, response.body().string());
                    //logit(response.body() != null ? response.body().string() : null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
    }

    @AfterInject
    void aaa() {
        logit(sharedViewModel + " " + notificationManager + " " + aaao);
    }

    @Background
    public void timerTask() {
        if (timer) return;
        timer = true;
        for (int i = 1; ; i++) {
            try {
                Thread.sleep(1000);
                logit(i);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    static class ResultHolder {
        CubicAccelerateDecelerateInterpolator resultA;
        CubicAccelerateDecelerateInterpolator resultB;
    }

    @Background
    void computeResultA(ResultHolder resultHolder) {
        CubicAccelerateDecelerateInterpolator resultA = new CubicAccelerateDecelerateInterpolator();
        // Do some stuff with resultA

        try {
            Thread.sleep(10000);
            joinWork(resultHolder, resultA, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Background
    void longlong() {
        try {
            Thread.sleep(2000);
            showResult(10000L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    void showResult(double result) {
        logit(result);
        UiUtil.INSTANCE.toast(this, "Yo");
    }

    @Background
    void computeResultB(ResultHolder resultHolder) {
        CubicAccelerateDecelerateInterpolator resultB = new CubicAccelerateDecelerateInterpolator();
        // Do some stuff with resultB
        try {
            Thread.sleep(1000);
            joinWork(resultHolder, null, resultB);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    void joinWork(ResultHolder resultHolder, CubicAccelerateDecelerateInterpolator resultA, CubicAccelerateDecelerateInterpolator resultB) {
        if (resultA != null)
            resultHolder.resultA = resultA;
        if (resultB != null)
            resultHolder.resultB = resultB;

        if (resultHolder.resultA == null || resultHolder.resultB == null) {
            return;
        }

        logit(resultHolder.resultA + " " + resultHolder.resultB);
        // Show the results on the UI Thread
    }


}