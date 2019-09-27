package com.wenyang.im.rpc.distributed;

import com.wenyang.im.rpc.jdbc.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.wenyang.im.rpc.constants.Constants.MAX_MESSAGE_QUEUE;

/**
 * @Author wen.yang
 */
public class ServiceMessageStore {

    //线程安全类
    private ReadWriteLock mLock = new ReentrantReadWriteLock();
    private Lock mReadLock = mLock.readLock();
    private Lock mWriteLock = mLock.writeLock();

    TreeMap<Long, Long> reloadUserMessageMaps(String userId) throws Exception {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        TreeMap<Long, Long> out = new TreeMap<>();
        try {
            connection = DBUtil.getConnection();

            String sql = "select `_seq`, `_mid` from " + getUserMessageTable(userId) + " where `_uid` = ? order by `_seq` DESC limit " + MAX_MESSAGE_QUEUE;

            statement = connection.prepareStatement(sql);
            statement.setString(1, userId);
            statement.executeQuery();
            rs = statement.executeQuery();
            while (rs.next()) {
                int index = 1;
                long msgSeq = rs.getLong(index++);
                long msgId = rs.getLong(index);
                out.put(msgSeq, msgId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeDB(connection, statement, rs);
        }

        return out;
    }

    private Map<String, TreeMap<Long, Long>> userMessages = new HashMap<>();


    public long getFriendHead(String user) {
        return 0;
    }

    public long getFriendRqHead(String user) {
        return 0;
    }

    public long getSettingHead(String user) {
        return 0;
    }

    public long getMessageHead(String user) {
        TreeMap<Long, Long> maps = userMessages.get(user);
        if (maps == null) {
            loadUserMessages(user);
        }
        mReadLock.lock();
        try {
            maps = userMessages.get(user);
            Map.Entry<Long, Long> lastEntry = maps.lastEntry();
            if (lastEntry != null) {
                return lastEntry.getKey();
            }
        } finally {
            mReadLock.unlock();
        }
        return 0;
    }

    private void loadUserMessages(String user) {
        mWriteLock.lock();
        try {
            TreeMap<Long, Long> maps = userMessages.get(user);
            if (maps == null) {
                maps = reloadUserMessageMaps(user);
                userMessages.put(user, maps);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mWriteLock.unlock();
        }
    }

    private String getUserMessageTable(String uid) {
        if (DBUtil.IsEmbedDB) {
            return "t_user_messages";
        }
        int hashId = Math.abs(uid.hashCode())%128;
        return "t_user_messages_" + hashId;
    }
}
