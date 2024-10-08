package practice.english.n2l.api

import com.google.gson.GsonBuilder
import practice.english.n2l.util.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitService
{
    companion object {

        val gson = GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .setLenient()
            .create()

        private val client = OkHttpClient.Builder().addInterceptor(
            HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(Constants.ConnectTimeout, TimeUnit.SECONDS)
            .readTimeout(Constants.ReadTimeout, TimeUnit.SECONDS)
            .build()

        private val retrofit: Retrofit by lazy {
            Retrofit.Builder()
                .baseUrl(Constants.URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
        }

        val service: ApiService by lazy {
            retrofit.create(ApiService::class.java)
        }
    }
}
