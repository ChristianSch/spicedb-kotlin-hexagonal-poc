package eu.do24.spicedb

import com.authzed.api.v1.Core.*
import com.authzed.api.v1.PermissionService.*
import com.authzed.api.v1.PermissionService.CheckPermissionResponse.Permissionship.PERMISSIONSHIP_HAS_PERMISSION
import com.authzed.api.v1.PermissionsServiceGrpc
import com.authzed.api.v1.PermissionsServiceGrpc.PermissionsServiceBlockingStub
import com.authzed.api.v1.SchemaServiceGrpc
import com.authzed.api.v1.SchemaServiceOuterClass
import com.authzed.grpcutil.BearerToken
import eu.do24.domain.ports.authz.AddPermission
import eu.do24.domain.ports.authz.Permission
import eu.do24.domain.ports.authz.PermissionCheckI
import io.grpc.ManagedChannelBuilder

data class SpiceDbPagination(val cursor: String?, val limit: Int)
class PaginatedResult<T>(val items: List<T>, val pagination: SpiceDbPagination)


/**
 * A client for the SpiceDB database. Automatically handles consistency by keeping track of the zed token internally.
 */
class SpiceDbClient(private val host: String, private val port: Int, private val token: String) : PermissionCheckI,
    AddPermission {
    private var permissionsService: PermissionsServiceBlockingStub
    private var schemaService: SchemaServiceGrpc.SchemaServiceBlockingStub
    private var zedToken: ZedToken? = null
        get() {
            println("get zedToken: $field")
            return field
        }
        set(value) {
            println("update zedToken: $value")
            field = value
        }

    init {
        val channel = ManagedChannelBuilder
            .forTarget("$host:$port")
            .apply {
                if (host.startsWith("localhost") || host.startsWith("0.0.0.0")) {
                    usePlaintext()
                } else {
                    useTransportSecurity()
                }
            }
            .build()

        val bearerToken = BearerToken(token)

        permissionsService = PermissionsServiceGrpc.newBlockingStub(channel).withCallCredentials(bearerToken)
        schemaService = SchemaServiceGrpc.newBlockingStub(channel).withCallCredentials(bearerToken)
    }

    /**
     * Add a permission to the SpiceDB database. Format:
     * `obj:objId#permission@subject:subjectId`
     *
     * Note that the [relation] is a relation, not the permission itself. I.e.:
     * `book->viewer` instead of `book->view`.
     *
     * Example:
     * ```java
     * addPermission("book", "123", "user", "456", "viewer")
     * ```
     */
    override fun addPermission(permission: Permission) {
        val request = WriteRelationshipsRequest.newBuilder()
            .addUpdates(
                RelationshipUpdate.newBuilder().setRelationship(
                    Relationship.newBuilder()
                        .setResource(
                            ObjectReference.newBuilder().setObjectType(permission.obj).setObjectId(permission.objId)
                                .build()
                        )
                        .setSubject(buildSubjectRef(permission.subject, permission.subjectId))
                        .setRelation(permission.permission)
                        .build()
                )
                    .setOperation(RelationshipUpdate.Operation.OPERATION_CREATE)
                    .build()
            ).build()

        val res = permissionsService.writeRelationships(request)
        // save zedToken
        zedToken = res.writtenAt
    }

    /**
     * Check if a permission is granted.
     *
     * Example:
     * ```java
     * checkPermission("book", "123", "user", "456", "view", ...)
     * ```
     */
    override fun checkPermission(permission: Permission): Boolean {
        val request = CheckPermissionRequest.newBuilder()
            .setConsistency(
                Consistency.newBuilder()
                    .setMinimizeLatency(true)
                    .setAtLeastAsFresh(zedToken)
                    .build()
            )
            .setResource(buildObjectRef(permission.obj, permission.objId))
            .setSubject(buildSubjectRef(permission.subject, permission.subjectId))
            .setPermission(permission.permission)
            .build()

        val response = permissionsService.checkPermission(request)

        // save zedToken
        zedToken = response.checkedAt

        return response.permissionship == PERMISSIONSHIP_HAS_PERMISSION
    }

    /**
     * Checks a batch of permissions. Note that this is calls a check for each permission.
     */
    fun checkPermissions(permissions: List<Permission>) = permissions.map { checkPermission(it) }.all { it }

    /**
     * Get all subjects that have a permission for a given object in a paginated manner.
     *
     * Note that if there are multiple relations granting the permission you may get duplicate ids.
     *
     * Example:
     * ```java
     * getSubjectsForRelation("book", "user", "123", "view")
     * ```
     */
    fun getSubjectsForRelation(
        objType: String,
        subject: String,
        subjectId: String,
        permission: String,
        pagination: SpiceDbPagination? = null
    ): PaginatedResult<String> {
        val request = LookupResourcesRequest.newBuilder()
            .setSubject(buildSubjectRef(subject, subjectId))
            .setPermission(permission)
            .setResourceObjectType(objType)
            .setConsistency(
                Consistency.newBuilder()
                    .setMinimizeLatency(true)
                    .setAtLeastAsFresh(zedToken)
                    .build()
            )
            .apply {
                if (pagination != null) {
                    // the first page has a null cursor
                    if (pagination.cursor != null) {
                        optionalCursor = Cursor.newBuilder().setToken(pagination.cursor).build()
                    }
                    optionalLimit = pagination.limit
                }
            }
            .build()

        val response = permissionsService.lookupResources(request)
        val results = mutableListOf<String>()
        var cursor: Cursor? = null

        while (response.hasNext()) {
            val next = response.next()
            cursor = next.afterResultCursor
            results.add(next.resourceObjectId)
            // also update zedToken
            zedToken = next.lookedUpAt
        }

        return PaginatedResult(
            results,
            SpiceDbPagination(cursor?.token, pagination?.limit ?: results.size)
        )
    }

    /**
     * Write the schema to the SpiceDB instance.
     *
     * Example:
     * ```java
     * val schema = """
     *     definition book {
     *         relation viewer: user
     *         permission view = viewer
     *     }
     *     definition user {}
     * """.trimIndent()
     *
     * writeSchema(schema)
     * ```
     */
    fun writeSchema(schema: String) {
        val request = SchemaServiceOuterClass.WriteSchemaRequest.newBuilder()
            .setSchema(schema)
            .build()

        schemaService.writeSchema(request)
    }

    /**
     * Get the current schema of the SpiceDB instance.
     */
    fun getSchema(): String? {
        val request = SchemaServiceOuterClass.ReadSchemaRequest.newBuilder()
            .build()

        return schemaService.readSchema(request).schemaText
    }

    private fun buildObjectRef(type: String, id: String) =
        ObjectReference.newBuilder()
            .setObjectType(type)
            .setObjectId(id)
            .build()

    private fun buildSubjectRef(type: String, id: String) =
        SubjectReference.newBuilder()
            .setObject(ObjectReference.newBuilder().setObjectType(type).setObjectId(id).build())
            .build()
}
