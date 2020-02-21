/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

 # execute following http request
 # replace [xxx] to your own information
 curl "http://[hostname]:[port]/robotjulieserver/doRequest?ServiceName=doRemoteSSHCommand&RemoteHost=[remote hostname]&RemoteUser=[remote user]&RemotePass=[remote password]&RemoteCommand=[Command]"
 # after successful executed, you will get a JobId from return, such as {"JobID":"0000000000000"}
 # query the test result
 curl "http://[hostname]:[port]/robotjulieserver/doRequest?ServiceName=getJobLogConsole&JobID=[your job Id]" | jq

*/
package com.robotslacker.sshagent.service;

import java.util.HashMap;
import org.json.JSONObject;
import org.apache.log4j.Logger;
import org.json.JSONException;
import com.robotslacker.sshagent.util.SSHUtil;
import com.robotslacker.sshagent.service.internal.Service;

/**
 *
 * @author shi.zhao
 */
public class doRemoteSSHCommand extends Service
{
    @Override
    public boolean isbgService()
    {
        return true;
    }
    
    @Override
    public JSONObject DoService() 
    {
        try
        {
            HashMap    m_Parameters = getParameters();

            String  p_RemoteUser = (String)m_Parameters.get("RemoteUser");
            String  p_RemoteHost = (String)m_Parameters.get("RemoteHost");
            String  p_RemotePass = (String)m_Parameters.get("RemotePass");
            String  p_RemoteCommand = (String)m_Parameters.get("RemoteCommand");
            m_Parameters.replace("RemotePass","*********");
            
            appendLog("Service Started .");
            appendLog("RemoteHost = " + p_RemoteHost);
            appendLog("RemoteUser = " + p_RemoteUser);
            appendLog("RemotePass = " + "**********");
            appendLog("RemoteCommand = " + p_RemoteCommand);
            
            SSHUtil.executeCommand(p_RemoteHost, p_RemoteUser, p_RemotePass, p_RemoteCommand, this.getConsole());
            
            JSONObject  m_Return = new JSONObject();
            m_Return.put("error_id", 0);
            m_Return.put("error_msg", "Command executed successful!");
            return m_Return;
        }
        catch (JSONException ex)
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

