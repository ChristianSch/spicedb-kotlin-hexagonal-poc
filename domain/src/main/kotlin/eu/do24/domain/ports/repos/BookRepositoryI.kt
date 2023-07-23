package eu.do24.domain.ports.repos

import eu.do24.domain.models.Book


interface BookRepositoryI {
    fun getBatch(batchSize: Int, offset: Int): List<Book>
    fun getById(id: Int): Book?
}