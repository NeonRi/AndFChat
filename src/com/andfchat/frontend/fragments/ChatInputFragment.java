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


package com.andfchat.frontend.fragments;

import roboguice.event.Observes;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;
import roboguice.util.Ln;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.andfchat.R;
import com.andfchat.core.connection.FlistWebSocketConnection;
import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.ChatroomManager;
import com.andfchat.core.util.Console;
import com.google.inject.Inject;

public class ChatInputFragment extends RoboFragment {

    @Inject
    protected ChatroomManager chatroomManager;
    @Inject
    protected FlistWebSocketConnection connection;
    @Inject
    protected Console commands;
    @Inject
    private InputMethodManager inputManager;

    @InjectView(R.id.chatMessage)
    private EditText inputText;
    @InjectView(R.id.sendButton)
    private Button sendButton;

    private long lastMessage = System.currentTimeMillis();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_chat_input, container, false);
        return layout;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        { // Setup send button
            sendButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessage();
                }
            });
        }
    }

    private void sendMessage() {
        Chatroom activeChat = chatroomManager.getActiveChat();
        // Ignore empty messages
        if (inputText.getText().toString().length() == 0 ) {
            return;
        }
        // Text command like /help / open /close shouldn't be send to the server, console commands too.
        if (commands.checkForCommands(inputText.getText().toString()) || activeChat.isSystemChat()) {
            // Reset input
            inputText.setText("");
            return;
        }
        else if ((System.currentTimeMillis() - lastMessage > 2000)) {


            if (activeChat.isPrivateChat()) {
                connection.sendPrivatMessage(activeChat.getRecipient().getName(), inputText.getText().toString());
            } else {
                connection.sendMessageToChannel(activeChat, inputText.getText().toString());
            }

            lastMessage = System.currentTimeMillis();
        }
        else {
            //TODO: Show error message (input to fast).
        }

        // Reset input
        inputText.setText("");
    }

    protected void setActiveChat(@Observes Chatroom chatroom) {
        Ln.v("Active chat set event is called!");

        // Set maximum text length
        inputText.setFilters(new InputFilter[] {new InputFilter.LengthFilter(chatroom.getMaxTextLength())});
    }

    public void hideKeyboard() {
        inputManager.hideSoftInputFromWindow(inputText.getWindowToken(), 0);
    }
}
