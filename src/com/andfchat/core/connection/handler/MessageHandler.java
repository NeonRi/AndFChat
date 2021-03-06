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


package com.andfchat.core.connection.handler;

import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import roboguice.util.Ln;

import com.andfchat.core.connection.FeedbackListner;
import com.andfchat.core.connection.ServerToken;
import com.andfchat.core.data.CharacterManager;
import com.andfchat.core.data.ChatEntry;
import com.andfchat.core.data.ChatEntryType;
import com.andfchat.core.data.Chatroom;

/**
 * Handles incoming messages for channel and Broadcasts
 * @author AndFChat
 *
 */
public class MessageHandler extends TokenHandler {

    @Override
    public void incomingMessage(ServerToken token, String msg, List<FeedbackListner> feedbackListner) throws JSONException {
        JSONObject jsonObject = new JSONObject(msg);
        if(token == ServerToken.MSG) {
            String character = jsonObject.getString("character");
            String message = jsonObject.getString("message");
            String channel = jsonObject.getString("channel");

            Chatroom log = chatroomManager.getChatroom(channel);

            if (log != null) {
                log.addMessage(message, characterManager.findCharacter(character), new Date());
                log.setHasNewMessage(true);
            }
            else {
                Ln.e("Incoming message is for a unknown channel: " + channel);
            }
        }
        else if(token == ServerToken.BRO) {
            String message = jsonObject.getString("message");
            chatroomManager.addBroadcast(new ChatEntry(message, characterManager.findCharacter(CharacterManager.USER_SYSTEM), new Date(), ChatEntryType.NOTATION_SYSTEM));
        }
    }

    @Override
    public ServerToken[] getAcceptableTokens() {
        return new ServerToken[]{ServerToken.MSG, ServerToken.BRO};
    }

}
