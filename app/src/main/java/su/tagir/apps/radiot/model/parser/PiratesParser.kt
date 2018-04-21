package su.tagir.apps.radiot.model.parser

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import su.tagir.apps.radiot.model.entries.RTEntry
import su.tagir.apps.radiot.utils.parseDate
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.regex.Pattern


object PiratesParser {

    private val IMG_PATTERN = Pattern.compile("<img src=\"([^\"]+)")

    private val PODCAST = "item"
    private val TITLE = "title"
    private val DATE = "pubDate"
    private val IMAGE = "description"
    private val ENCLOSURE = "enclosure"
    private val AUTHOR = "author"
    private val SUMMARY = "summary"

    private val NAMESPACE_ITUNES = "http://www.itunes.com/dtds/podcast-1.0.dtd"
    private val ITUNES = "itunes"

    private val ATTR_URL = "url"
    private val ATTR_LENGTH = "length"

    @Throws(IOException::class, XmlPullParserException::class)
    fun parsePirates(inputStream: InputStream): List<RTEntry> {
        inputStream.use { stream ->
            val podcasts = ArrayList<RTEntry>()
            val xpp = Xml.newPullParser()
            xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true)
            xpp.setInput(stream, null)
            while (xpp.next() != XmlPullParser.END_DOCUMENT) {
                if (xpp.eventType != XmlPullParser.START_TAG) {
                    continue
                }
                if (xpp.name == PODCAST) {
                    val podcast = getPodcast(xpp)
                    if (podcast.url.isNotBlank()) {
                        podcasts.add(podcast)
                    }
                }
            }
            return podcasts
        }
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun getPodcast(xpp: XmlPullParser): RTEntry {
        var title = ""
        val image = "http://pirates.radio-t.com/images/pirates-logo-big.jpg"
        var url = ""
        var date = Date()
        xpp.require(XmlPullParser.START_TAG, XmlPullParser.NO_NAMESPACE, PODCAST)
        while (!(xpp.next() == XmlPullParser.END_TAG && xpp.name == PODCAST)) {
            if (xpp.eventType != XmlPullParser.START_TAG) {
                continue
            }
            val name = xpp.name
            when (name) {
                TITLE -> title = readTag(xpp, TITLE)
                DATE -> {
                    val dateStr = readTag(xpp, DATE)
                    date = dateStr.parseDate("EEE, dd MMM yyyy HH:mm:ss z")
                }
                ENCLOSURE -> url = getLink(xpp)

                else -> skip(xpp)
            }
        }
        return RTEntry(url = url, image = image, title = title, audioUrl = url, categories = listOf("pirates"), date = date)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(xpp: XmlPullParser, tag: String): String {
        return readTag(xpp, XmlPullParser.NO_NAMESPACE, tag)
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readTag(xpp: XmlPullParser, nameSpace: String, tag: String): String {
        xpp.require(XmlPullParser.START_TAG, nameSpace, tag)
        val result = readText(xpp)
        xpp.require(XmlPullParser.END_TAG, nameSpace, tag)
        return result
    }


    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(xpp: XmlPullParser): String {
        var result = ""
        if (xpp.next() == XmlPullParser.TEXT) {
            result = xpp.text
            xpp.nextTag()
        }
        return result
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun getLink(xpp: XmlPullParser): String {
        var url = ""
        xpp.require(XmlPullParser.START_TAG, XmlPullParser.NO_NAMESPACE, ENCLOSURE)
        val tag = xpp.name
        if (tag == ENCLOSURE) {
            url = xpp.getAttributeValue(XmlPullParser.NO_NAMESPACE, ATTR_URL)
            xpp.nextTag()
        }
        xpp.require(XmlPullParser.END_TAG, XmlPullParser.NO_NAMESPACE, ENCLOSURE)
        return url
    }

    private fun getImageUrlFromDescription(description: String): String {
        val matcher = IMG_PATTERN.matcher(description)
        return if (matcher.find()) matcher.group(1) else ""
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun skip(parser: XmlPullParser) {
        var depth = 1
        while (depth != 0) {
            val next = parser.next()
            if (next == XmlPullParser.END_TAG)
                depth--
            else if (next == XmlPullParser.START_TAG) depth++
        }
    }


}