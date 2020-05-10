package hu.ait.marketplace.ui.data

data class Post(
    var authorid: String = "",
    var title: String = "",
    var price: String = "",
    var location: String = "",
    var body: String = "",
    var imgUrl: String = "",
    var sold: Boolean = false
)