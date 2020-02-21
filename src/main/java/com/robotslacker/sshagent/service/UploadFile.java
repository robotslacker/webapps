/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robotslacker.sshagent.service;

import com.jcraft.jsch.JSchException;
import java.io.IOException;
import java.util.HashMap;
import org.json.JSONObject;
import org.apache.log4j.Logger;

import com.robotslacker.sshagent.util.SSHUtil;
import com.robotslacker.sshagent.service.internal.Service;

/**
 *
 * @author shi.zhao
 */
public class UploadFile  extends Service
{
    @Override
    public boolean isbgService() 
    {
        return false;
    }

    @Override
    public JSONObject DoService() 
    {
        HashMap    m_Parameters = getParameters();

        String  p_RemoteUser = (String)m_Parameters.get("RemoteUser");
        String  p_RemoteHost = (String)m_Parameters.get("RemoteHost");
        String  p_RemotePass = (String)m_Parameters.get("RemotePass");
        String  p_RemotePath = (String)m_Parameters.get("RemotePath");
        String  p_RemoteFile = (String)m_Parameters.get("RemoteFile");
        String  p_LocalFile = (String)m_Parameters.get("LocalFile");
        String  p_LocalPath = (String)m_Parameters.get("LocalPath");
        
        try
        {
            SSHUtil.ScpFile(p_LocalPath, p_LocalFile, p_RemoteHost, p_RemoteUser, p_RemotePass, p_RemotePath, p_RemoteFile, this.getConsole());
 
            JSONObject  m_Return = new JSONObject();
            m_Return.put("error_id", 0);
            m_Return.put("error_msg", "Command executed successful!");
            return m_Return;
        }
        catch (JSchException | IOException ex)
        {
            this.appendLog(ex.getMessage());
            Logger.getRootLogger().error(ex);
            JSONObject  m_Return = new JSONObject();
            m_Return.put("error_id", -30001);
            m_Return.put("error_msg", "Command executed failed!");
            return m_Return;
        }
    }
}

