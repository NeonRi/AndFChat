/*******************************************************************************
 *     This file is part of AndFChat.
 *
 *     AndFChat is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     AndFChat is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with AndFChat.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/


package com.andfchat.core.util.commands;

import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.Chatroom.ChatroomType;



public class CloseChannelToPublic extends TextCommand {

    public CloseChannelToPublic() {
        allowedIn = new ChatroomType[]{ChatroomType.PRIVATE_CHANNEL, ChatroomType.PUBLIC_CHANNEL};
    }

    @Override
    public String getDescription() {
        return "*  /closeroom | THIS WILL CLOSE AN OPENEND PRIVATE ROOM, REMOVING IT FROM THE LIST, AND MAKING FURTHER ENTRY INVIT-ONLY.";
    }

    @Override
    public boolean fitToCommand(String token) {
        return token.equals("/closeroom");
    }

    @Override
    public void runCommand(String token, String text) {
        Chatroom chatroom = chatroomManager.getActiveChat();
        if (chatroom != null) {
            connection.closeChannel(chatroom);
        }
    }
}
