package com.decibel.civilianc2.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.decibel.civilianc2.R;
import com.decibel.civilianc2.adaptors.MessageViewAdapter;
import com.decibel.civilianc2.model.dataaccess.UserSettings;
import com.decibel.civilianc2.model.entities.MessageRecord;
import com.decibel.civilianc2.model.managers.MessageManager;
import com.decibel.civilianc2.model.managers.Model;
import com.decibel.civilianc2.protocols.ax25.AX25Packet;
import com.decibel.civilianc2.protocols.ax25.aprs.TextMessage;

import java.util.List;

public class SendMessage extends Activity implements MessageManager.IEventListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_message);
        pastMessages = findViewById(R.id.messagesList);

        recipientCallSign = getIntent().getStringExtra(RecipientCallSignKey);
        setTitle("Civilan C2 Messaging - " + recipientCallSign);
        final String callSign = Model.getInstance().getUserSettings().getSetting(UserSettings.CallSign);
        if(callSign == null || callSign.isEmpty()){
            Toast.makeText(this, "You need to set your call sign to view messages sent to you.", Toast.LENGTH_LONG).show();
            finish();
        }

        List<MessageRecord> messages = Model.getInstance().getMessageManager().getMessageRecords(recipientCallSign, "APDR14");
        adapter = new MessageViewAdapter(this, messages);
        pastMessages.setAdapter(adapter);
        Model.getInstance().getMessageManager().addEventListener(this);

        editMessage = findViewById(R.id.editMessage);
        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSendMessage();
            }


        });
    }

    public static final String RecipientCallSignKey = "RecipientCallSign";

    @Override
    public void onMessageReceived(final MessageRecord messageRecord) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (messageRecord.getMessage().getSource().equals(recipientCallSign)) {
                        adapter.add(messageRecord);
                    }
                }catch (Exception e){
                    String message = e.getMessage();
                }
            }
        });
    }

    private void onSendMessage() {
        String message = editMessage.getText().toString();
        if(message != null && !message.isEmpty()) {
            TextMessage ax25Text = new TextMessage();
            ax25Text.Message = message;
            int lastId = Model.getInstance().getUserSettings().getInt("LastMessageId", 0);
            String messageId = String.format("%03d", lastId);
            ax25Text.MessageId = messageId;
            AX25Packet packet = new AX25Packet();
            packet.Header.SourceAddress = Model.getInstance().getUserSettings().getSetting(UserSettings.CallSign);
            packet.Header.DestinationAddress = recipientCallSign;
            packet.Header.DigipeaterAddresses.add("WIDE1-1");
            packet.Payload = ax25Text;
            Model.getInstance().getModulator().sendMessage(packet);
            Model.getInstance().getUserSettings().setSetting("LastMessageId", lastId++);
        } else {
            Toast.makeText(this, "You need to enter a message before hitting send.", Toast.LENGTH_LONG).show();
        }
    }

    MessageViewAdapter adapter;
    String recipientCallSign;
    Handler handler = new Handler();
    ListView pastMessages;
    EditText editMessage;
}
