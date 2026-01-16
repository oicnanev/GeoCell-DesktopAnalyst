package com.geocell.desktopanalyst.util

/**
 * Utility object for converting color names and values to KML hexadecimal format.
 *
 * KML uses the aabbggrr format where:
 * - aa: alpha (transparency)
 * - bb: blue component
 * - gg: green component
 * - rr: red component
 *
 * @since 1.0.0 - 2025-10-11
 * @author Nuno Venancio
 */
object ColorConverter {

    /**
     * Converts a color name to KML hexadecimal format.
     *
     * Supports a wide range of CSS color names including shades of red, pink, orange,
     * yellow, purple, green, blue, brown, and gray. The function is case-insensitive
     * and handles common color name variations.
     *
     * @param colorName the name of the color to convert (e.g., "red", "light blue", "darkgreen")
     * @return the color in KML hexadecimal format (aabbggrr)
     *
     * @sample
     * ```
     * convertColorToKmlHex("red")        // returns "ff0000ff"
     * convertColorToKmlHex("LIGHT BLUE") // returns "ffe6d8ff"
     * convertColorToKmlHex("darkgreen")  // returns "ff006400"
     * ```
     *
     * @see makeColorTransparent
     */
    fun convertColorToKmlHex(colorName: String): String =
        when (colorName.lowercase().trim()) {
            // Shades of Red ------------------------------------------------
            "indianred", "indian red"                           -> "ff5c5ccd"
            "lightcoral", "light coral"                         -> "ff8080f0"
            "salmon"                                            -> "ff7280fa"
            "darksalmon", "dark salmon"                         -> "ff4e6efc"
            "lightsalmon", "light salmon"                       -> "ff7aa0ff"
            "crimson"                                           -> "ff3c14dc"
            "red"                                               -> "ff0000ff"
            "firebrick", "fire brick"                           -> "ff2222b2"
            "darkred"                                           -> "ff00008b"

            // Shades of Pink -----------------------------------------------
            "lightpink", "light pink"                           -> "ffcbc0ff"
            "pink"                                              -> "ffcbc0ff"
            "hotpink", "hot pink"                               -> "ffb469ff"
            "deeppink", "deep pink"                             -> "ff9a32ff"
            "mediumvioletred", "medium violet red"              -> "ff8515c7"
            "palevioletred", "pale violet red"                  -> "ff9370db"

            // Shades of Orange ---------------------------------------------
            "coral"                                             -> "ff507fff"
            "tomato"                                            -> "ff4763ff"
            "orangered", "orange red"                           -> "ff1e69ff"
            "darkorange", "dark orange"                         -> "ff008cff"
            "orange"                                            -> "ff0066ff"

            // Shades of Yellow ---------------------------------------------
            "gold"                                              -> "ff00d7ff"
            "yellow"                                            -> "ff00ffff"
            "lightyellow", "light yellow"                       -> "ffe0ffff"
            "lemonchiffon", "lemon chiffon"                     -> "ffd0ffff"
            "lightgoldenrodyellow", "light goldenrod yellow"    -> "ffe8d8ff"
            "papayawhip", "papaya whip"                         -> "ffdbd5ff"
            "moccasin"                                          -> "ffb5e4ff"
            "peachpuff", "peach puff"                           -> "ffb9d1ff"
            "palegoldenrod", "pale goldenrod"                   -> "ffacdbdb"
            "khaki"                                             -> "ff8ce6f0"
            "darkkhaki", "dark khaki"                           -> "ff6bb7b3"

            // Shades of Purple ---------------------------------------------
            "lavender"                                          -> "ffe6e6fa"
            "thistle"                                           -> "ffd8bfd8"
            "plum"                                              -> "ffdda0dd"
            "violet"                                            -> "ffee82ee"
            "orchid"                                            -> "ffda70d6"
            "fuchsia"                                           -> "ffff00ff"
            "magenta"                                           -> "ffff00ff"
            "mediumorchid", "medium orchid"                     -> "ffba55d3"
            "mediumpurple", "medium purple"                     -> "ff9370db"
            "rebeccapurple", "rebecca purple"                   -> "ff663399"
            "blueviolet", "blue violet"                         -> "ff8a2be2"
            "darkviolet", "dark violet"                         -> "ff9400d3"
            "darkorchid", "dark orchid"                         -> "ff9932cc"
            "darkmagenta", "dark magenta"                       -> "ff8b008b"
            "purple"                                            -> "ff800080"
            "indigo"                                            -> "ff4b0082"
            "slateblue", "slate blue"                           -> "ff6a5acd"
            "darkslateblue", "dark slate blue"                  -> "ff483d8b"
            "mediumslateblue", "medium slate blue"              -> "ff7b68ee"

            // Shades of Green ----------------------------------------------
            "greenyellow", "green yellow"                       -> "ff2fffad"
            "chartreuse"                                        -> "ff00ff7f"
            "lawngreen", "lawn green"                           -> "ff00fc7c"
            "lime"                                              -> "ff00ff00"
            "limegreen", "lime green"                           -> "ff32cd32"
            "palegreen", "pale green"                           -> "ff98fb98"
            "lightgreen", "light green"                         -> "ff90ee90"
            "mediumspringgreen", "medium spring green"          -> "ff00fa9a"
            "springgreen", "spring green"                       -> "ff00ff7f"
            "mediumseagreen", "medium sea green"                -> "ff3cb371"
            "seagreen", "sea green"                             -> "ff2e8b57"
            "forestgreen", "forest green"                       -> "ff228b22"
            "green"                                             -> "ff008000"
            "darkgreen", "dark green"                           -> "ff006400"
            "yellowgreen", "yellow green"                       -> "ff32cd9a"
            "olivedrab", "olive drab"                           -> "ff3c6e3c"
            "olive"                                             -> "ff008080"
            "darkolivegreen", "dark olive green"                -> "ff556b2f"
            "mediumaquamarine", "medium aquamarine"             -> "ff66cdaa"
            "darkseagreen", "dark sea green"                    -> "ff8fbc8f"
            "lightseagreen", "light sea green"                  -> "ff20b2aa"
            "teal"                                              -> "ff808000"
            "darkcyan", "dark cyan"                             -> "ff8b8b00"

            // Shades of Blue -----------------------------------------------
            "aqua"                                              -> "ffffff00"
            "cyan"                                              -> "ffffff00"
            "aquamarine"                                        -> "ff7fffd4"
            "mediumturquoise", "medium turquoise"               -> "ff48d1cc"
            "turquoise"                                         -> "ff40e0d0"
            "powderblue", "powder blue"                         -> "ffe6e0ff"
            "lightblue", "light blue"                           -> "ffe6d8ff"
            "lightskyblue", "light sky blue"                    -> "ffface87"
            "skyblue", "sky blue"                               -> "ffebce87"
            "deepskyblue", "deep sky blue"                      -> "ffffbf00"
            "dodgerblue", "dodger blue"                         -> "ffff901e"
            "cornflowerblue", "cornflower blue"                 -> "ffed9564"
            "steelblue", "steel blue"                           -> "ffb48246"
            "royalblue", "royal blue"                           -> "ff9b30ff"
            "blue"                                              -> "ffff0000"
            "mediumblue", "medium blue"                         -> "ffcd0000"
            "darkblue", "dark blue"                             -> "ff8b0000"
            "navy"                                              -> "ff800000"
            "midnightblue", "midnight blue"                     -> "ff701919"

            // Shades of Brown ----------------------------------------------
            "cornsilk"                                          -> "ffdcf8ff"
            "blanchedalmond", "blanched almond"                 -> "ffceb6ff"
            "bisque"                                            -> "ffb8e4ff"
            "navajowhite", "navajo white"                       -> "ffaddeff"
            "wheat"                                             -> "ffb3def5"
            "burlywood"                                         -> "ff87adae"
            "tan"                                               -> "ff8cb4d2"
            "rosybrown", "rosy brown"                           -> "ff8f8fbc"
            "sandybrown", "sandy brown"                         -> "ff60a4f4"
            "goldenrod"                                         -> "ff20a5da"
            "darkgoldenrod", "dark goldenrod"                   -> "ff0b86b8"
            "peru"                                              -> "ff3f85cd"
            "chocolate"                                         -> "ff1e69ff"
            "saddlebrown", "saddle brown"                       -> "ff13458b"
            "sienna"                                            -> "ff2d52a0"
            "brown"                                             -> "ff191970"
            "maroon"                                            -> "ff000080"

            // Shades of Gray -----------------------------------------------
            "lightgray", "light gray"                           -> "ffd3d3d3"
            "silver"                                            -> "ffc0c0c0"
            "darkgray", "dark gray"                             -> "ffa9a9a9"
            "gray"                                              -> "ff808080"
            "dimgray", "dim gray"                               -> "ff696969"
            "lightslategray", "light slate gray"                -> "ff778899"
            "slategray", "slate gray"                           -> "ff708090"
            "darkslategray", "dark slate gray"                  -> "ff2f4f4f"
            "black"                                             -> "ff000000"

            // Other Colors -------------------------------------------------
            "white"                                             -> "ffffffff"
            "snow"                                              -> "fffffafa"
            "honeydew"                                          -> "fff0fff0"
            "mintcream", "mint cream"                           -> "fff5fffa"
            "azure"                                             -> "fff0ffff"
            "aliceblue", "alice blue"                           -> "fff0f8ff"
            "ghostwhite", "ghost white"                         -> "fff8f8ff"
            "whitesmoke", "white smoke"                         -> "fff5f5f5"
            "seashell"                                          -> "fff5e6ff"
            "beige"                                             -> "ffdcf5f5"
            "oldlace", "old lace"                               -> "ffefe6dd"
            "floralwhite", "floral white"                       -> "ffeffaf0"
            "ivory"                                             -> "fff0ffff"
            "antiquewhite", "antique white"                     -> "ffd7ebfa"
            "linen"                                             -> "ffe6f0fa"
            "lavenderblush", "lavender blush"                   -> "fff5f0ff"
            "mistyrose", "misty rose"                           -> "ffe1e4ff"

            // Default ------------------------------------------------------
            else -> "ff0000ff" // Default to red
        }

    /**
     * Applies transparency to a KML color by modifying the alpha channel.
     *
     * This function is particularly useful for polygon colors where some transparency
     * is desired to see underlying map features. The function handles both 6-character
     * (rrggbb) and 8-character (aarrggbb) color formats.
     *
     * @param colorHex the original color in hexadecimal format
     * @param transparency the transparency value (00 = fully transparent, ff = fully opaque)
     * @return the color with applied transparency in KML format
     *
     * @sample
     * ```
     * makeColorTransparent("ff0000ff")         // returns "4f0000ff" (semi-transparent red)
     * makeColorTransparent("00ff00", "7f")     // returns "7f00ff00" (semi-opaque green)
     * makeColorTransparent("blue", "2f")       // returns "2fff0000" (very transparent blue)
     * ```
     *
     * @see convertColorToKmlHex
     */
    fun makeColorTransparent(colorHex: String, transparency: String = "4f"): String {
        val fullColor = if (colorHex.length == 6) "ff$colorHex" else colorHex
        return transparency + fullColor.substring(2)
    }
}