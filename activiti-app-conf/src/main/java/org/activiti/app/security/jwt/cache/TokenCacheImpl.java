package org.activiti.app.security.jwt.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import javax.annotation.PostConstruct;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.activiti.app.security.WUCCUser;
import org.activiti.app.service.api.UserCache;
import org.activiti.app.service.idm.UserCacheImpl;
import org.activiti.engine.identity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class TokenCacheImpl implements TokenCache {
    private final Logger logger = LoggerFactory.getLogger(UserCacheImpl.class);

    @Autowired
    protected Environment environment;

    protected LoadingCache<String, CachedToken> tokenCache;

    @PostConstruct
    protected void initCache() {
        Long userCacheMaxSize = environment.getProperty("cache.users.max.size", Long.class);
        Long userCacheMaxAge = environment.getProperty("cache.users.max.age", Long.class);

        tokenCache = CacheBuilder.newBuilder().maximumSize(userCacheMaxSize != null ? userCacheMaxSize : 2048)
                .expireAfterAccess(userCacheMaxAge != null ? userCacheMaxAge : (30 * 60), TimeUnit.SECONDS).recordStats().build(new CacheLoader<String, CachedToken>() {

                    public CachedToken load(final String key) throws Exception {
                        return new CachedToken(null, true);
                    }

                });
    }

    public void putToken(String key, CachedToken cachedToken) {
        tokenCache.put(key, cachedToken);
    }

    public CachedToken getToken(String key) {
        return getToken(key, false, false, true); // always check validity by default
    }

    public CachedToken getToken(String key, boolean throwExceptionOnNotFound, boolean throwExceptionOnInactive, boolean checkValidity) {
        try {
            // The cache is a LoadingCache and will fetch the value itself
            CachedToken cachedToken = tokenCache.get(key);
            return cachedToken;

        } catch (ExecutionException e) {
            return null;
        } catch (UncheckedExecutionException uee) {

            // Some magic with the exceptions is needed:
            // the exceptions like UserNameNotFound and Locked cannot
            // bubble up, since Spring security will react on them otherwise
            if (uee.getCause() instanceof RuntimeException) {
                RuntimeException runtimeException = (RuntimeException) uee.getCause();

                if (runtimeException instanceof UsernameNotFoundException) {
                    if (throwExceptionOnNotFound) {
                        throw runtimeException;
                    } else {
                        return null;
                    }
                }

                if (runtimeException instanceof LockedException) {
                    if (throwExceptionOnNotFound) {
                        throw runtimeException;
                    } else {
                        return null;
                    }
                }

            }
            throw uee;
        }
    }

    @Override
    public void invalidate(String userId) {
        tokenCache.invalidate(userId);
    }

}
