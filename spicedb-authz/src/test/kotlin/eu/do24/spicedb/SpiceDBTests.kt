package eu.do24.spicedb

import assertk.assertThat
import assertk.assertions.*
import io.grpc.StatusRuntimeException
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName


@Testcontainers
class SpiceDBTests {
    private val token = "some-random-key-here"

    @Container
    var container: GenericContainer<*> = GenericContainer(DockerImageName.parse("authzed/spicedb:v1.23.1"))
        .withExposedPorts(50051)
        .withCommand("serve-testing")
        .withReuse(false)
        .waitingFor(Wait.forLogMessage(".*grpc server started serving.*", 2))

    private fun getPort(): Int {
        return container.getMappedPort(50051)!!
    }

    @Test
    fun `add permission`() {
        // set up adapters
        val spiceDB = SpiceDbClient("0.0.0.0", getPort(), token)

        // Given
        // A user
        val user = "user-1"

        // And a schema
        val schema = """
            definition book {
                relation viewer: user
                permission view = viewer
            }
            
            definition user {}
        """.trimIndent()

        spiceDB.writeSchema(schema)

        // When
        // We add a permission to access a book
        spiceDB.addPermission("book", "1", "user", user, "viewer")

        // Then
        // SpiceDB has the permission
        assertThat(spiceDB.checkPermission(Permission("book", "1", "user", user, "view"))).isTrue()
    }

    @Nested
    inner class CheckPermission {
        @Test
        fun `check permission`() {
            // set up adapters
            val spiceDB = SpiceDbClient("0.0.0.0", getPort(), token)

            // Given
            // A user
            val user = "user-1"

            // And a schema
            val schema = """
            definition book {
                relation viewer: user
                permission view = viewer
            }
            
            definition user {}
        """.trimIndent()

            spiceDB.writeSchema(schema)

            // That has a permission to access a book
            spiceDB.addPermission("book", "1", "user", user, "viewer")

            // When
            // We check if a user has permission to access a book
            val result = spiceDB.checkPermission(Permission("book", "1", "user", user, "view"))

            // Then
            // The permission check returns true
            assertThat(result).isTrue()
        }

        @Test
        fun `check permission fails for undefined relation or permission`() {
            // set up adapters
            val spiceDB = SpiceDbClient("0.0.0.0", getPort(), token)

            // Given
            // A user
            val user = "user-1"

            // And a schema
            val schema = """
            definition book {
                relation viewer: user
                permission view = viewer
            }
            
            definition user {}
        """.trimIndent()

            spiceDB.writeSchema(schema)

            // That has a permission to access a book
            spiceDB.addPermission("book", "1", "user", user, "viewer")

            // When
            // We check if a user has permission to access a book
            val e = assertThrows<StatusRuntimeException> {
                spiceDB.checkPermission(Permission("book", "1", "user", user, "read"))
            }

            // Then
            // The permission check returns an error
            assertThat(e.message).isEqualTo("FAILED_PRECONDITION: relation/permission `read` not found under definition `book`")
        }
    }

    @Nested
    inner class CheckPermissions {
        @Test
        fun `should work if all subjects are viewable`() {
            // set up adapters
            val spiceDB = SpiceDbClient("0.0.0.0", getPort(), token)

            // Given
            // A user
            val user = "user-1"

            // And a schema
            val schema = """
            definition book {
                relation viewer: user
                permission view = viewer
            }
            
            definition user {}
        """.trimIndent()

            spiceDB.writeSchema(schema)

            // When
            // We add a permission to access book 1
            spiceDB.addPermission("book", "1", "user", user, "viewer")

            // And book 2
            spiceDB.addPermission("book", "2", "user", user, "viewer")

            // Then
            // The permission check returns true
            assertThat(
                spiceDB.checkPermissions(
                    listOf(
                        Permission("book", "1", "user", user, "view"),
                        Permission("book", "2", "user", user, "view"),
                    ),
                )
            ).isTrue()
        }

        @Test
        fun `should not work if not all subjects are viewable`() {
            // set up adapters
            val spiceDB = SpiceDbClient("0.0.0.0", getPort(), token)

            // Given
            // A user
            val user = "user-1"

            // And a schema
            val schema = """
            definition book {
                relation viewer: user
                permission view = viewer
            }
            
            definition user {}
        """.trimIndent()

            spiceDB.writeSchema(schema)

            // When
            // We add a permission to access book 1
            spiceDB.addPermission("book", "1", "user", user, "viewer")

            // Then
            // The permission check is false because the user isn't allowed to view book:2
            assertThat(
                spiceDB.checkPermissions(
                    listOf(
                        Permission("book", "1", "user", user, "view"),
                        Permission("book", "2", "user", user, "view"),
                    ),
                )
            ).isFalse()
        }
    }

