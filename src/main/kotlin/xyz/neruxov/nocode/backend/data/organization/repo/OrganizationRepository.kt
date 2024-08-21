package xyz.neruxov.nocode.backend.data.organization.repo

import org.springframework.data.jpa.repository.JpaRepository
import xyz.neruxov.nocode.backend.data.organization.model.Organization

interface OrganizationRepository : JpaRepository<Organization, Long>