/*
 * Copyright (c) 2012-2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package com.wenyang.im.rpc.mqtt.spi;


import com.wenyang.im.rpc.code.ErrorCode;
import com.wenyang.im.rpc.model.UserClientEntry;
import com.wenyang.im.rpc.mqtt.protocol.WFCMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import lombok.Data;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Defines the SPI to be implemented by a StorageService that handle persistence of messages
 */
public interface IMessagesStore {

    @Data
    class StoredMessage {

        final MqttQoS m_qos;
        final byte[] m_payload;
        final String m_topic;
        private boolean m_retained;
        private String m_clientID;
        private MessageGUID m_guid;

        public StoredMessage(byte[] message, MqttQoS qos, String topic) {
            m_qos = qos;
            m_payload = message;
            m_topic = topic;
        }

    }

    WFCMessage.Message storeMessage(String fromUser, String fromClientId, WFCMessage.Message message);

    void storeSensitiveMessage(WFCMessage.Message message);

    int getNotifyReceivers(String fromUser, WFCMessage.Message.Builder message, Set<String> notifyReceivers);

    Set<String> getAllEnds();

    WFCMessage.PullMessageResult fetchMessage(String user, String exceptClientId, long fromMessageId, int pullType);

    WFCMessage.PullMessageResult loadRemoteMessages(String user, WFCMessage.Conversation conversation, long beforeUid, int count);

    long insertUserMessages(String sender, int conversationType, String target, int line, int messageContentType, String userId, long messageId);

    WFCMessage.GroupInfo createGroup(String operator, WFCMessage.GroupInfo groupInfo, List<WFCMessage.GroupMember> memberList);

    ErrorCode addGroupMembers(String operator, boolean isAdmin, String groupId, List<WFCMessage.GroupMember> memberList);

    ErrorCode kickoffGroupMembers(String operator, boolean isAdmin, String groupId, List<String> memberList);

    ErrorCode quitGroup(String operator, String groupId);

    ErrorCode dismissGroup(String operator, String groupId, boolean isAdmin);

    ErrorCode modifyGroupInfo(String operator, String groupId, int modifyType, String value, boolean isAdmin);

    ErrorCode modifyGroupAlias(String operator, String groupId, String alias);

    List<WFCMessage.GroupInfo> getGroupInfos(List<WFCMessage.UserRequest> requests);

    WFCMessage.GroupInfo getGroupInfo(String groupId);

    ErrorCode getGroupMembers(String groupId, long maxDt, List<WFCMessage.GroupMember> members);

    WFCMessage.GroupMember getGroupMember(String groupId, String memberId);

    ErrorCode transferGroup(String operator, String groupId, String newOwner, boolean isAdmin);

    ErrorCode setGroupManager(String operator, String groupId, int type, List<String> userList, boolean isAdmin);

    boolean isMemberInGroup(String member, String groupId);

    ErrorCode canSendMessageInGroup(String member, String groupId);

    ErrorCode recallMessage(long messageUid, String operatorId, boolean isAdmin);

    WFCMessage.Robot getRobot(String robotId);

    void addRobot(WFCMessage.Robot robot);

    ErrorCode getUserInfo(List<WFCMessage.UserRequest> requestList, WFCMessage.PullUserResult.Builder builder);

    ErrorCode modifyUserInfo(String userId, WFCMessage.ModifyMyInfoRequest request);

    ErrorCode modifyUserStatus(String userId, int status);

    int getUserStatus(String userId);


    void addUserInfo(WFCMessage.User user, String password);

    WFCMessage.User getUserInfo(String userId);

    WFCMessage.User getUserInfoByName(String name);

    WFCMessage.User getUserInfoByMobile(String mobile);

    List<WFCMessage.User> searchUser(String keyword, boolean buzzy, int page);

    boolean updateSystemSetting(int id, String value, String desc);

    void createChatroom(String chatroomId, WFCMessage.ChatroomInfo chatroomInfo);

    void destoryChatroom(String chatroomId);

    /**
     * 获取聊天室信息
     *
     * @param chatroomId
     * @return
     */
    WFCMessage.ChatroomInfo getChatroomInfo(String chatroomId);

