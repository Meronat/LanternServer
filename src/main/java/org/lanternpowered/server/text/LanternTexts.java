/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org>
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.lanternpowered.server.text;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class LanternTexts {

    @SuppressWarnings("deprecation")
    public static String toLegacy(Text text) {
        return TextSerializers.LEGACY_FORMATTING_CODE.serialize(text);
    }

    public static String toPlain(Text text) {
        return TextSerializers.PLAIN.serialize(text);
    }

    @SuppressWarnings("deprecation")
    public static Text fromLegacy(String text) {
        return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(text);
    }

    /**
     * The client doesn't like it when the server just sends a
     * primitive json string, so we put it as one entry in an array
     * to avoid errors.
     *
     * @param json the json
     * @return the result json
     */
    public static String fixJson(String json) {
        final char start = json.charAt(0);
        if (start == '[' || start == '{') {
            return json;
        } else {
            return '[' + json + ']';
        }
    }
}
