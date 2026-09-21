package com.example.ui

import com.example.data.model.Product
import org.junit.Assert.*
import org.junit.Test

class DiscoverSearchTest {

    private val products = listOf(
        Product(id = "1", name = "Cotton Shirt", brand = "ZARA", price = 2999.0, description = "Lightweight summer shirt", productImages = emptyList()),
        Product(id = "2", name = "Linen Trousers", brand = "H&M", price = 3999.0, description = "Breathable fabric for hot days", productImages = emptyList()),
        Product(id = "3", name = "Denim Jacket", brand = "Levis", price = 5999.0, description = "Classic blue cotton denim", productImages = emptyList())
    )

    @Test
    fun `test search by name`() {
        val results = filterProducts(products, "Shirt")
        assertEquals(1, results.size)
        assertEquals("1", results[0].id)
    }

    @Test
    fun `test search by merchant`() {
        val results = filterProducts(products, "H&M")
        assertEquals(1, results.size)
        assertEquals("2", results[0].id)
    }

    @Test
    fun `test search by description`() {
        // "cotton" is in description of Product 3 and name of Product 1
        val results = filterProducts(products, "cotton")
        assertEquals(2, results.size)
        assertTrue(results.any { it.id == "1" })
        assertTrue(results.any { it.id == "3" })
        
        // "summer" is only in description of Product 1
        val results2 = filterProducts(products, "summer")
        assertEquals(1, results2.size)
        assertEquals("1", results2[0].id)
    }

    @Test
    fun `test search case insensitive`() {
        val results = filterProducts(products, "COTTON")
        assertEquals(2, results.size)
    }

    @Test
    fun `test search with empty query returns all`() {
        val results = filterProducts(products, "")
        assertEquals(3, results.size)
    }

    @Test
    fun `test search with no match returns empty`() {
        val results = filterProducts(products, "shoes")
        assertTrue(results.isEmpty())
    }
}
