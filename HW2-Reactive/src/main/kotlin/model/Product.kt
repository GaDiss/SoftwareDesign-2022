package model

import org.bson.Document

class Product(
    private val name: String,
    private val usdPrice: Double
) {
    constructor(doc: Document) : this(
        doc.getString(NAME),
        doc.getDouble(PRICE)
    )

    fun toDocument(): Document {
        return Document(NAME, name)
            .append(PRICE, usdPrice)
    }

    fun toString(currency: Currency): String {
        return "$name: ${price(currency)}"
    }

    private fun price(currency: Currency): Double =
        usdPrice * when (currency) {
            Currency.USD -> 1.0
            Currency.EUR -> 0.9
            Currency.RUB -> 1234.5
        }

    companion object {
        const val NAME = "name"
        const val PRICE = "price"
    }
}