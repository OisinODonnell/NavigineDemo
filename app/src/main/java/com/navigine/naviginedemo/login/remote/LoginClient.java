package com.navigine.naviginedemo.login.remote;

import com.navigine.naviginedemo.login.model.Login;
import com.navigine.naviginedemo.login.model.User;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Oisin on 8/23/2017.
 */

public interface LoginClient {

    String BASE_URL = "http://192.168.0.109:8080/";

    @GET("Login/{email}/{password}")
    Call<Login> getUser(@Path("email") String email, @Path("password") String password);

    class Factory {
        private static LoginClient service;

        public static LoginClient getInstance() {
            if(service == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(BASE_URL).build();
                service = retrofit.create(LoginClient.class);
                return service;
            } else {
                return service;
            }
        }
    }
}
