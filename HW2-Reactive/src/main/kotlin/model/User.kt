package model

import org.bson.Document

class User(
    private val id: Int,
    val currency: Currency
) {
    constructor(doc: Document) : this(
        doc.getInteger(ID),
        Currency.valueOf(doc.getString(CURRENCY))
    )

    fun toDocument(): Document {
        return Document(ID, id)
            .append(CURRENCY, currency.currencyCode)
    }

    override fun toString(): String {
        return "$id: $currency"
    }

    companion object {
        const val ID = "_id"
        const val CURRENCY = "currency"
    }
}