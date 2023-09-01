package food.backend.oauth.controller;


import food.backend.oauth.entity.KakaoToken;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/oauth/kakao")
@RequiredArgsConstructor
public class KakaoOauthController {

    private final String GRANT_TYPE = "authorization_code";
    private RestTemplate restTemplate = new RestTemplate();

    @Value("${oauth.kakao.client_id}")
    private String clientId;

    @Value("${oauth.kakao.redirect_uri}")
    private String redirectURI;

    @Value("${oauth.kakao.auth_uri}")
    private String authURI;
    @GetMapping("/authorize")
    public String redirectLoginPage() {

        StringBuilder kakaoLoginRedirectURIBuilder = new StringBuilder("redirect:" + authURI + "/oauth/authorize")
                .append("?response_type=code")
                .append("&client_id=" + clientId)
                .append("&redirect_uri=" + redirectURI);

        return kakaoLoginRedirectURIBuilder.toString();

    }

    @GetMapping("/callback")
    public String generateToken(@RequestParam("code") String authCode, HttpServletResponse httpServletResponse) {

        String tokenProviderURI = authURI + "/oauth/token";

        HttpHeaders httpHeaders = new HttpHeaders();

        httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> httpBody = new LinkedMultiValueMap<>();
        httpBody.add("code", authCode);
        httpBody.add("client_id", clientId);
        httpBody.add("grant_type", GRANT_TYPE);
        httpBody.add("redirect_uri", redirectURI);

        HttpEntity<?> request = new HttpEntity<>(httpBody, httpHeaders);

        KakaoToken kakaoToken = restTemplate.postForObject(tokenProviderURI, request, KakaoToken.class);

        Cookie accessTokenCookie = new Cookie("access_token", kakaoToken.getAccessToken());
        Cookie refreshTokenCookie = new Cookie("refresh_token", kakaoToken.getRefreshToken());

        accessTokenCookie.setMaxAge(3600);
        accessTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(3600);
        refreshTokenCookie.setPath("/");

        httpServletResponse.addCookie(accessTokenCookie);
        httpServletResponse.addCookie(refreshTokenCookie);

        return "redirect:http://localhost:8080";
    }

}


