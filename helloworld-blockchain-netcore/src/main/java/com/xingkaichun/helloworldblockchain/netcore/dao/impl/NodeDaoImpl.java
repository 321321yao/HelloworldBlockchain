package com.xingkaichun.helloworldblockchain.netcore.dao.impl;

import com.google.common.base.Strings;
import com.xingkaichun.helloworldblockchain.core.utils.FileUtil;
import com.xingkaichun.helloworldblockchain.core.utils.JdbcUtil;
import com.xingkaichun.helloworldblockchain.core.utils.SqliteUtil;
import com.xingkaichun.helloworldblockchain.netcore.dao.NodeDao;
import com.xingkaichun.helloworldblockchain.netcore.model.NodeEntity;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author 邢开春 微信HelloworldBlockchain 邮箱xingkaichun@qq.com
 */
public class NodeDaoImpl implements NodeDao {

    public NodeDaoImpl(String blockchainDataPath) {
        this.blockchainDataPath = blockchainDataPath;
        init();
    }

    private void init() {
        String createTable1Sql1 = "CREATE TABLE IF NOT EXISTS [Node](" +
                "  [ip] VARCHAR(20) NOT NULL, " +
                "  [port] INTEGER NOT NULL, " +
                "  [blockChainHeight] INTEGER NOT NULL, " +
                "  [isNodeAvailable] INTEGER NOT NULL, " +
                "  [errorConnectionTimes] INTEGER NOT NULL, " +
                "  [fork] INTEGER NOT NULL, " +
                "  UNIQUE([ip], [port]));";
        JdbcUtil.executeSql(connection(),createTable1Sql1);
    }

    @Override
    public NodeEntity queryNode(String ip, int port){
        checkIp(ip);
        checkPort(port);
        String sql = "select * from Node WHERE ip = ? and port = ?";
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            preparedStatement.setString(1,ip);
            preparedStatement.setLong(2,port);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()){
                return resultSetToNodeEntity(resultSet);
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return null;
    }

    @Override
    public List<NodeEntity> queryAllNoForkNodeList(){
        String sql = "select * from Node where fork = 0";
        PreparedStatement preparedStatement = null;
        List<NodeEntity> list = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                list.add(resultSetToNodeEntity(resultSet));
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return list;
    }

    @Override
    public List<NodeEntity> queryAllNoForkAliveNodeList(){
        String sql = "select * from Node WHERE isNodeAvailable = 1 and fork = 0";
        PreparedStatement preparedStatement = null;
        List<NodeEntity> list = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                list.add(resultSetToNodeEntity(resultSet));
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return list;
    }

