package com.example.shop.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
@Profile("!registration-test")
public class WebhookRegistrationDelivery implements RegistrationDelivery {
    @Value("${registration.delivery-url:}") private String url;
    @Value("${registration.delivery-token:}") private String token;

    public void send(String channel, String identifier, String code) {
        if (!url.startsWith("https://") || token.isEmpty()) {
            throw new IllegalStateException("验证码投递服务尚未配置");
        }
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);
        factory.setReadTimeout(5000);
        Map<String, Object> body = new HashMap<>();
        body.put("channel", channel);
        body.put("identifier", identifier);
        body.put("code", code);
        body.put("purpose", "registration");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        ResponseEntity<Map> response = new RestTemplate(factory).postForEntity(url, new HttpEntity<>(body, headers), Map.class);
        if (response.getBody() == null || !Boolean.TRUE.equals(response.getBody().get("succeeded"))) {
            throw new IllegalStateException("验证码投递失败");
        }
    }
}
