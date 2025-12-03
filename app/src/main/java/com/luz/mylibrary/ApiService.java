package com.luz.mylibrary;

import com.google.gson.JsonObject;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
    @GET("search.json")
    Call<JsonObject> searchBookByTitle(@Query("title") String title);
}