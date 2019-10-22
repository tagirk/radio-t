package su.tagir.apps.radiot.image

sealed class Transformation {

    object Circle: Transformation()

    data class RoundedCorner(val radius: Int): Transformation()
}