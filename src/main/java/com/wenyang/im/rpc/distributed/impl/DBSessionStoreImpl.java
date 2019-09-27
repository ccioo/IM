package com.wenyang.im.rpc.distributed.impl;

import com.wenyang.im.rpc.distributed.DBSessionStore;
import com.wenyang.im.rpc.mqtt.session.ClientSession;
import com.wenyang.im.rpc.mqtt.session.ServerSession;
import com.wenyang.im.rpc.jdbc.DBUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * @Author wen.yang
 */
@Slf4j
public class DBSessionStoreImpl implements DBSessionStore {


    @Override
    public ServerSession getSession(String uid, String clientId, ClientSession clientSession) {
        String sql = "select  `_package_name`,`_token`,`_voip_token`,`_secret`,`_db_secret`,`_platform`,`_push_type`,`_device_name`,`_device_version`,`_phone_name`,`_language`,`_carrier_name`, `_dt` from t_user_session where `_uid` = ? and `_cid` = ? limit 1";
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = DBUtil.getConnection();
            statement = connection.prepareStatement(sql);
            statement.setString(1, uid);
            statement.setString(2, clientId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                ServerSession session = new ServerSession(uid, clientId, clientSession);

                int index = 1;
                session.setAppName(resultSet.getString(index++));
                session.setDeviceToken(resultSet.getString(index++));
                session.setVoipDeviceToken(resultSet.getString(index++));
                session.setSecret(resultSet.getString(index++));
                session.setDbSecret(resultSet.getString(index++));
                session.setPlatform(resultSet.getInt(index++));
                session.setPushType(resultSet.getInt(index++));

                session.setDeviceName(resultSet.getString(index++));
                session.setDeviceVersion(resultSet.getString(index++));
                session.setPhoneName(resultSet.getString(index++));
                session.setLanguage(resultSet.getString(index++));
                session.setCarrierName(resultSet.getString(index++));
                session.setUpdateDt(resultSet.getLong(index));
                return session;
            }
            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeDB(connection, statement);
        }
        return null;
    }


    /**
     * 创建会话：持久化
     *
     * @param clientId
     * @param uid
     * @param clientSession
     * @return
     */
    @Override
    public ServerSession createSession(String clientId, String uid, ClientSession clientSession) {
        Connection connection = null;
        PreparedStatement statement = null;
        log.info("Database create session {},{}", uid, clientId);
        try {
            connection = DBUtil.getConnection();
            String sql = "insert into t_user_session  (`_uid`,`_cid`,`_secret`,`_db_secret`, `_dt`) values (?,?,?,?,?)";
            statement = connection.prepareStatement(sql);
            int index = 1;
            ServerSession session = new ServerSession(uid, clientId, clientSession);
            statement.setString(index++, uid);
            statement.setString(index++, clientId);
            session.setSecret(UUID.randomUUID().toString());
            statement.setString(index++, session.getSecret());
            session.setDbSecret(UUID.randomUUID().toString());
            statement.setString(index++, session.getDbSecret());
            statement.setLong(index++, System.currentTimeMillis());
            int count = statement.executeUpdate();
            log.info("Update rows {}", count);
            return session;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeDB(connection, statement);
        }
        return null;
    }
}
