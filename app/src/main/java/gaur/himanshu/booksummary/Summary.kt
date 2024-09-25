package gaur.himanshu.booksummary

data class Summary(
    val fileName:String,
    val summary: String,
    val type:Type = Type.INTERNAL
)
