package su.tagir.apps.radiot.model.db


//object StringListConverter {

//    @TypeConverter
//    @JvmStatic
//    fun fromString(string: String?): List<String>? {
//        return if (string == null) null else TextUtils.split(string, ",").asList()
//    }
//
//    @TypeConverter
//    @JvmStatic
//    fun toString(strings: List<String>?): String? {
//        return if (strings == null) null else TextUtils.join(",", strings)
//    }
//}
//
//object DateConverter {
//    @TypeConverter
//    @JvmStatic
//    fun fromMillis(millis: Long?): Date? {
//        return if (millis == null) null else Date(millis)
//    }
//
//    @TypeConverter
//    @JvmStatic
//    fun toMillis(time: Date?): Long? {
//        return time?.time
//    }
//}
//
//object UrlListConverter {
//
//    @TypeConverter
//    @JvmStatic
//    fun fromString(string: String?): List<Url>? {
//        if (string == null) {
//            return null
//        }
//        return TextUtils.split(string, ",").map { Url(it) }
//    }
//
//    @TypeConverter
//    @JvmStatic
//    fun toString(urls: List<Url>?): String? {
//        if (urls == null) {
//            return null
//        }
//        return TextUtils.join(",", urls.map { it.url })
//    }
//}
//
//object MentionsListConverter {
//
//    @TypeConverter
//    @JvmStatic
//    fun fromJson(json: String?): List<Mention>? {
//        if (json == null) {
//            return null
//        }
//        val jsonObject = JSONObject(json)
//        val array = JSONArray(jsonObject.getString("list"))
//        val list = ArrayList<Mention>(array.length())
//        for (i in 0 until array.length()) {
//            val obj = array[i] as JSONObject
//            val ids = if (obj.has("ids")) obj.getString("ids")?.split(",") else null
//            val screenName = if (obj.has("screenName")) obj.getString("screenName") else null
//            val userId = if (obj.has("userId")) obj.getString("userId") else null
//            val mention = Mention(screenName = screenName, userId = userId, userIds = ids)
//            list.add(mention)
//        }
//        return list
//    }
//
//    @TypeConverter
//    @JvmStatic
//    fun toJson(mentions: List<Mention>?): String? {
//        if (mentions == null) {
//            return null
//        }
//        val objects = mentions.map {
//            val jsonObject = JSONObject()
//            jsonObject.put("screenName", it.screenName)
//            jsonObject.put("userId", it.userId)
//            if (it.userIds != null) {
//                val ids = TextUtils.join(",", it.userIds)
//                jsonObject.put("ids", ids)
//            }
//            jsonObject
//        }
//        val json = JSONObject()
//        json.put("list", objects)
//        return json.toString()
//    }
//}
//
//object EditConverter {
//
//    @TypeConverter
//    @JvmStatic
//    fun fromJson(json: String?): Edit? {
//        if (json == null) {
//            return null
//        }
//        val jsonObject = JSONObject(json)
//        val time = jsonObject.getLong("time")
//        val summary = jsonObject.getString("summary")
//
//        return Edit(Date(time), summary)
//    }
//
//    @TypeConverter
//    @JvmStatic
//    fun toJson(edit: Edit?): String? {
//        if (edit == null) {
//            return null
//        }
//        val jsonObject = JSONObject()
//        jsonObject.put("time", edit.time.time)
//        jsonObject.put("summary", edit.summary)
//
//        return jsonObject.toString()
//    }
//}
//
//object VotesConverter {
//
//    @TypeConverter
//    @JvmStatic
//    fun fromJson(json: String?): Map<String, Boolean> {
//        if (json == null) {
//            return emptyMap()
//        }
//
//        val jsonObject = JSONObject(json)
//        val map = HashMap<String, Boolean>()
//        jsonObject
//                .keys()
//                .forEach {
//                    map[it] = jsonObject.getBoolean(it)
//                }
//        return map
//    }
//
//    @TypeConverter
//    @JvmStatic
//    fun toJson(votes: Map<String, Boolean>?): String? {
//        if (votes == null || votes.isEmpty()) {
//            return null
//        }
//        val jsonObject = JSONObject()
//        votes.entries.forEach {
//            jsonObject.put(it.key, it.value)
//        }
//        return jsonObject.toString()
//    }
//}