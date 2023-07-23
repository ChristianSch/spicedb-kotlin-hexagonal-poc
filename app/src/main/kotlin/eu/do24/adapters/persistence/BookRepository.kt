package eu.do24.adapters.persistence;

import eu.do24.domain.models.Book
import eu.do24.domain.ports.repos.BookRepositoryI


/**
 * The book entity
 */
data class BookE(val id: Int, val title: String, val author: String, val year: Int)
class BookFilter(val year: Int)

class BookRepository : BookRepositoryI {
    private val books = listOf(
        BookE(1, "Kotlin in Action", "Dmitry Jemerov", 2017),
        BookE(2, "Atomic Kotlin", "Bruce Eckel", 2019),
        BookE(3, "Kotlin for Android Developers", "Antonio Leiva", 2018),
        BookE(4, "Kotlin for Java Developers", "Svetlana Isakova", 2019),
        BookE(5, "Hexagonal Architecture with Kotlin", "Tomasz Nurkiewicz", 2018),
        BookE(6, "Kotlin Blueprints", "Ashish Belagali", 2017),
        BookE(7, "Kotlin for Enterprise Applications using Java EE", "Raghavendra Rao K", 2018),
        BookE(8, "Kotlin Programming Cookbook", "Aanand Shekhar Roy", 2018),
        BookE(9, "Kotlin Standard Library Cookbook", "Samuel Urbanowicz", 2008),
        BookE(10, "Kotlin for Android App Development", "Peter Sommerhoff", 2017),
        BookE(11, "Kotlin Programming By Example", "Iyanu Adelekan", 2018),
        BookE(12, "Kotlin for Android Development", "Peter Späth", 2017),
    )

//    fun get(ctx: PageContext): Page<Book> {
//        val cursor = ctx.cursor.toInt()
//        val index = books.indexOfFirst { it.id == cursor }
//        // no such book
//        if (index == -1) {
//            return Page(emptyList(), null, false)
//        }
//
//        val items = books.subList(index + 1, index + 1 + ctx.pageSize)
//        val nextCursor =
//            if (index + 1 + ctx.pageSize < books.size) books[index + 1 + ctx.pageSize].id.toString() else null
//        return Page(items, nextCursor, books.size > index + 1 + ctx.pageSize)
//    }
//
//    fun getBooksForUserWithFilter(filter: BookFilter, ctx: PageContext): Page<Book> {
//        if (ctx.cursor == null) {
//            val items = books.filter { it.year == filter.year }.take(ctx.pageSize)
//            return Page(items, items[0].id.toString(), items.size > ctx.pageSize)
//        }
//
//        val cursor = ctx.cursor.toInt()
//        val index = books.indexOfFirst { it.id == cursor }
//        // no such book
//        if (index == -1) {
//            return Page(emptyList(), null, false)
//        }
//
//        // fill items with books that match the filter
//        val items = books.filter { it.year == filter.year }
//
//        // get the next cursor
//        val nextCursor =
//            if (index + 1 + ctx.pageSize < items.size) items[index + 1 + ctx.pageSize].id.toString() else null
//
//        // return the page
//        return Page(items, nextCursor, items.size > index + 1 + ctx.pageSize)
//    }
//
//    fun getById(id: Int, actx: AuthContext): Book? {
//        return books.find { it.id == id }
//    }

    override fun getBatch(batchSize: Int, offset: Int): List<Book> {
        return books.subList(offset, offset + batchSize).map { Book(it.id, it.title, it.author, it.year) }
    }

    override fun getById(id: Int): Book? {
        return books.find { it.id == id }.let { Book(it!!.id, it.title, it.author, it.year) }
    }
}