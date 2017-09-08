package org.activiti.app.security.jwt;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public interface TokenVerifyService {
    UserDetails loadUserByAccessToken(String var1) throws UsernameNotFoundException;
}