    @Nested
    inner class GetSubjectsForRelation {
        @Test
        fun `get all subjects for existing relations`() {
            // set up adapters
            val spiceDB = SpiceDbClient("0.0.0.0", getPort(), token)

            // Given
            // A user
            val user = "user-1"

            // And a schema
            val schema = """
                definition book {
                    relation viewer: user
                    permission view = viewer
                }
                
                definition user {}
            """.trimIndent()

            spiceDB.writeSchema(schema)

            // And permissions
            for (i in 1..10) {
                spiceDB.addPermission("book", i.toString(), "user", user, "viewer")
            }

            // When
            // We get all subjects for the relation
            val result = spiceDB.getSubjectsForRelation("book", "user", user, "viewer", null)

            // Then
            // SpiceDB returns all subjects
            assertThat(result.items).containsAll(*(1..9).map { it.toString() }.toTypedArray())

            // And pagination
            assertThat(result.pagination.cursor).isNotNull()
        }

        @Test
        fun `get page of subjects for existing relations`() {
            // set up adapters
            val spiceDB = SpiceDbClient("0.0.0.0", getPort(), token)

            // Given
            // A user
            val user = "user-1"

            // And a schema
            val schema = """
                definition book {
                    relation viewer: user
                    permission view = viewer
                }
                
                definition user {}
            """.trimIndent()

            spiceDB.writeSchema(schema)

            // And permissions
            for (i in 1..10) {
                spiceDB.addPermission("book", i.toString(), "user", user, "viewer")
            }

            // When
            // We get a page of subjects for the relation
            val page = SpiceDbPagination(null, 5)
            val result = spiceDB.getSubjectsForRelation("book", "user", user, "viewer", page)

            // Then
            // The number of returned subjects is as defined
            // We can't test the exact subjects because it's non-deterministic
            assertThat(result.items.size).isEqualTo(page.limit)

            // And pagination is not null, because there are more pages
            assertThat(result.pagination.cursor).isNotNull()
        }

        @Test
        fun `get second page of subjects for existing relations`() {
            // set up adapters
            val spiceDB = SpiceDbClient("0.0.0.0", getPort(), token)

            // Given
            // A user
            val user = "user-1"

            // And a schema
            val schema = """
                definition book {
                    relation viewer: user
                    permission view = viewer
                }
                
                definition user {}
            """.trimIndent()

            spiceDB.writeSchema(schema)

            // And permissions
            for (i in 1..10) {
                spiceDB.addPermission("book", i.toString(), "user", user, "viewer")
            }

            // And a first page
            val firstPage = spiceDB.getSubjectsForRelation("book", "user", user, "viewer", SpiceDbPagination(null, 5))

            // When
            // We get a page of subjects for the relation
            val secondPage = spiceDB.getSubjectsForRelation(
                "book",
                "user",
                user,
                "viewer",
                SpiceDbPagination(firstPage.pagination.cursor, 5)
            )

            // Then
            // The number of returned subjects is as defined
            assertThat(secondPage.items.size).isEqualTo(5)

            // Both pages hold all subjects
            assertThat(firstPage.items + secondPage.items).containsExactlyInAnyOrder(*(1..10).map { it.toString() }
                .toTypedArray())

            // And the cursor is not null
            assertThat(secondPage.pagination.cursor).isNotNull()

            // And the next page is empty
            val thirdPage = spiceDB.getSubjectsForRelation(
                "book",
                "user",
                user,
                "viewer",
                SpiceDbPagination(secondPage.pagination.cursor, 5)
            )
            assertThat(thirdPage.items).isEmpty()
        }
    }


    @Test
    fun `add schema`() {
        // Given
        // An adapter
        val spiceDB = SpiceDbClient("0.0.0.0", getPort(), token)

        // And a schema
        val schema = """
            definition book {
                relation viewer: user
                permission view = viewer
            }

            definition user {}
        """.trimIndent()

        // When
        // We add the schema
        spiceDB.writeSchema(schema)

        // Then
        // The schema is added
        assertThat(spiceDB.getSchema()).isEqualTo(schema.replace("    ", "\t"))
    }
}