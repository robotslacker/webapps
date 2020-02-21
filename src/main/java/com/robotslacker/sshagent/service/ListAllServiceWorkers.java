/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 * curl "http://[hostname]:[port]/robotjulieserver/doRequest?ServiceName=ListAllServiceWorkers"|jq 
 */
package com.robotslacker.sshagent.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import com.robotslacker.sshagent.service.internal.Service;
import com.robotslacker.sshagent.service.internal.Worker;
import com.robotslacker.sshagent.service.internal.WorkerService;

/**
 *
 * @author shizhao
 */
public class ListAllServiceWorkers  extends Service
{

    @Override
    public boolean isbgService() {
        return false;
    }

    @Override
    public JSONObject DoService() 
    {
        JSONObject  m_Return = new JSONObject();
        Worker      m_Worker;
        
        HashMap<String,Worker> m_AllServiceWorkers = WorkerService.getAllWorkers();
        if (m_AllServiceWorkers == null)
        {
            m_Return.put("JobWorkerLists","");
            return m_Return;
        }
        Iterator iter = m_AllServiceWorkers.entrySet().iterator();
        JSONArray m_JSONArray = new JSONArray();
        while (iter.hasNext()) 
        {
            Map.Entry entry = (Map.Entry) iter.next();
            JSONObject  m_WorkerDesc = new JSONObject();
            m_Worker = (Worker)entry.getValue();
            m_WorkerDesc.put("JobID", m_Worker.getJobID());
            m_WorkerDesc.put("ServiceName", m_Worker.getServiceName());
            if (m_Worker.getStartedTime() == null)
            {
                m_WorkerDesc.put("StartedTime", "");
            }
            else
            {
                m_WorkerDesc.put("StartedTime", m_Worker.getStartedTime().toString());
            }
            if (m_Worker.getExpiredTime() == null)
            {
                m_WorkerDesc.put("ExpiredTime", "");
            }
            else
            {
                m_WorkerDesc.put("ExpiredTime", m_Worker.getExpiredTime().toString());
            }
            if (m_Worker.getEndTime()== null)
            {
                m_WorkerDesc.put("getEndTime", "");
            }
            else
            {
                m_WorkerDesc.put("getEndTime", m_Worker.getEndTime().toString());
            }
            JSONArray m_JSONParameterArray = new JSONArray();
            HashMap<String,String> m_Parameters = m_Worker.getParameters();
            for (Map.Entry m_ParametersEntry : m_Parameters.entrySet()) 
            {
                JSONObject m_Parameter = new JSONObject();
                m_Parameter.put((String)m_ParametersEntry.getKey(),(String)m_ParametersEntry.getValue());
                m_JSONParameterArray.put(m_Parameter);
            }
            m_WorkerDesc.put("Parameters", m_JSONParameterArray);
            m_JSONArray.put(m_WorkerDesc);
        }
        m_Return.put("JobWorkerLists", m_JSONArray);
        return m_Return;
    }
}

