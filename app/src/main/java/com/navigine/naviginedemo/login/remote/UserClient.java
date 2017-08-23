package com.navigine.naviginedemo.login.remote;

import com.navigine.naviginedemo.login.model.User;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Oisin on 8/23/2017.
 */

public interface UserClient {


    String BASE_URL = "http://192.168.56.1:8080/";


    @GET("Login/{email}/{password}")
    Call<User> getUser(@Path("email") String email, @Path("password") String password);

    class Factory {
        public static UserClient service;

        public static UserClient getInstance() {
            if(service == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(BASE_URL).build();
                service = retrofit.create(UserClient.class);
                return service;
            } else {
                return service;
            }
        }
    }
}
