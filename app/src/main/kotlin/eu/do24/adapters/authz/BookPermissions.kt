package eu.do24.adapters.authz

import eu.do24.domain.ports.authz.BookPermissionCheck
import eu.do24.domain.ports.authz.CanAccessBookI
import eu.do24.domain.ports.authz.Permission
import eu.do24.domain.ports.authz.PermissionCheckI

/**
 * This is a simple adapter that uses the SpiceDB client to check permissions.
 */
class BookPermissions(private val checker: PermissionCheckI) : CanAccessBookI {
    override fun canAccessBook(permission: BookPermissionCheck): Boolean {
        return checker.checkPermission(Permission("user", permission.userId, "book", permission.bookId, "view"))
    }
}