    @Override
    public void addNode(NodeEntity node){
        checkIp(node.getIp());
        checkPort(node.getPort());

        String sql1 = "        INSERT INTO Node (ip, port, blockChainHeight, isNodeAvailable, errorConnectionTimes, fork)" +
                "        VALUES (?,?,?,?,?,?)";
        PreparedStatement preparedStatement1 = null;
        try {
            preparedStatement1 = connection().prepareStatement(sql1);
            preparedStatement1.setString(1,node.getIp());
            preparedStatement1.setLong(2,node.getPort());
            preparedStatement1.setLong(3,node.getBlockChainHeight().intValue());
            preparedStatement1.setLong(4,SqliteUtil.booleanToLong(node.getIsNodeAvailable()));
            preparedStatement1.setLong(5,node.getErrorConnectionTimes());
            preparedStatement1.setLong(6,SqliteUtil.booleanToLong(node.getFork()));
            preparedStatement1.executeUpdate();
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement1);
        }
    }

    @Override
    public void updateNode(NodeEntity node){
        checkIp(node.getIp());
        checkPort(node.getPort());

        NodeEntity nodeEntity = queryNode(node.getIp(),node.getPort());
        if(node.getBlockChainHeight()==null){
            node.setBlockChainHeight(nodeEntity.getBlockChainHeight());
        }
        if(node.getIsNodeAvailable()==null){
            node.setIsNodeAvailable(nodeEntity.getIsNodeAvailable());
        }
        if(node.getErrorConnectionTimes()==null){
            node.setErrorConnectionTimes(nodeEntity.getErrorConnectionTimes());
        }
        if(node.getFork()==null){
            node.setFork(nodeEntity.getFork());
        }

        String sql1 = "UPDATE Node SET blockChainHeight = ? ,isNodeAvailable = ? ," +
                "                errorConnectionTimes = ?, fork = ? where ip = ? and port = ?";
        PreparedStatement preparedStatement1 = null;
        try {
            preparedStatement1 = connection().prepareStatement(sql1);
            preparedStatement1.setLong(1,node.getBlockChainHeight().intValue());
            preparedStatement1.setLong(2,SqliteUtil.booleanToLong(node.getIsNodeAvailable()));
            preparedStatement1.setLong(3,node.getErrorConnectionTimes());
            preparedStatement1.setLong(4,SqliteUtil.booleanToLong(node.getFork()));
            preparedStatement1.setString(5,node.getIp());
            preparedStatement1.setLong(6,node.getPort());
            preparedStatement1.executeUpdate();
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement1);
        }
    }

    @Override
    public void deleteNode(String ip, int port){
        checkIp(ip);
        checkPort(port);

        String sql1 = "delete from Node WHERE ip = ? and port = ?";
        PreparedStatement preparedStatement1 = null;
        try {
            preparedStatement1 = connection().prepareStatement(sql1);
            preparedStatement1.setString(1,ip);
            preparedStatement1.setLong(2,port);
            preparedStatement1.executeUpdate();
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement1);
        }
    }

    @Override
    public List<NodeEntity> queryAllNodeList(){
        String sql = "select * from Node";
        PreparedStatement preparedStatement = null;
        List<NodeEntity> list = new ArrayList<>();
        ResultSet resultSet = null;
        try {
            preparedStatement = connection().prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()){
                list.add(resultSetToNodeEntity(resultSet));
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            JdbcUtil.closeStatement(preparedStatement);
            JdbcUtil.closeResultSet(resultSet);
        }
        return list;
    }



    private static final String NODE_SYNCHRONIZE_DATABASE_DIRECT_NAME = "NetCoreDatabase";
    private static final String NODE_SYNCHRONIZE_DATABASE_File_Name = "NodeDaoImpl.db";

    private String blockchainDataPath;
    private Connection connection;

    private synchronized Connection connection() {
        try {
            if(connection != null && !connection.isClosed()){
                return connection;
            }
            File nodeSynchronizeDatabaseDirect = new File(blockchainDataPath,NODE_SYNCHRONIZE_DATABASE_DIRECT_NAME);
            FileUtil.mkdir(nodeSynchronizeDatabaseDirect);
            File nodeSynchronizeDatabasePath = new File(nodeSynchronizeDatabaseDirect,NODE_SYNCHRONIZE_DATABASE_File_Name);
            String jdbcConnectionUrl = JdbcUtil.getJdbcConnectionUrl(nodeSynchronizeDatabasePath.getAbsolutePath());
            connection = DriverManager.getConnection(jdbcConnectionUrl);
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private NodeEntity resultSetToNodeEntity(ResultSet resultSet) {
        try {
            String ip = resultSet.getString("ip");
            Integer port = resultSet.getInt("port");
            long blockChainHeight = resultSet.getLong("blockChainHeight");
            long isNodeAvailable = resultSet.getLong("isNodeAvailable");
            long errorConnectionTimes = resultSet.getLong("errorConnectionTimes");
            long fork = resultSet.getLong("fork");

            NodeEntity entity = new NodeEntity();
            entity.setIp(ip);
            entity.setPort(port);
            entity.setBlockChainHeight(blockChainHeight);
            entity.setIsNodeAvailable(SqliteUtil.longToBoolean(isNodeAvailable));
            entity.setErrorConnectionTimes((int) errorConnectionTimes);
            entity.setFork(SqliteUtil.longToBoolean(fork));
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void checkIp(String ip) {
        if(Strings.isNullOrEmpty(ip)){
            throw new NullPointerException("ip不合法");
        }
    }

    private void checkPort(int port) {
        if(port <= 0){
            throw new NullPointerException("port不合法");
        }
    }
}
