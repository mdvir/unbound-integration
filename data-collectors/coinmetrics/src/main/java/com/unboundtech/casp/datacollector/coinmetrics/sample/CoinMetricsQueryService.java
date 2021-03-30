package com.unboundtech.casp.datacollector.coinmetrics.sample;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.util.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.HttpHeaders;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.concurrent.TimeUnit.*;

public class CoinMetricsQueryService {
    private final String accessToken;
    private final WebTarget assetMetricsTarget;


    public CoinMetricsQueryService(String accessToken) {
        this.accessToken = accessToken;
        Client client = ClientBuilder.newBuilder()
                .connectTimeout(3, SECONDS)
                .register(JacksonFeature.class)
                .build();

        assetMetricsTarget = client.target("https://api.coinmetrics.io/v4/timeseries/asset-metrics");
    }

    public BigInteger getUSDPriceForBTC(String startTime, String frequency) {
        CoinMetricsData coinMetricsData =  assetMetricsTarget
                .queryParam("assets", "btc")
                .queryParam("metrics", "PriceUSD")
                .queryParam("start_time", startTime)
                .queryParam("frequency", frequency)
                .queryParam("pretty", true)
                .queryParam("sort", "time")
                .queryParam("pretty", true)
                .queryParam("api_key", accessToken)
                .request()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .get(new GenericType<CoinMetricsData>(){});
        if(coinMetricsData != null && !coinMetricsData.data.isEmpty()) {
            return coinMetricsData.data
                    .stream()
                    .max(Comparator.comparing(CoinMetricsResponse::getTime))
                    .get()
                    .getPriceUSD();
        }else {
            return new BigInteger(String.valueOf(1L)).negate();
        }

    }



    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class CoinMetricsResponse {
        public String asset;
        public ZonedDateTime time;
        public BigInteger priceUSD;

        public CoinMetricsResponse(){}

        public String getAsset() {
            return asset;
        }

        public ZonedDateTime getTime() {
            return time;
        }

        public BigInteger getPriceUSD() {
            return priceUSD;
        }

        public void setAsset(String asset) {
            this.asset = asset;
        }

        public void setTime(ZonedDateTime time) {
            this.time = time;
        }

        public void setPriceUSD(BigInteger priceUSD) {
            this.priceUSD = priceUSD;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public static class CoinMetricsData {
        public List<CoinMetricsResponse> data = new ArrayList<>();
    }

}
