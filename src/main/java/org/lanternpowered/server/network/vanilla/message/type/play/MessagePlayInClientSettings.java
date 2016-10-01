/*
 * This file is part of LanternServer, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://github.com/LanternPowered>
 * Copyright (c) SpongePowered <https://www.spongepowered.org>
 * Copyright (c) Contributors
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
package org.lanternpowered.server.network.vanilla.message.type.play;

import org.lanternpowered.server.entity.living.player.HandSide;
import org.lanternpowered.server.network.message.Message;
import org.spongepowered.api.text.chat.ChatVisibility;

import java.util.Locale;

public final class MessagePlayInClientSettings implements Message {

    private final Locale locale;
    private final ChatVisibility chatVisibility;
    private final HandSide mainHand;
    private final int viewDistance;
    private final int skinPartsBitPattern;
    private final boolean enableColors;

    public MessagePlayInClientSettings(Locale locale, int viewDistance, ChatVisibility chatVisibility,
            HandSide mainHand, boolean enableColors, int skinPartsBitPattern) {
        this.mainHand = mainHand;
        this.skinPartsBitPattern = skinPartsBitPattern;
        this.chatVisibility = chatVisibility;
        this.viewDistance = viewDistance;
        this.enableColors = enableColors;
        this.locale = locale;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getSkinPartsBitPattern() {
        return this.skinPartsBitPattern;
    }

    public ChatVisibility getChatVisibility() {
        return this.chatVisibility;
    }

    public boolean getEnableColors() {
        return this.enableColors;
    }

    public HandSide getMainHand() {
        return this.mainHand;
    }
}
