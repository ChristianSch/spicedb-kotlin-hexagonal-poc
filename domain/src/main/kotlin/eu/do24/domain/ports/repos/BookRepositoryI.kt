package eu.do24.domain.ports.repos

data class Book(val id: Int, val title: String, val author: String, val year: Int)

interface BookRepositoryI {
    fun getBatch(batchSize: Int, offset: Int): List<Book>
    fun getById(id: Int): Book?
}