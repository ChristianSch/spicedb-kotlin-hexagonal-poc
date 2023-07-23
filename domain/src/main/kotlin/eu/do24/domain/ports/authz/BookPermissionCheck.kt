package eu.do24.domain.ports.authz

data class BookPermissionCheck(val bookId: String, val userId: String)

interface CanAccessBookI {
    fun canAccessBook(permission: BookPermissionCheck): Boolean
}