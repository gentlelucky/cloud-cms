package com.hq.cloud.oauth2server.config;

import com.hq.cloud.oauth2server.service.CustomUserDetailsService;
import com.hq.cloud.oauth2server.util.BCryptUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

/**
 * @author Administrator
 * @Package com.hq.cloud.oauth2server.config
 * @Description: 认证服务器
 * @date 2018/4/12 15:16
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    private Logger log = LoggerFactory.getLogger(AuthorizationServerConfig.class);
    /**
     * 客户端ID
     */
    @Value("${security.oauth2.clientId}")
    private String clientId;
    /**
     * 客户端安全码
     */
    @Value("${security.oauth2.clientSecret}")
    private String clientSecret;
    /**
     * 客户端范围
     */
    @Value("${security.oauth2.scope}")
    private String scope;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.allowFormAuthenticationForClients()
                .tokenKeyAccess("isAuthenticated()")
                .checkTokenAccess("permitAll()");
        log.info("AuthorizationServerSecurityConfigurer is complete");
    }

    /**
     * 配置客户端详情信息(Client Details)
     * clientId：（必须的）用来标识客户的Id。
     * secret：（需要值得信任的客户端）客户端安全码，如果有的话。
     * scope：用来限制客户端的访问范围，如果为空（默认）的话，那么客户端拥有全部的访问范围。
     * authorizedGrantTypes：此客户端可以使用的授权类型，默认为空。
     * authorities：此客户端可以使用的权限（基于Spring Security authorities）。
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("hq")
                .secret(BCryptUtil.encode("hq"))
                .scopes("xx")
                .authorizedGrantTypes("password", "refresh_token");
        log.info("ClientDetailsServiceConfigurer is complete!");
    }
    /**
     * 配置授权、令牌的访问端点和令牌服务
     * tokenStore：采用redis储存
     * accessTokenConverter：采用jwt方式生成token
     * authenticationManager:身份认证管理器, 用于"password"授权模式
     * userDetailsService: 配合身份认证管理器, 检查用户名密码有效性
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore())
                //.accessTokenConverter(accessTokenConverter())
                .authenticationManager(authenticationManager);
                //.userDetailsService(userDetailsService());
        log.info("AuthorizationServerEndpointsConfigurer is complete.");
    }

    @Bean
    public TokenStore tokenStore() {
        return new RedisTokenStore(redisConnectionFactory);
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new CustomUserDetailsService();
    }

    @Bean
    public AccessTokenConverter accessTokenConverter() {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        // 设置签名
        jwtAccessTokenConverter.setSigningKey("Hq");
        return jwtAccessTokenConverter;
    }
}
