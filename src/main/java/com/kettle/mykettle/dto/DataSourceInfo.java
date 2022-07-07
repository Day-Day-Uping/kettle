package com.kettle.mykettle.dto;

/**
 * @program: mykettle
 * @description: 数据源
 * @author: Mr.HuangDaDa
 * @create: 2022-06-28 15:51
 **/
public class DataSourceInfo {
    private String linkName;
    private String hostName;
    private String DBName;
    private String DBPort;
    private String userName;
    private String passWord;

    public String getLinkName() {
        return linkName;
    }

    public void setLinkName(String linkName) {
        this.linkName = linkName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getDBName() {
        return DBName;
    }

    public void setDBName(String DBName) {
        this.DBName = DBName;
    }

    public String getDBPort() {
        return DBPort;
    }

    public void setDBPort(String DBPort) {
        this.DBPort = DBPort;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
}
