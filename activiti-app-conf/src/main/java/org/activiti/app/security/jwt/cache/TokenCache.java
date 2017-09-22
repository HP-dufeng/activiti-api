package org.activiti.app.security.jwt.cache;

import org.activiti.app.security.WUCCUser;

public interface TokenCache {
    CachedToken getToken(String key);

    CachedToken getToken(String key, boolean throwExceptionOnNotFound, boolean throwExceptionOnInactive, boolean checkValidity);

    void putToken(String key, CachedToken cachedToken);

    void invalidate(String key);

    public static class CachedToken {

        private WUCCUser user;

        private boolean expired;

        public CachedToken(WUCCUser user, boolean expired) {
            this.user = user;
            this.expired = expired;
        }

        public WUCCUser getUser() {
            return user;
        }

        public void setUser(WUCCUser user) {
            this.user = user;
        }

        public void setExpired(boolean expired) {
            this.expired = expired;
        }

        public boolean isExpired() {
            return expired;
        }
    }
}
