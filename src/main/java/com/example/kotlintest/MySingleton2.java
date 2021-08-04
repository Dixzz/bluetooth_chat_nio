package com.example.kotlintest;

import android.content.Context;

import java.io.File;

import javax.inject.Inject;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

import static com.example.kotlintest.HelperKt.isOnline;
import static com.example.kotlintest.HelperKt.logit;

@Module
@InstallIn(SingletonComponent.class)
public class MySingleton2 {

    private static final String BASE_URl = "https://jsonplaceholder.typicode.com/";
    private final Retrofit retrofit;
    private static final Interceptor REWRITE_CACHE_CONTROL_INTERCEPTOR = chain -> {
        Response response = chain.proceed(chain.request());
        String cacheControlHeader;
        logit(isOnline());
        if (isOnline()) {
            int maxAge = 2419200;
            cacheControlHeader = "public, max-age=" + maxAge;
        } else {
            //int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
            int maxStale = 2419200;
            cacheControlHeader = "public, only-if-cached, max-stale=" + maxStale;
        }

        return response.newBuilder()
                .removeHeader("Pragma")             // HTTP/1.0
                .removeHeader("Cache-Control")      // HTTP/1.1
                .header("Cache-Control", cacheControlHeader)
                .build();
    };

    private OkHttpClient createCachedClient(final Context context) {
        File httpCacheDirectory = new File(context.getCacheDir(), "network_cache");

        Cache cache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);
        OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
        okHttpClient.cache(cache);
        Interceptor interceptor = REWRITE_CACHE_CONTROL_INTERCEPTOR;
        okHttpClient.interceptors().add(interceptor);
        okHttpClient.networkInterceptors().add(interceptor);
        return okHttpClient.build();
    }
    @Inject
    public MySingleton2(@ApplicationContext Context context) {

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URl)
                .client(createCachedClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public Api init() {
        return retrofit.create(Api.class);
    }

    public interface Api {
        @GET("todos/1")
        Call<ResponseBody> getPoData();
    }
}