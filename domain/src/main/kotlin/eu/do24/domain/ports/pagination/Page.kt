package eu.do24.ports.pagination

// implements cursor-based pagination holding an entity of type T
data class Page<T>(
    val items: List<T>,
    val cursor: String?,
    val hasMore: Boolean
)