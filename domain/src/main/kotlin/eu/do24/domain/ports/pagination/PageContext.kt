package eu.do24.ports.pagination

// holds context about the current page so that we can get more
class PageContext(val cursor: String? = null, val pageSize: Int = 4)
