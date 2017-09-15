package org.activiti.app.security.jwt;


import java.util.ArrayList;
import java.util.Collection;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.activiti.app.security.ActivitiAppUser;
import org.activiti.app.security.AuthoritiesConstants;
import org.activiti.app.security.CustomUserDetailService;

import org.activiti.app.security.WUCCUser;
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
public class JwtUserDetailsService implements TokenVerifyService, CustomUserDetailService {

        @Autowired
        private UserCache userCache;


        @Autowired
        private Environment env;

        private long userValidityPeriod;



        @Transactional
        public UserDetails loadUserByAccessToken(String accessToken) {

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

                // Adding it manually to cache
                userCache.putUser(user.getId(), new CachedUser(user, grantedAuthorities));

                // Set authentication globally for Activiti
                Authentication.setAuthenticatedUserId(String.valueOf(user.getId()));

                return new ActivitiAppUser(user, user.getId(), grantedAuthorities);
        }

        @Override
        @Transactional
        public UserDetails loadUserByUsername(final String login) {
                return null;
        }

        @Transactional
        public UserDetails loadByUserId(final String userId) {

                CachedUser cachedUser = userCache.getUser(userId, true, true, false); // Do not check for validity. This would lead to A LOT of db requests! For login, there is a validity period (see below)
                if (cachedUser == null) {
                        throw new UsernameNotFoundException("User " + userId + " was not found in the database");
                }

                long lastDatabaseCheck = cachedUser.getLastDatabaseCheck();
                long currentTime = System.currentTimeMillis(); // No need to create a Date object. The Date constructor simply calls this method too!

                if (userValidityPeriod <= 0L || (currentTime - lastDatabaseCheck >= userValidityPeriod)) {

                        userCache.invalidate(userId);
                        cachedUser = userCache.getUser(userId, true, true, false); // Fetching it again will refresh data

                        cachedUser.setLastDatabaseCheck(currentTime);
                }

                // The Spring security docs clearly state a new instance must be returned on every invocation
                User user = cachedUser.getUser();
                String actualUserId = user.getId();

                // Set authentication globally for Activiti
                Authentication.setAuthenticatedUserId(String.valueOf(user.getId()));

                return new ActivitiAppUser(cachedUser.getUser(), actualUserId, cachedUser.getGrantedAuthorities());
        }

        public void setUserValidityPeriod(long userValidityPeriod) {
                this.userValidityPeriod = userValidityPeriod;
        }
}
