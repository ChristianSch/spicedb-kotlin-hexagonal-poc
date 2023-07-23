package eu.do24.domain.ports.authz

data class Permission(
    val obj: String,
    val objId: String,
    val subject: String,
    val subjectId: String,
    val permission: String,
)