/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * curl "http://[hostname]:[port]/robotjulieserver/doRequest?ServiceName=getJobLogConsole&JobID=2016072921312900126"|jq 
 */
package com.robotslacker.sshagent.service;

import java.util.HashMap;
import java.util.List;
import org.json.JSONObject;
import com.robotslacker.sshagent.service.internal.Service;
import com.robotslacker.sshagent.service.internal.Worker;
import com.robotslacker.sshagent.service.internal.WorkerService;

/**
 *
 * @author shi.zhao
 */
public class getJobLogConsole  extends Service
{
    @Override
    public boolean isbgService() 
    {
        return false;
    }

    @Override
    public JSONObject DoService() 
    {
        JSONObject  m_Return = new JSONObject();
        
        HashMap    m_Parameters = getParameters();

        String  p_JobID = (String)m_Parameters.get("JobID");
        Worker  m_Worker = WorkerService.getWorker(p_JobID);
        if (m_Worker == null)
        {
            m_Return.put("error_code", -10003);
            m_Return.put("error_msg", "JOB [" + p_JobID + "] does not exist or has been cleaned");
            return m_Return;
        }
        List<String> m_ConsoleLogs = m_Worker.getConsoleLog();
        int nPos = 1;
        for (String m_Message : m_ConsoleLogs)
        {
            m_Return.put(String.valueOf(nPos), m_Message);
            nPos ++;
        }
        return m_Return;
    }
}

