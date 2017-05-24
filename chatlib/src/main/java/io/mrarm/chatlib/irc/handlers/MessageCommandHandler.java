package io.mrarm.chatlib.irc.handlers;

import io.mrarm.chatlib.NoSuchChannelException;
import io.mrarm.chatlib.dto.MessageInfo;
import io.mrarm.chatlib.dto.MessageSenderInfo;
import io.mrarm.chatlib.dto.NickPrefixList;
import io.mrarm.chatlib.dto.StatusMessageInfo;
import io.mrarm.chatlib.irc.*;
import io.mrarm.chatlib.user.UserInfo;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MessageCommandHandler implements CommandHandler {

    @Override
    public String[] getHandledCommands() {
        return new String[] { "PRIVMSG", "NOTICE" };
    }

    @Override
    public void handle(ServerConnectionData connection, MessagePrefix sender, String command, List<String> params)
            throws InvalidMessageException {
        try {
            MessageInfo.MessageType type = (command.equals("NOTICE") ? MessageInfo.MessageType.NOTICE :
                    MessageInfo.MessageType.NORMAL);

            UUID userUUID = connection.getUserInfoApi().resolveUser(sender.getNick(), sender.getUser(),
                    sender.getHost(), null, null).get();
            for (String channel : params.get(0).split(",")) {
                if (channel.equals("*") && type == MessageInfo.MessageType.NOTICE) {
                    connection.getServerStatusData().addMessage(new StatusMessageInfo(sender.getServerName(),
                            new Date(), StatusMessageInfo.MessageType.NOTICE, params.get(1)));
                    continue;
                }

                ChannelData channelData = connection.getJoinedChannelData(channel);
                ChannelData.Member memberInfo = channelData.getMember(userUUID);
                MessageSenderInfo senderInfo = new MessageSenderInfo(sender.getNick(), sender.getUser(),
                        sender.getHost(), memberInfo != null ? memberInfo.getNickPrefixes() : null, userUUID);
                MessageInfo message = new MessageInfo(senderInfo, new Date(), params.get(1), type);
                channelData.addMessage(message);
            }
        } catch (NoSuchChannelException e) {
            throw new InvalidMessageException("Invalid channel specified in a message", e);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

}