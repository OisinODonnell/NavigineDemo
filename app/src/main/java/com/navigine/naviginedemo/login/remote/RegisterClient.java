package com.navigine.naviginedemo.login.remote;

import com.navigine.naviginedemo.login.model.Login;
import com.navigine.naviginedemo.login.model.Register;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Oisin on 8/24/2017.
 */

public interface RegisterClient {

    String BASE_URL = "http://192.168.0.109:8080/";

    @GET("Login/register/Mobile/{firstname}/{surname}/{password}/{phone}/{gender}/{yob}/{emailAddress}")
    Call<Register> getRegister(@Path("firstname") String firstname,
                               @Path("surname") String surname,
                               @Path("password") String password,
                               @Path("phone") String phone,
                               @Path("gender") String gender,
                               @Path("yob") int yob,
                               @Path("emailAddress") String emailAddress);

    // Take into account that the yob may need to be handled differently here.

    class Factory {
        private static RegisterClient service;

        public static RegisterClient getInstance() {
            if(service == null) {
                Retrofit retrofit = new Retrofit.Builder()
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(BASE_URL).build();
                service = retrofit.create(RegisterClient.class);
                return service;
            } else {
                return service;
            }
        }
    }
}
