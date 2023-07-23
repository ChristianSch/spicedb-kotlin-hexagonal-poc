package eu.do24.domain.ports.authz


interface PermissionCheckI {
    fun checkPermission(permission: Permission): Boolean
}