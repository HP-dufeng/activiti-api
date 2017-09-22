package org.activiti.app.security.jwt;


import java.util.ArrayList;
import java.util.Collection;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.activiti.app.security.ActivitiAppUser;
import org.activiti.app.security.AuthoritiesConstants;
import org.activiti.app.security.CustomUserDetailService;

import org.activiti.app.security.WUCCUser;
import org.activiti.app.security.jwt.cache.TokenCache;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.api.UserCache.CachedUser;
import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.engine.impl.identity.Authentication;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

/**
 * This class is called AFTER successful authentication, to populate the user object with additional details The default (no ldap) way of authentication is a bit hidden in Spring Security magic. But
 * basically, the user object is fetched from the db and the hashed password is compared with the hash of the provided password (using the Spring {@link StandardPasswordEncoder}).
 */
public class JwtUserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService, TokenVerifyService {

      @Autowired
  private UserCache userCache;

        @Autowired
        private TokenCache tokenCache;

        @Autowired
        private Environment env;

        private WUCCUser verfiryToken (String accessToken){
                String token_verify_url =  env.getProperty("token_verify_url");

                RestTemplate restTemplate = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.set("Authorization", "Bearer " + accessToken);

                HttpEntity<?> entity = new HttpEntity<>(headers);

                HttpEntity<WUCCUser> response = restTemplate.exchange(
                        token_verify_url,
                        HttpMethod.GET,
                        entity,
                        WUCCUser.class);

                WUCCUser user = response.getBody();

                return user;
        }




        @Transactional
        public UserDetails loadUserByAccessToken(String accessToken) {

                WUCCUser user;

                TokenCache.CachedToken token = tokenCache.getToken(accessToken);
                if(!token.isExpired()){
                    user = token.getUser();
                }
                else {
                        user = verfiryToken(accessToken);

                        // Adding it manually to cache
                        tokenCache.putToken(accessToken, new TokenCache.CachedToken(user, false));
                }

                // Add capabilities to user object
                Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

                // add default authority
                grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));

                String[] roles = new String[user.getRole().size()];
                user.getRole().toArray(roles);

                grantedAuthorities.addAll(AuthorityUtils.createAuthorityList(roles));
                // check if user is in super user group

                if(user.getSuperAdmin()!=null && user.getSuperAdmin().equals("True")) {
                        grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
                }


                // Set authentication globally for Activiti
                Authentication.setAuthenticatedUserId(String.valueOf(user.getId()));

                return new ActivitiAppUser(user, user.getId(), grantedAuthorities);
        }

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        WUCCUser user;

        TokenCache.CachedToken token = tokenCache.getToken(userName);
        if(!token.isExpired()){
            user = token.getUser();
        }
        else {
            throw new UsernameNotFoundException("User " + userName + " was not found or expired");
        }

        // Add capabilities to user object
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<GrantedAuthority>();

        // add default authority
        grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.USER));

        String[] roles = new String[user.getRole().size()];
        user.getRole().toArray(roles);

        grantedAuthorities.addAll(AuthorityUtils.createAuthorityList(roles));
        // check if user is in super user group

        if(user.getSuperAdmin()!=null && user.getSuperAdmin().equals("True")) {
            grantedAuthorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ADMIN));
        }


        // Set authentication globally for Activiti
        Authentication.setAuthenticatedUserId(String.valueOf(user.getId()));

        return new ActivitiAppUser(user, user.getId(), grantedAuthorities);
    }
}
