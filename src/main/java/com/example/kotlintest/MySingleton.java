package com.example.kotlintest;

import android.widget.Toast;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean(scope = EBean.Scope.Singleton)
public class MySingleton {
    @RootContext
    MyApp context;

    void test() {
        Toast.makeText(context, "yo from "+context, Toast.LENGTH_SHORT).show();
    }
}
