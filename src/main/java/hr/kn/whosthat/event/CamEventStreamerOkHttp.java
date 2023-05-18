package hr.kn.whosthat.event;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CamEventStreamerOkHttp {

    private static final Logger logger = LoggerFactory.getLogger(CamEventStreamerOkHttp.class);

    private final static Map<String, Long> thresholds = new HashMap<>(Map.of(
        "videoloss", 0L,
        "motion", 0L));

    public static void main(String[] args) throws IOException {
        var builder = new OkHttpClient.Builder();
        var authCache = new ConcurrentHashMap<String, CachingAuthenticator>();

        var credentials = new Credentials("a", "b");
        var digestAuthenticator = new DigestAuthenticator(credentials);

        var client = builder
            .authenticator(new CachingAuthenticatorDecorator(digestAuthenticator, authCache))
            .addInterceptor(new AuthenticationCacheInterceptor(authCache))
            //    .addNetworkInterceptor(logger)
            .build();

        var url = "http://192.168.6.20:65002/ISAPI/Event/notification/alertStream";
        var request = new Request.Builder().url(url).get().build();

        var response = client.newCall(request).execute();

        var stringBuilder = new StringBuilder();
        while (true) {
            var ch = (char) response.body().source().readByte();
            if (ch == '\n' && stringBuilder.toString().contains("<eventType>videoloss</eventType>") &&
                System.currentTimeMillis() - thresholds.get("videoloss") > 10000) {
                thresholds.put("videoloss", System.currentTimeMillis());
                logger.info("Videoloss alarm");
                stringBuilder.setLength(0);
            } else if (ch == '\n' && stringBuilder.toString().contains("<eventType>VMD</eventType>")) {
                thresholds.put("motion", System.currentTimeMillis());
                logger.info("Motion alarm");
                stringBuilder.setLength(0);
            } else {
                stringBuilder.append(ch);
            }
        }
    }

}
