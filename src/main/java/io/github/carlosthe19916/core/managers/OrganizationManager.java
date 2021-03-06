package io.github.carlosthe19916.core.managers;

import io.github.carlosthe19916.core.keys.KeyProvider;
import io.github.carlosthe19916.core.models.*;
import io.github.carlosthe19916.core.models.utils.DefaultKeyProviders;
import io.github.carlosthe19916.core.models.utils.RepresentationToModel;
import io.github.carlosthe19916.core.representations.idm.OrganizationRepresentation;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.Set;

@Transactional
@ApplicationScoped
public class OrganizationManager {

    @Inject
    private RoleProvider roleProvider;

    @Inject
    private ComponentProvider componentProvider;

    @Inject
    private DefaultKeyProviders defaultKeyProviders;

    @Inject
    private OrganizationProvider organizationProvider;

    @Inject
    private RoleMembershipProvider roleMembershipProvider;

    public OrganizationModel createOrganization(OrganizationRepresentation representation, UserModel owner) {
        OrganizationType organizationType = Optional.ofNullable(representation.getType()).map(f -> OrganizationType.valueOf(f.toLowerCase())).orElse(OrganizationType.common);
        OrganizationModel organization = organizationProvider.addOrganization(representation.getName(), organizationType);
        RepresentationToModel.updateOrganization(representation, organization);

        // Memberships
        Set<RoleModel> autoGrantedRoles = roleProvider.getAutoGrantedRoles();
        if (autoGrantedRoles.isEmpty()) {
            throw new ModelRuntimeException("No auto grant roles available");
        }
        for (RoleModel role : autoGrantedRoles) {
            roleMembershipProvider.addRoleMembership(owner, role, organization);
        }

        // Certificate
        if (componentProvider.getComponents(organization, organization.getId(), KeyProvider.class.getName()).isEmpty()) {
            try {
                defaultKeyProviders.createProviders(organization);
            } catch (ModelException e) {
                throw new ModelRuntimeException("Could not create certificates", e);
            }
        }

        return organization;
    }
}
