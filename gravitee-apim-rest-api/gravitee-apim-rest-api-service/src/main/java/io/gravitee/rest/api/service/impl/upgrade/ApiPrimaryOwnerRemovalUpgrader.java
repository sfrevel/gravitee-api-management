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
package io.gravitee.rest.api.service.impl.upgrade;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.*;

import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.api.ApiRepository;
import io.gravitee.repository.management.api.EnvironmentRepository;
import io.gravitee.repository.management.api.GroupRepository;
import io.gravitee.repository.management.api.MembershipRepository;
import io.gravitee.repository.management.api.OrganizationRepository;
import io.gravitee.repository.management.api.RoleRepository;
import io.gravitee.repository.management.api.UserRepository;
import io.gravitee.repository.management.api.search.ApiCriteria;
import io.gravitee.repository.management.model.*;
import io.gravitee.rest.api.model.permissions.SystemRole;
import io.gravitee.rest.api.service.Upgrader;
import io.gravitee.rest.api.service.common.UuidString;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

/**
 * @author GraviteeSource Team
 */
@Component
public class ApiPrimaryOwnerRemovalUpgrader implements Upgrader, Ordered {

    private static final Logger LOG = LoggerFactory.getLogger(ApiPrimaryOwnerRemovalUpgrader.class);

    private final RoleRepository roleRepository;

    private final ApiRepository apiRepository;

    private final MembershipRepository membershipRepository;

    private final OrganizationRepository organizationRepository;

    private final EnvironmentRepository environmentRepository;

    private final UserRepository userRepository;

    private final GroupRepository groupRepository;

    @Value("${services.api-primary-owner-default:}")
    private String defaultPrimaryOwnerId;

    public ApiPrimaryOwnerRemovalUpgrader(
        @Lazy RoleRepository roleRepository,
        @Lazy ApiRepository apiRepository,
        @Lazy MembershipRepository membershipRepository,
        @Lazy OrganizationRepository organizationRepository,
        @Lazy EnvironmentRepository environmentRepository,
        @Lazy UserRepository userRepository,
        @Lazy GroupRepository groupRepository
    ) {
        this.roleRepository = roleRepository;
        this.apiRepository = apiRepository;
        this.membershipRepository = membershipRepository;
        this.organizationRepository = organizationRepository;
        this.environmentRepository = environmentRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public boolean upgrade() {
        try {
            Set<Organization> organizations = organizationRepository.findAll();
            for (Organization org : organizations) {
                checkOrganization(org.getId());
            }
            return true;
        } catch (Exception e) {
            LOG.error("Failed to fix APIs Primary Owner removal", e);
            return true;
        }
    }

    private void checkOrganization(String organizationId) throws TechnicalException {
        String apiPrimaryOwnerRoleId = findApiPrimaryOwnerRoleId(organizationId);
        List<String> environmentIds = findEnvironmentIds(organizationId);
        List<String> apiIds = apiRepository.searchIds(new ApiCriteria.Builder().environments(environmentIds).build());
        List<String> unCorruptedApiIds = findApiPrimaryOwnerReferenceIds(apiPrimaryOwnerRoleId, apiIds);
        if (shouldFix(apiIds, unCorruptedApiIds)) {
            ArrayList<String> corruptedApiIds = new ArrayList<>(apiIds);
            corruptedApiIds.removeAll(unCorruptedApiIds);
            warnOrFix(corruptedApiIds, apiPrimaryOwnerRoleId);
        }
    }

    private boolean shouldFix(List<String> apiIds, List<String> unCorruptedApiIds) {
        return apiIds.size() > unCorruptedApiIds.size();
    }

    private void warnOrFix(List<String> apiIds, String apiPrimaryOwnerRoleId) throws TechnicalException {
        if (isEmpty(defaultPrimaryOwnerId)) {
            warn(apiIds);
        } else {
            warnAndFix(apiIds, apiPrimaryOwnerRoleId);
        }
    }

    private void warn(List<String> apiIds) {
        LOG.warn("");
        LOG.warn("##############################################################");
        LOG.warn("#                           WARNING                          #");
        LOG.warn("##############################################################");
        LOG.warn("");
        LOG.warn("The following APIs do not have a Primary Owner:");
        LOG.warn("");
        apiIds.forEach(LOG::warn);
        LOG.warn("");
        LOG.warn("This can be fixed by editing the services.api-primary-owner-default property of your configuration file");
        LOG.warn("This value must refer to a valid user or group ID");
        LOG.warn("");
        LOG.warn("##############################################################");
        LOG.warn("");
    }

    private void warnAndFix(List<String> apiIds, String apiPrimaryOwnerRoleId) throws TechnicalException {
        warn(apiIds);
        LOG.info("Attempting to fix APIs without a Primary Owner from configuration");
        Membership membership = prepareMembership(apiPrimaryOwnerRoleId);
        for (String apiId : apiIds) {
            membership.setId(UuidString.generateRandom());
            membership.setReferenceId(apiId);
            membershipRepository.create(membership);
        }
        String memberType = membership.getMemberType().name().toLowerCase();
        LOG.info("APIs without a Primary Owner has been associated with {} {}", memberType, defaultPrimaryOwnerId);
    }

    @Override
    public int getOrder() {
        return 140;
    }

    private String findApiPrimaryOwnerRoleId(String organizationId) throws TechnicalException {
        return roleRepository
            .findByScopeAndNameAndReferenceIdAndReferenceType(
                RoleScope.API,
                SystemRole.PRIMARY_OWNER.name(),
                organizationId,
                RoleReferenceType.ORGANIZATION
            )
            .map(Role::getId)
            .orElseThrow(() -> new TechnicalException("Unable to find API Primary Owner role for organization " + organizationId));
    }

    private List<String> findEnvironmentIds(String organizationId) throws TechnicalException {
        return environmentRepository.findByOrganization(organizationId).stream().map(Environment::getId).collect(toList());
    }

    private List<String> findApiPrimaryOwnerReferenceIds(String apiPrimaryOwnerRoleId, List<String> apiIds) throws TechnicalException {
        return membershipRepository
            .findByReferencesAndRoleId(MembershipReferenceType.API, apiIds, apiPrimaryOwnerRoleId)
            .stream()
            .map(Membership::getReferenceId)
            .collect(toList());
    }

    private Membership prepareMembership(String poRoleId) throws TechnicalException {
        Optional<User> optUser = userRepository.findById(defaultPrimaryOwnerId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            return membership(user.getId(), MembershipMemberType.USER, poRoleId);
        }

        return groupRepository
            .findById(defaultPrimaryOwnerId)
            .map(group -> membership(group.getId(), MembershipMemberType.GROUP, poRoleId))
            .orElseThrow(() -> new TechnicalException("Unable to find a user or group with id " + defaultPrimaryOwnerId));
    }

    private static Membership membership(String memberId, MembershipMemberType memberType, String roleId) {
        return new Membership(null, memberId, memberType, null, MembershipReferenceType.API, roleId);
    }
}
