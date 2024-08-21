package xyz.neruxov.nocode.backend.data.organization.enums

enum class OrganizationRole {
    MEMBER,
    ADMIN,
    OWNER,
    ;

    fun isEqualOrHigherThan(role: OrganizationRole): Boolean {
        return this.ordinal >= role.ordinal
    }

}