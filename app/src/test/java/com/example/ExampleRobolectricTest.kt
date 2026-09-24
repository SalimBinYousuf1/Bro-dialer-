package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.ContactItem
import com.example.data.model.ContactPhoneNumber
import com.example.domain.usecase.PhoneNumberHelper
import com.example.domain.usecase.T9SearchEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun testAppNameIsSalim() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Salim", appName)
    }

    @Test
    fun testPhoneNumberHelperNormalization() {
        assertEquals("+15551234567", PhoneNumberHelper.normalizeNumber("+1 (555) 123-4567"))
        assertEquals("0812345678", PhoneNumberHelper.normalizeNumber("0812-345-678"))
        assertEquals("911", PhoneNumberHelper.normalizeNumber("911"))
    }

    @Test
    fun testEmergencyNumbers() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(PhoneNumberHelper.isEmergencyNumber(context, "911"))
        assertTrue(PhoneNumberHelper.isEmergencyNumber(context, "112"))
        assertFalse(PhoneNumberHelper.isEmergencyNumber(context, "5551234567"))
    }

    @Test
    fun testT9SearchEngine() {
        val contacts = listOf(
            ContactItem(
                id = 1L,
                lookupKey = "1",
                name = "Alice Smith",
                numbers = listOf(ContactPhoneNumber(number = "555-1234", normalizedNumber = "5551234", type = "Mobile", isPrimary = true))
            ),
            ContactItem(
                id = 2L,
                lookupKey = "2",
                name = "Bob Jones",
                numbers = listOf(ContactPhoneNumber(number = "555-9876", normalizedNumber = "5559876", type = "Mobile", isPrimary = true))
            )
        )

        // '2' matches 'A' in "Alice" or 'B' in "Bob"
        val resultsA = T9SearchEngine.search(contacts, "25423") // 2(A) 5(L) 4(I) 2(C) 3(E)
        assertEquals(1, resultsA.size)
        assertEquals("Alice Smith", resultsA.first().contact.name)

        // T9 match for "Bob": 2(B) 6(O) 2(B)
        val resultsBob = T9SearchEngine.search(contacts, "262")
        assertEquals(1, resultsBob.size)
        assertEquals("Bob Jones", resultsBob.first().contact.name)

        // Number search: "9876"
        val resultsNum = T9SearchEngine.search(contacts, "9876")
        assertEquals(1, resultsNum.size)
        assertEquals("Bob Jones", resultsNum.first().contact.name)
    }
}
