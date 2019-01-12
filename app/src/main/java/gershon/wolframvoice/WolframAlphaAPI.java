package gershon.wolframvoice;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import gershon.wolframvoice.model.ResultPod;
import gershon.wolframvoice.parser.ResultPodXmlParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class WolframAlphaAPI {

    public static ArrayList<ResultPod> getQueryResult(String query) {

        String resultXml = makeRestCall(getFormattedUrl(query));

        if (resultXml != null)
            return ResultPodXmlParser.parseResultXml(resultXml, query);

        return null;
    }

    public static String getFormattedUrl(String query) {
        try {
            return Utils.WOLFRAM_BASE_URL + "input=" + URLEncoder.encode(query, "utf-8") + "&appid=" + BuildConfig.WOLFRAM_APP_ID;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String makeRestCall(String url) {

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = null;

        try {
            response = client.newCall(request).execute();
            if (response != null)
                return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }
}
