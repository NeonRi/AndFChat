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


package com.andfchat.frontend.adapter;

import java.util.List;

import roboguice.RoboGuice;
import roboguice.event.EventManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.andfchat.R;
import com.andfchat.core.connection.handler.PrivateMessageHandler;
import com.andfchat.core.connection.handler.VariableHandler.Variable;
import com.andfchat.core.data.Chatroom;
import com.andfchat.core.data.ChatroomManager;
import com.andfchat.core.data.FlistChar;
import com.andfchat.core.data.SessionData;
import com.andfchat.core.util.FlistCharComparator;
import com.andfchat.frontend.util.NameSpannable;
import com.google.inject.Inject;

public class MemberListAdapter extends ArrayAdapter<FlistChar> {

    private final static FlistCharComparator COMPARATOR = new FlistCharComparator();

    @Inject
    private ChatroomManager chatroomManager;
    @Inject
    private SessionData sessionData;
    @Inject
    protected EventManager eventManager;

    private FlistChar activeCharacter;
    private final List<FlistChar> chars;

    public MemberListAdapter(Context context, List<FlistChar> chars) {
        super(context, R.layout.list_item_user, chars);

        if (chars.size() > 1) {
            this.sort(COMPARATOR);
        }

        this.chars = chars;

        RoboGuice.getInjector(context).injectMembers(this);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final FlistChar character = this.getItem(position);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.list_item_user, null);
        // Set username
        TextView textView = (TextView)rowView.findViewById(R.id.itemText);
        textView.setText(new NameSpannable(character, null, getContext().getResources()));

        // Set icon
        ImageView itemIcon = (ImageView)rowView.findViewById(R.id.itemIcon);

        switch (character.getStatus()) {
            case online:
                itemIcon.setBackgroundResource(R.drawable.icon_blue);
                break;
            case busy:
                itemIcon.setBackgroundResource(R.drawable.icon_orange);
                break;
            case dnd:
                itemIcon.setBackgroundResource(R.drawable.icon_red);
                break;
            case looking:
                itemIcon.setBackgroundResource(R.drawable.icon_green);
                break;
            case away:
                itemIcon.setBackgroundResource(R.drawable.icon_grey);
                break;
            default:
                itemIcon.setBackgroundResource(R.drawable.icon_blue);
        }

        View userLabel = rowView.findViewById(R.id.userlabel);

        userLabel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!sessionData.getCharacterName().equals(character.getName())) {
                    activeCharacter = character;
                    notifyDataSetChanged();
                }
            }
        });

        if (activeCharacter != null && activeCharacter.getName().equals(character.getName())) {
            rowView.findViewById(R.id.pmuser).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Chatroom chatroom;
                    if (chatroomManager.hasOpenPrivateConversation(activeCharacter) == false) {
                        int maxTextLength = sessionData.getIntVariable(Variable.priv_max);
                        chatroom = PrivateMessageHandler.openPrivateChat(chatroomManager, activeCharacter, maxTextLength);
                    } else {
                        chatroom = chatroomManager.getPrivateChatFor(activeCharacter);
                    }

                    activeCharacter = null;
                    chatroomManager.setActiveChat(chatroom);
                    eventManager.fire(chatroom);
                    notifyDataSetChanged();
                }
            });

            rowView.findViewById(R.id.details).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.f-list.net/c/" + character.getName()));
                    getContext().startActivity(browserIntent);
                }
            });

            rowView.findViewById(R.id.closeUsermenu).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    activeCharacter = null;
                    notifyDataSetChanged();
                }
            });
        } else {
            rowView.findViewById(R.id.usermenu).setVisibility(View.GONE);
        }

        return rowView;
    }

    @Override
    public void add(FlistChar flistChar) {
        if (flistChar == null) {
            return;
        }

        boolean added = false;
        for (int i = 0; i < chars.size(); i++) {
            if (COMPARATOR.compare(chars.get(i), flistChar) == 0) {
                chars.add(i, flistChar);
                added = true;
                break;
            }
        }

        if (!added) {
            chars.add(flistChar);
        }

        notifyDataSetChanged();
    }

}
