package com.pinneapple.dojocam_app;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.cometchat.pro.constants.CometChatConstants;
import com.cometchat.pro.core.CometChat;
import com.cometchat.pro.core.MessagesRequest;
import com.cometchat.pro.exceptions.CometChatException;
import com.cometchat.pro.models.BaseMessage;
import com.cometchat.pro.models.CustomMessage;
import com.cometchat.pro.models.MediaMessage;
import com.cometchat.pro.models.TextMessage;
import com.squareup.picasso.Picasso;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;
import com.stfalcon.chatkit.messages.MessageInput;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

public class ChatActivity extends AppCompatActivity {

    public static String groupIDint;
    private MessagesListAdapter<IMessage> adapter;

    public static void start(Context context, String groupID) {
        Intent starter = new Intent(context, ChatActivity.class);
        groupIDint = groupID;
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();

        initViews();
        addListener();
        fetchPreviousMessages();
    }

    private void fetchPreviousMessages() {
        //String groupIDint = "supergroup";
        MessagesRequest messagesRequest = new MessagesRequest.MessagesRequestBuilder().setGUID(groupIDint).build();
        messagesRequest.fetchPrevious(new CometChat.CallbackListener<List<BaseMessage>>() {
            @Override
            public void onSuccess(List<BaseMessage> baseMessages) {
                addMessages(baseMessages);
            }

            @Override
            public void onError(CometChatException e) {

            }
        });
    }

    private void addMessages(List<BaseMessage> baseMessages) {
        List<IMessage> list = new ArrayList<>();
        for (BaseMessage message : baseMessages) {
            if (message instanceof TextMessage) {
                list.add(new MessageWrapper((TextMessage) message));
            }
        }
        adapter.addToEnd(list, true);
    }

    private void addListener() {
        String listenerID  = "listener 1";
        CometChat.addMessageListener(listenerID, new CometChat.MessageListener() {
            @Override
            public void onTextMessageReceived(TextMessage textMessage) {
                fetchPreviousMessages();
                adapter.addToStart(new MessageWrapper(textMessage), true);
            }
            @Override
            public void onMediaMessageReceived(MediaMessage mediaMessage) {
            }
            @Override
            public void onCustomMessageReceived(CustomMessage customMessage) {
            }
        });
    }

    private void initViews() {
        MessageInput inputView = findViewById(R.id.input);
        MessagesList messagesList = findViewById(R.id.messagesList);
        inputView.setInputListener(new MessageInput.InputListener() {
           @Override
           public boolean onSubmit(CharSequence input){
               sendMessage(input.toString());
               return true;
           }
        });

        String senderId = CometChat.getLoggedInUser().getUid();
        ImageLoader imageLoader = new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
                Picasso.get().load(url).into(imageView);
            }
        };
        adapter = new MessagesListAdapter<>(senderId, imageLoader);
        messagesList.setAdapter(adapter);
    }

    private void sendMessage(String message) {
        TextMessage textMessage = new TextMessage(groupIDint, message, CometChatConstants.RECEIVER_TYPE_GROUP);
        CometChat.sendMessage(textMessage, new CometChat.CallbackListener<TextMessage>() {
            @Override
            public void onSuccess(TextMessage textMessage) {
                Log.d("suce", "SIRVEEEE");
                addMessage(textMessage);
            }
            @Override
            public void onError(CometChatException e) {
                Log.d("suce", "No SIRVEEEE");
            }
        });
    }

    private void addMessage(TextMessage textMessage) {
        adapter.addToStart(new MessageWrapper(textMessage), true);
        //adapter.notifyItemInserted(0);
    }

    private void mensaje_recibdo_persona(String listener_id){
        int limit = 30;
        int latestId = CometChat.getLastDeliveredMessageId();
        String UID = "superhero1";

        MessagesRequest messagesRequest= new MessagesRequest.MessagesRequestBuilder()
                .setMessageId(latestId)
                .setLimit(limit)
                .setUID(UID)
                .build();

        messagesRequest.fetchNext(new CometChat.CallbackListener<List<BaseMessage>>() {
            @Override
            public void onSuccess(List <BaseMessage> list) {
                for (BaseMessage message: list) {
                    if (message instanceof TextMessage) {
                        Log.d("TAG", "Text message received successfully: " +
                                ((TextMessage) message).toString());
                    } else if (message instanceof MediaMessage) {
                        Log.d("TAG", "Media message received successfully: " +
                                ((MediaMessage) message).toString());
                    }
                }
            }
            @Override
            public void onError(CometChatException e) {
                Log.d("TAG", "Message fetching failed with exception: " + e.getMessage());
            }
        });
    }

    public void ultimo_mensaje_grupo (String GUID){
        int limit = 30;
        int latestId = CometChat.getLastDeliveredMessageId();
        //private String GUID = "supergroup";

        MessagesRequest messagesRequest= new MessagesRequest.MessagesRequestBuilder()
                .setMessageId(latestId)
                .setLimit(limit)
                .setGUID(GUID)
                .build();

        messagesRequest.fetchNext(new CometChat.CallbackListener<List<BaseMessage>>() {
            @Override
            public void onSuccess(List <BaseMessage> list) {
                for (BaseMessage message: list) {
                    if (message instanceof TextMessage) {
                        Log.d("TAG", "Text message received successfully: " +
                                ((TextMessage) message).toString());
                    } else if (message instanceof MediaMessage) {
                        Log.d("TAG", "Media message received successfully: " +
                                ((MediaMessage) message).toString());
                    }
                }
            }

            @Override
            public void onError(CometChatException e) {
                Log.d("TAG", "Message fetching failed with exception: " + e.getMessage());
            }
        });
    }
}