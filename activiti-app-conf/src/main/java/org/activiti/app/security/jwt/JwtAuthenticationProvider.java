package org.activiti.app.security.jwt;

import org.activiti.app.security.ActivitiAppUser;
import org.activiti.app.security.WUCCUser;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

public class JwtAuthenticationProvider implements AuthenticationProvider,
                                                  InitializingBean {
    private TokenVerifyService tokenVerifyService;


    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        try {
            String accessToken = (String)authentication.getPrincipal();
            ActivitiAppUser appUser = (ActivitiAppUser)tokenVerifyService.loadUserByAccessToken(accessToken);
            WUCCUser user = (WUCCUser) appUser.getUserObject();

            JwtAuthenticationToken jwtAuthenticationToken = new JwtAuthenticationToken(appUser,"",appUser.getAuthorities());

            return jwtAuthenticationToken;
        } catch (Exception e) {
            throw new JwtAuthenticationException("Failed to verify token", e);
        }

    }

    public boolean supports(Class<?> authentication) {
        return PreAuthenticatedAuthenticationToken.class.equals(authentication);
    }

    public void setUserDetailsService(TokenVerifyService tokenVerifyService) {
        this.tokenVerifyService = tokenVerifyService;
    }

    protected TokenVerifyService getUserDetailsService() {
        return this.tokenVerifyService;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(this.tokenVerifyService, "A JwtUserDetailsService must be set.");
    }
}
