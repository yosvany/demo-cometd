/*
 * Copyright (c) 2008-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.demo.cometd.service;

import com.demo.cometd.dao.TestDAO;
import com.demo.cometd.reposiroty.DBRepository;
import org.cometd.annotation.Listener;
import org.cometd.annotation.Service;
import org.cometd.annotation.Session;
import org.cometd.annotation.server.Configure;
import org.cometd.bayeux.Promise;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.*;
import org.cometd.server.authorizer.GrantAuthorizer;
import org.cometd.server.filter.DataFilterMessageListener;
import org.cometd.server.filter.JSONDataFilter;
import org.cometd.server.filter.NoMarkupFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Autowired;

@Service("chat")
public class ChatService {
    private final ConcurrentMap<String, Map<String, String>> _members = new ConcurrentHashMap<>();

    private final static Logger logger = LoggerFactory.getLogger(ChatService.class);

    @Inject
    private BayeuxServer _bayeux;

    @Session
    private ServerSession _session;


    @Autowired
    private DBRepository repository;



    @Configure({"/chat/**", "/members/**"})
    protected void configureChatStarStar(ConfigurableServerChannel channel) {

        logger.info("[configureChatStarStar] chanel: {}", channel);
        DataFilterMessageListener noMarkup = new DataFilterMessageListener(new NoMarkupFilter(), new BadWordFilter());
        channel.addListener(noMarkup);
        channel.addAuthorizer(GrantAuthorizer.GRANT_ALL);
    }

    @Configure("/service/members")
    protected void configureMembers(ConfigurableServerChannel channel) {

        logger.info("[configureMembers] chanel: {}", channel);
        channel.addAuthorizer(GrantAuthorizer.GRANT_PUBLISH);
        channel.setPersistent(true);
    }

    @Listener("/service/members")
    public void handleMembership(ServerSession client, ServerMessage message) {

        logger.info("[handleMembership]  client: {} - message: {}", client, message);
        Map<String, Object> data = message.getDataAsMap();
        final String room = ((String)data.get("room")).substring("/chat/".length());
        Map<String, String> roomMembers = this._members.get(room);
        if (roomMembers == null) {
            Map<String, String> new_room = new ConcurrentHashMap<>();
            roomMembers = this._members.putIfAbsent(room, new_room);
            if (roomMembers == null) {
                roomMembers = new_room;
            }
        }
        final Map<String, String> members = roomMembers;
        String userName = (String)data.get("user");
        members.put(userName, client.getId());
        client.addListener((ServerSession.RemoveListener)(session, timeout) -> {
            members.values().remove(session.getId());
            broadcastMembers(room, members.keySet());
        });

        broadcastMembers(room, members.keySet());
    }

    private void broadcastMembers(String room, Set<String> members) {
        logger.info("[broadcastMembers]  room: {}, members: {}", room, members);
        // Broadcast the new members list
        ClientSessionChannel channel = this._session.getLocalSession().getChannel("/members/" + room);
        channel.publish(members);
    }

    @Configure("/service/privatechat")
    protected void configurePrivateChat(ConfigurableServerChannel channel) {
        logger.info("[configurePrivateChat] channel: {}", channel);
        DataFilterMessageListener noMarkup = new DataFilterMessageListener(new NoMarkupFilter(), new BadWordFilter());
        channel.setPersistent(true);
        channel.addListener(noMarkup);
        channel.addAuthorizer(GrantAuthorizer.GRANT_PUBLISH);
    }

    @Listener("/service/privatechat")
    public void privateChat(ServerSession client, ServerMessage message) {

        logger.info("[privateChat] client :{}, message: {}", client, message);
        Map<String, Object> data = message.getDataAsMap();
        String room = ((String)data.get("room")).substring("/chat/".length());

        Map<String, String> membersMap = this._members.get(room);
        if (membersMap == null) {
            Map<String, String> new_room = new ConcurrentHashMap<>();
            membersMap = this._members.putIfAbsent(room, new_room);
            if (membersMap == null) {
                membersMap = new_room;
            }
        }
        String[] peerNames = ((String)data.get("peer")).split(",");
        ArrayList<ServerSession> peers = new ArrayList<>(peerNames.length);

        for (String peerName : peerNames) {
            String peerId = membersMap.get(peerName);
            if (peerId != null) {
                ServerSession peer = this._bayeux.getSession(peerId);
                if (peer != null) {
                    peers.add(peer);
                }
            }
        }

        if (peers.size() > 0) {
            Map<String, Object> chat = new HashMap<>();
            String text = (String)data.get("chat");
            chat.put("chat", text);
            chat.put("user", data.get("user"));
            chat.put("scope", "private");
            ServerMessage.Mutable forward = this._bayeux.newMessage();
            forward.setChannel("/chat/" + room);
            forward.setId(message.getId());
            forward.setData(chat);

            // test for lazy messages
            if (text.lastIndexOf("lazy") > 0) {
                forward.setLazy(true);
            }

            for (ServerSession peer : peers) {
                if (peer != client) {
                    peer.deliver(this._session, forward, Promise.noop());
                }
            }
            client.deliver(this._session, forward, Promise.noop());
        }
    }

    @Configure("/service/demo")
    protected void configureServiceDemo(ConfigurableServerChannel channel) {
        logger.debug("configure: /service/demo");
        channel.setPersistent(true);
        channel.addAuthorizer(GrantAuthorizer.GRANT_PUBLISH);

    }

    @Listener("/chat/demo")
    public void handleChatDemo(ServerSession client, ServerMessage message) {
        logger.info("Listener service/demo .. " );
        save();

    }


    public void save(){
        TestDAO dao = new TestDAO();
        dao.setMessage("test");
        logger.debug("saving ....");
        this.repository.save(dao);
        logger.debug("id = {}", dao.getId());
    }


    static class BadWordFilter extends JSONDataFilter {
        @Override
        protected Object filterString(ServerSession session, ServerChannel channel, String string) {
            if (string.contains("dang")) {
                throw new AbortException();
            }
            return string;
        }
    }
}
