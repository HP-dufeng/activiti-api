/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.app.conf;

import javax.inject.Inject;

import org.activiti.app.security.AjaxAuthenticationFailureHandler;
import org.activiti.app.security.AjaxAuthenticationSuccessHandler;
import org.activiti.app.security.AjaxLogoutSuccessHandler;
//import org.activiti.app.security.CustomPersistentRememberMeServices;
import org.activiti.app.security.Http401UnauthorizedEntryPoint;
import org.activiti.app.security.jwt.JwtAuthFilter;
import org.activiti.app.security.jwt.JwtAuthenticationProvider;
import org.activiti.app.security.jwt.JwtUserDetailsService;
import org.activiti.app.security.jwt.TokenVerifyService;
import org.activiti.app.web.CORSFilter;
import org.activiti.app.web.CustomFormLoginConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Based on http://docs.spring.io/spring-security/site/docs/3.2.x/reference/htmlsingle/#multiple-httpsecurity
 * 
 * @author Joram Barrez
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
public class SecurityConfiguration {
	
	private static final Logger logger = LoggerFactory.getLogger(SecurityConfiguration.class);
	
	public static final String KEY_LDAP_ENABLED = "ldap.authentication.enabled";

    //
	// GLOBAL CONFIG
	//

	@Autowired
	private Environment env;

	@Autowired
	public void configureGlobal(AuthenticationManagerBuilder auth) {

		// Default auth (database backed)
		try {
			auth.authenticationProvider(dbAuthenticationProvider());
		} catch (Exception e) {
			logger.error("Could not configure authentication mechanism:", e);
		}
	}

	@Bean
	public TokenVerifyService userDetailsService() {
		JwtUserDetailsService userDetailsService = new JwtUserDetailsService();

		// Undocumented setting to configure the amount of time user data is cached before a new check for validity is made
		// Use <= 0 for always do a check
		userDetailsService.setUserValidityPeriod(env.getProperty("cache.users.recheck.period", Long.class, 30000L));

		return userDetailsService;
	}

	
	@Bean(name = "dbAuthenticationProvider")
	public AuthenticationProvider dbAuthenticationProvider() {


		JwtAuthenticationProvider daoAuthenticationProvider = new JwtAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(userDetailsService());

		return daoAuthenticationProvider;

	}
	
	//
	// REGULAR WEBAP CONFIG
	//
	
	@Configuration
	@Order(10) // API config first (has Order(1))
    public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

		private static final Logger logger = LoggerFactory.getLogger(FormLoginWebSecurityConfigurerAdapter.class);

	    @Inject
	    private Environment env;

	    @Inject
	    private AjaxAuthenticationSuccessHandler ajaxAuthenticationSuccessHandler;

	    @Inject
	    private AjaxAuthenticationFailureHandler ajaxAuthenticationFailureHandler;

	    @Inject
	    private AjaxLogoutSuccessHandler ajaxLogoutSuccessHandler;

	    @Inject
	    private Http401UnauthorizedEntryPoint authenticationEntryPoint;

	    @Override
	    protected void configure(HttpSecurity http) throws Exception {
	        http
//				.addFilterBefore(new CORSFilter(), ChannelProcessingFilter.class)
				.addFilterBefore(new JwtAuthFilter(), BasicAuthenticationFilter.class)
	            .exceptionHandling()
	                .authenticationEntryPoint(authenticationEntryPoint)
	                .and()
	            .sessionManagement()
	                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
	                .and()
	            .rememberMe()
//	                .rememberMeServices(rememberMeServices())
	                .key(env.getProperty("security.rememberme.key"))
	                .and()
	            .logout()
	                .logoutUrl("/app/logout")
	                .logoutSuccessHandler(ajaxLogoutSuccessHandler)
	                .deleteCookies("JSESSIONID")
	                .permitAll()
	                .and()
	            .csrf()
	                .disable() // Disabled, cause enabling it will cause sessions
	            .headers()
	                .frameOptions()
	                	.sameOrigin()
	                	.addHeaderWriter(new XXssProtectionHeaderWriter())
	                .and()
	            .authorizeRequests()
					.antMatchers(HttpMethod.OPTIONS, "**").permitAll()//allow CORS option calls
	                .antMatchers("/*").permitAll()
	                .antMatchers("/app/rest/authenticate").permitAll()
	                .antMatchers("/app/rest/integration/login").permitAll()
	                .antMatchers("/app/rest/temporary/example-options").permitAll()
	                .antMatchers("/app/rest/idm/email-actions/*").permitAll()
	                .antMatchers("/app/rest/idm/signups").permitAll()
	                .antMatchers("/app/rest/idm/passwords").permitAll()
					.regexMatchers("^/app/rest/models/(.*)/(thumbnail)(.*)").permitAll()
					.regexMatchers("^/app/rest/models/(.*)/(bpmn20)(.*)").permitAll()
	                .antMatchers("/app/**").authenticated();

	        // Custom login form configurer to allow for non-standard HTTP-methods (eg. LOCK)
//	        CustomFormLoginConfig<HttpSecurity> loginConfig = new CustomFormLoginConfig<HttpSecurity>();
//	        loginConfig.loginProcessingUrl("/app/authentication")
//	            .successHandler(ajaxAuthenticationSuccessHandler)
//	            .failureHandler(ajaxAuthenticationFailureHandler)
//	            .usernameParameter("j_username")
//	            .passwordParameter("j_password")
//	            .permitAll();

//	        http.apply(loginConfig);
	    }

//	    @Bean
//	    public RememberMeServices rememberMeServices() {
//	      return new CustomPersistentRememberMeServices(env, userDetailsService());
//	    }
//
//	    @Bean
//	    public RememberMeAuthenticationProvider rememberMeAuthenticationProvider() {
//	        return new RememberMeAuthenticationProvider(env.getProperty("security.rememberme.key"));
//	    }



	}


	//
	// BASIC AUTH
	//

	@Configuration
	@Order(1)
	public static class ApiWebSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

		protected void configure(HttpSecurity http) throws Exception {

			http
				.sessionManagement()
					.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
					.and()
				.csrf()
					.disable()
				.antMatcher("/api" + "/**")
				.authorizeRequests()
					.antMatchers("/api" + "/**").authenticated()
					.and().httpBasic();
		}
	}





//	public static class LdapAuthenticationEnabledCondition implements Condition {
//
//		@Override
//		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
//			return context.getEnvironment().getProperty(KEY_LDAP_ENABLED, Boolean.class, false);
//		}
//
//	}

}
