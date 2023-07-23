package eu.do24.core.books

import eu.do24.core.domain.Book
import eu.do24.domain.ports.authz.CanAccessBookI
import eu.do24.ports.repos.BookRepositoryI

/**
 * GetBook is a use case that gets a book from the repository.
 *
 * It uses the CanAccessBookI port to check if the user can access the book.
 * It uses the BookRepositoryI port to get the book from the repository.
 */
class GetBook(private val check: CanAccessBookI, private val repo: BookRepositoryI) {
    fun get(userId: String, bookId: Int): Book? {
        if (check.canAccessBook(userId, bookId.toString())) {
            val b = repo.getById(bookId)
            return Book(id = b!!.id, title = b.title, author = b.author, year = b.year)
        }

        return null
    }
}