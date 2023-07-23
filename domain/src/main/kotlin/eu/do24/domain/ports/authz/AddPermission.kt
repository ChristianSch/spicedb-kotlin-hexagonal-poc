package eu.do24.domain.ports.authz

interface AddPermission {
    fun addPermission(permission: Permission)
}