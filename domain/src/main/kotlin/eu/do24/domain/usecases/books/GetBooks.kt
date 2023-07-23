package eu.do24.domain.usecases.books

import eu.do24.domain.models.Book
import eu.do24.domain.ports.authz.BookPermissionCheck
import eu.do24.domain.ports.authz.CanAccessBookI
import eu.do24.domain.ports.pagination.Page
import eu.do24.ports.pagination.PageContext
import eu.do24.domain.ports.repos.BookRepositoryI

class GetBooks(private val checkedBooksI: CanAccessBookI, private val repo: BookRepositoryI) {
    /**
     * GetBooks is a use case that gets a list of books from the repository.
     *
     * It uses the CanAccessBooksI port to check if the user can access the books.
     * It uses the BookRepositoryI port to get the books from the repository.
     */
    fun getBooks(userId: String, ctx: PageContext): Page<Book> {
        // no prior pagination
        if (ctx.cursor == null) {
            // fetch books from the beginning and convert to domain objects
            val books = repo.getBatch(ctx.pageSize, 0)
                .map {
                    Book(
                        id = it.id,
                        title = it.title,
                        author = it.author,
                        year = it.year
                    )
                }

            // return the page
            return Page(
                items = books.take(ctx.pageSize),
                cursor = books[0].id.toString(),
                hasMore = books.size > ctx.pageSize
            )
        }

        // get the cursor
        val cursor = ctx.cursor.toInt()
        val offset = ctx.pageSize * (cursor - 1)

        // check if the user can access the books
        // FIXME:
//        if (checkedBooksI.getAccessibleBooks(userId, cursor, offset)) {
//            val b = repo.getBatch(cursor, offset)
//            return b
//        }

        return getBooksWithPermission(userId, ctx)
    }

    private fun getBooksWithPermission(userId: String, ctx: PageContext): Page<Book> {
        val books: MutableList<Book> = mutableListOf()

        // we need to fetch as many books from the repo until we have enough books the user is allowed to see
        while (books.size < ctx.pageSize) {
            // get the next batch of books
            val b = repo.getBatch(ctx.pageSize, books.size)
                .filter { checkedBooksI.canAccessBook(BookPermissionCheck(userId, it.id.toString())) }

            if (b.isEmpty()) {
                // no more books
                break
            }

            // add the books to the list
            books.addAll(b.map { Book(it.id, it.title, it.author, it.year) })

            // if we have enough books, we can stop
            if (books.size >= ctx.pageSize) {
                break
            }
        }

        // return the page
        return Page(
            items = books.take(ctx.pageSize),
            cursor = books[0].id.toString(),
            hasMore = books.size > ctx.pageSize
        )
    }

}