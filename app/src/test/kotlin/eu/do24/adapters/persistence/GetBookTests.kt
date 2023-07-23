package eu.do24.adapters.persistence

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import eu.do24.domain.ports.authz.BookPermissionCheck
import eu.do24.domain.ports.authz.CanAccessBookI
import eu.do24.domain.usecases.books.GetBook
import eu.do24.domain.ports.repos.Book
import eu.do24.domain.ports.repos.BookRepositoryI
import org.junit.jupiter.api.Test

/**
 * This test verifies that the GetBook use case works as expected.
 */
class GetBookTests {
    // mock adapters
    private class CheckBook(private val userIdsWithPermission: List<String>) : CanAccessBookI {
        override fun canAccessBook(permission: BookPermissionCheck): Boolean {
            return userIdsWithPermission.contains(permission.userId)
        }
    }

    private class BookRepository : BookRepositoryI {
        // not relevant for this test
        override fun getBatch(batchSize: Int, offset: Int): List<Book> {
            TODO("Not yet implemented")
        }

        override fun getById(id: Int): Book {
            return Book(id = id, title = "The Hobbit", author = "J.R.R. Tolkien", year = 1937)
        }
    }

    @Test
    fun `should return book with correct data`() {
        val check = CheckBook(listOf("alice"))
        val repo = BookRepository()

        val useCase = GetBook(check, repo)

        // Given
        // A user
        val user = "alice"

        // When
        // The user tries to get the book
        val book = useCase.get(user, 1)

        // Then
        // The use case should return the book with the correct data
        assertThat(book?.id).isEqualTo(1)
        assertThat(book?.title).isEqualTo("The Hobbit")
        assertThat(book?.author).isEqualTo("J.R.R. Tolkien")
        assertThat(book?.year).isEqualTo(1937)
    }

    @Test
    fun `should return book for permission`() {
        val check = CheckBook(listOf("alice"))
        val repo = BookRepository()

        val useCase = GetBook(check, repo)

        // Given
        // A user
        val user = "alice"

        // When
        // The user tries to get the book
        val book = useCase.get(user, 1)

        // Then
        // The use case should return the book
        assertThat(book).isNotNull()
    }

    @Test
    fun `should fail for missing view permission`() {
        val check = CheckBook(listOf("bob"))
        val repo = BookRepository()

        val useCase = GetBook(check, repo)

        // Given
        // A user
        val user = "alice"

        // When
        // The user tries to get the book
        val book = useCase.get(user, 1)

        // Then
        // The use case should return null
        assertThat(book).isNull()
    }
}