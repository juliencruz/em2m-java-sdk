package io.em2m.simplex

import io.em2m.simplex.model.BasicKeyResolver
import io.em2m.simplex.model.ConstKeyHandler
import io.em2m.simplex.model.Key
import io.em2m.simplex.std.Dates
import io.em2m.simplex.std.Numbers
import io.em2m.utils.coerceNonNull
import org.junit.Assert
import org.junit.Test
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class DateTest : Assert() {

    private val keyResolver = BasicKeyResolver(mapOf(
            Key("ns", "dateKey") to ConstKeyHandler("2015-04-21T17:31:06-07"),
            Key("ns", "duration") to ConstKeyHandler(210_000)))
            .delegate(Numbers.keys)

    private val dateKeyResolver = Dates.keys

    val simplex = Simplex()
            .keys(keyResolver)
            .keys(dateKeyResolver)

    @Test
    fun testArgs() {
        val exprStr = "\${Math:PI | number:2}"
        val expr = requireNotNull(simplex.parser.parse(exprStr))
        val result = expr.call(emptyMap())
        assertEquals("3.14", result)
    }

    @Test
    fun testDateNow() {
        val exprString = "\${Date:now}"
        val expr = simplex.parser.parse(exprString)
        val resultingDate: Date = expr.call(emptyMap()).coerceNonNull()
        assertTrue(Date().time - 1000 < resultingDate.time && resultingDate.time < Date().time + 1000)
    }

    @Test
    fun testFormatDate() {
        val exprString = "\${ns:dateKey | formatDate:yyyy}"
        val expr = requireNotNull(simplex.parser.parse(exprString))
        val result = expr.call(emptyMap())
        assertEquals("2015", result)
    }

    @Test
    fun testFormatDuration() {
        val exprString = "\${ns:duration | formatDuration}"
        val expr = requireNotNull(simplex.parser.parse(exprString))
        val result = expr.call(emptyMap())
        assertEquals("3 minutes", result)
    }

    @Test
    fun testFormatDateWithColon() {
        val exprString = "\${ns:dateKey | formatDate:yyyy-MM-dd HH\\:mm:America/Los_Angeles}"
        val expr = requireNotNull(simplex.parser.parse(exprString))
        val result = expr.call(emptyMap())
        assertEquals("2015-04-21 17:31", result)
    }

    /*
    @Test
    fun testFormatDateZulu() {
        val exprString = "\${ns:dateKey | formatDate:yyyy-MM-dd HH\\:mm\\:ss.SSS'Z'}"
        val expr = requireNotNull(simplex.parser.parse(exprString))
        val result = expr.call(emptyMap())
        assertEquals("2015-04-21 17:31:06.000Z", result)
    }
     */

    @Test
    fun testFromNow() {
        val exprString = "\${ns:dateKey | fromNow}"
        val expr = requireNotNull(simplex.parser.parse(exprString))
        val result = expr.call(emptyMap())
        assertTrue(result.toString().endsWith("ago"))
    }

    @Test
    fun testDateMath() {
        val pattern: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneId.of("America/New_York"))
        val expected = "2015-05-21"
        val exprString = "\${ns:dateKey | dateMath:now+30d/d}"
        val expr = requireNotNull(simplex.parser.parse(exprString))
        val result = expr.call(emptyMap())
        val actual = SimpleDateFormat("yyyy-MM-dd").format(result)
        assertEquals(expected, actual)
    }

}