    /**
     * 设置聊天室人数 返回聊天室信息
     */
    WFCMessage.ChatroomMemberInfo getChatroomMemberInfo(String chatroomId, final int maxMemberCount);

    /**
     * 获取聊天室人员数目
     *
     * @param chatroomId
     * @return
     */
    int getChatroomMemberCount(String chatroomId);

    /**
     * 获取聊天室有多少个客户端连接着
     *
     * @param userId
     * @return
     */
    Collection<String> getChatroomMemberClient(String userId);

    /**
     * 校验用户是否在聊天室
     */
    boolean checkUserClientInChatroom(String userId, String clientId, String chatroomId);

    /**
     * 聊天室内发送消息
     */
    long insertChatroomMessages(String target, int line, long messageId);

    /**
     * 获取聊天室成员
     */
    Collection<UserClientEntry> getChatroomMembers(String chatroomId);

    /**
     * 某一个用户拉取聊天室消息
     */
    WFCMessage.PullMessageResult fetchChatroomMessage(String fromUser, String chatroomId,
                                                      String exceptClientId, long fromMessageId);


    ErrorCode verifyToken(String userId, String token, List<String> serverIPs, List<Integer> ports);

    ErrorCode login(String name, String password, List<String> userIdRet);


    List<WFCMessage.FriendRequest> getFriendRequestList(String userId, long version);

    ErrorCode saveAddFriendRequest(String userId, WFCMessage.AddFriendRequest request, long[] head);

    ErrorCode handleFriendRequest(String userId, WFCMessage.HandleFriendRequest request, WFCMessage.Message.Builder msgBuilder, long[] heads, boolean isAdmin);

    ErrorCode deleteFriend(String userId, String friendUid, long[] head);

    ErrorCode blackUserRequest(String fromUser, String targetUserId, int status, long[] head);

    ErrorCode SyncFriendRequestUnread(String userId, long unreadDt, long[] head);

    boolean isBlacked(String fromUser, String userId);

    ErrorCode setFriendAliasRequest(String fromUser, String targetUserId, String alias, long[] head);

    ErrorCode handleJoinChatroom(String userId, String clientId, String chatroomId);

    ErrorCode handleQuitChatroom(String userId, String clientId, String chatroomId);

    ErrorCode getUserSettings(String userId, long version, WFCMessage.GetUserSettingResult.Builder builder);

    WFCMessage.UserSettingEntry getUserSetting(String userId, int scope, String key);

    List<WFCMessage.UserSettingEntry> getUserSetting(String userId, int scope);

    long updateUserSettings(String userId, WFCMessage.ModifyUserSettingReq request);

    boolean getUserGlobalSlient(String userId);

    boolean getUserPushHiddenDetail(String userId);

    boolean getUserConversationSlient(String userId, WFCMessage.Conversation conversation);

    ErrorCode createChannel(String operator, WFCMessage.ChannelInfo channelInfo);

    ErrorCode modifyChannelInfo(String operator, String channelId, int modifyType, String value);

    ErrorCode transferChannel(String operator, String channelId, String newOwner);

    ErrorCode distoryChannel(String operator, String channelId);

    List<WFCMessage.ChannelInfo> searchChannel(String keyword, boolean buzzy, int page);

    ErrorCode listenChannel(String operator, String channelId, boolean listen);

    WFCMessage.ChannelInfo getChannelInfo(String channelId);

    boolean checkUserInChannel(String user, String channelId);

    Set<String> handleSensitiveWord(String message);

    boolean addSensitiveWords(List<String> words);

    boolean removeSensitiveWords(List<String> words);

    List<String> getAllSensitiveWords();

    WFCMessage.Message getMessage(long messageId);

    long getMessageHead(String user);

    long getFriendHead(String user);

    long getFriendRqHead(String user);

    long getSettingHead(String user);

    //使用了数据库，会比较慢，仅能用户用户/群组等id的生成
    String getShortUUID();

    /**
     * Used to initialize all persistent store structures
     */
    void initStore();

}
