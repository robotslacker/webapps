/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robotslacker.sshagent.service.internal;

import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;

/**
 *
 * @author shi.zhao
 */
public abstract class Service 
{
    private HashMap<String,String>  m_Parameters = new HashMap<>(); 
    private List<String>            m_ConsoleLogs;
    
    public HashMap<String,String> getParameters()
    {
        return m_Parameters;
    }

    public void setParameters(HashMap<String,String> p_Parameters)
    {
        m_Parameters = p_Parameters;
    }

    public void setConsole(List<String> p_ConsoleLog)
    {
        m_ConsoleLogs = p_ConsoleLog;
    }
    
    protected void appendLog(String p_szLogMessage)
    {
        m_ConsoleLogs.add(p_szLogMessage);
    }
    
    public List<String> getConsole()
    {
        return m_ConsoleLogs;
    }
    
    public abstract boolean isbgService(); 
    public abstract JSONObject DoService() throws InterruptedException; 
}

