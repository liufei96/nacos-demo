package com.liufei.nacos.nacosdemo.utils;

import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.io.IOException;
import java.net.URI;

public class HttpClientUsg {
	public String requestByGetMethod(String url) throws IOException, InterruptedException {
		HttpClient httpClient = HttpClient.newBuilder().build();

		HttpRequest request = HttpRequest.newBuilder(URI.create(url))
//				.header("Content-Type", "application/json")
				.version(HttpClient.Version.HTTP_2)
				.GET()
				.build();

		HttpResponse httpResponse = httpClient.send(request, HttpResponse.BodyHandler.asString());
		System.out.println(httpResponse.toString());
		System.out.println(httpResponse.headers().toString());
		System.out.println(httpResponse.body().toString());
		return url;

	}

	public static void main(String args[]) {
		HttpClientUsg httpClientUtil = new HttpClientUsg();
		String url = "https://localhost:8444/ping";
		String res = null;
		try {
			res = httpClientUtil.requestByGetMethod(url).split("/n")[0];
			System.out.println(res);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}