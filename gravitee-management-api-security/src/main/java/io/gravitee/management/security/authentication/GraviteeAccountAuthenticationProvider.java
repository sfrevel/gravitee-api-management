/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.management.security.authentication;

import io.gravitee.management.model.UserEntity;
import io.gravitee.management.service.UserService;
import io.gravitee.management.service.exceptions.UserNotFoundException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

/**
 * 
 * @author Titouan COMPIEGNE
 *
 */
public class GraviteeAccountAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(GraviteeAccountAuthenticationProvider.class);

	@Autowired
	private UserService userService;

	private PasswordEncoder passwordEncoder;

	public GraviteeAccountAuthenticationProvider() {
		setPasswordEncoder(NoOpPasswordEncoder.getInstance());
	}

	@Override
	protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		if (authentication.getCredentials() == null) {
			LOGGER.debug("Authentication failed: no credentials provided");
			throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
		}

		String presentedPassword = authentication.getCredentials().toString();

		if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
			LOGGER.debug("Authentication failed: password does not match stored value");
			throw new BadCredentialsException(messages.getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
		}
	}

	@Override
	protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
		try {
			UserEntity user = userService.findByName(username);
			return mapUserEntityToUserDetails(user);
		} catch (UserNotFoundException notFound) {
			throw new UsernameNotFoundException("User '" + username + "' not found", notFound);
		} catch (Exception repositoryProblem) {
			LOGGER.error("Failed to retrieveUser : {}", username, repositoryProblem);
			throw new InternalAuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
		}
	}

	public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	private UserDetails mapUserEntityToUserDetails(UserEntity userEntity) {
		List<GrantedAuthority> authorities = AuthorityUtils.NO_AUTHORITIES;
		if (userEntity.getRoles() != null && userEntity.getRoles().size() > 0) {
			authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils.join(userEntity.getRoles(), ','));
		}
		return new User(userEntity.getUsername(), "unknown", authorities);
	}

}
