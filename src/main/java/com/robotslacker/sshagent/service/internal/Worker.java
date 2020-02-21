/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robotslacker.sshagent.service.internal;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 * @author shi.zhao
 */
public class Worker extends Thread
{
    private final String            JobID;
    private String                  ServiceName;
    private Date                    StartedTime;
    private Date                    ExpiredTime;
    private Date                    EndTime;
    private Class                   m_ServiceHandleClass;
    Service                         m_ServiceHandle;
    private boolean                 m_isbgJob;
    private final List<String>      m_ConsoleLogs = new ArrayList<>(); 
    
    private static int seq = 0;  
    private static final int ROTATION = 99999;  
    private static synchronized String getNextUID()
    {          
        StringBuilder   m_buf = new StringBuilder();  
        Date            m_CurrentDate = new Date();
        if (seq > ROTATION) seq = 0;  
        m_buf.delete(0, m_buf.length());  
        m_CurrentDate.setTime(System.currentTimeMillis());
        return String.format("%1$tY%1$tm%1$td%1$tk%1$tM%1$tS%2$05d", m_CurrentDate, seq++);  
    }  
    
    protected Worker()
    {
        JobID = getNextUID();
    }
    
    public String getServiceName()
    {
        return ServiceName;
    }
    
    protected void appendLog(String p_szLogMessage)
    {
        synchronized(this)
        {
            m_ConsoleLogs.add(p_szLogMessage);
        }
    }
    
    public List<String> getConsoleLogs()
    {
        synchronized(this)
        {
            return m_ConsoleLogs;
        }
    }
    
    public void setServiceName(String p_ServiceName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException
    {
        ServiceName = p_ServiceName;
        m_ServiceHandleClass = Class.forName(this.getClass().getPackage().getName() + "." + getServiceName());
        m_ServiceHandle = (Service)(m_ServiceHandleClass.getDeclaredConstructor().newInstance());
    }
    
    public Service getService()
    {
        return m_ServiceHandle;
    }
    
    public String getJobID()
    {
        return JobID;
    }

    public void setParameters(HashMap<String,String> p_Parameters)
    {
        m_ServiceHandle.setParameters(p_Parameters);
    }

    public HashMap<String,String> getParameters()
    {
        return m_ServiceHandle.getParameters();
    }
    
    public List<String> getConsoleLog()
    {
        return getConsoleLogs();
    }
    
    public Date getStartedTime()
    {
        return StartedTime;
    }

    public Date getEndTime()
    {
        return EndTime;
    }

    public Date getExpiredTime()
    {
        return ExpiredTime;
    }

    public void setExpiredTime(Date p_ExpiredTime)
    {
        ExpiredTime = p_ExpiredTime;
    }

    public boolean isbgJob()
    {
        return m_isbgJob;
    }

    public void setbgJob(boolean p_setbgJob)
    {
        m_isbgJob = p_setbgJob;
    }

    public JSONObject RunOnline() throws InterruptedException
    {
        StartedTime = new Date();
        StartedTime.setTime(System.currentTimeMillis());
        if (getExpiredTime() == null)
        {
            ExpiredTime = new Date();
            ExpiredTime.setTime(System.currentTimeMillis() + 3*3600*1000);
        }
        m_ServiceHandle.setConsole(m_ConsoleLogs);
        this.appendLog("[Worker-" + this.getJobID() + "]: will start service [" + this.getServiceName() + "] ... ");
        Logger.getLogger("SSHAgentServer").log(Level.INFO, "[Worker-{0}]: will start service [{1}] ... ", new Object[]{this.getJobID(), this.getServiceName()});
        JSONObject m_Return = m_ServiceHandle.DoService();
        if (m_Return.has("error_code"))
        {
            if (m_Return.getInt("error_code") != 0 )
            {
                this.appendLog("[Service Fail] " + 
                            "error_code=<" + String.valueOf(m_Return.getInt("error_code")) + "> error_msg=<" + m_Return.getString("error_msg") + ">" );
            }
        }
        this.appendLog("[Worker-" + this.getJobID() + "]: Done. [" + this.getServiceName() + "]");
        Logger.getLogger("SSHAgentServer").log(Level.INFO, "[Worker-{0}]: Done. [{1}] ", new Object[]{this.getJobID(), this.getServiceName()});
        EndTime = new Date();
        EndTime.setTime(System.currentTimeMillis());
        return m_Return;
    }
    
    @Override
    public void run() 
    {
        try 
        {
            StartedTime = new Date();
            StartedTime.setTime(System.currentTimeMillis());
            if (getExpiredTime() == null)
            {
                ExpiredTime = new Date();
                ExpiredTime.setTime(System.currentTimeMillis() + 3*3600*1000);
            }
            m_ServiceHandle.setConsole(m_ConsoleLogs);
            this.appendLog("[Worker-" + this.getJobID() + "]: will start service [" + this.getServiceName() + "] ... ");
            Logger.getLogger("SSHAgentServer").info("[Worker-" + this.getJobID() + "]: will start service [" + this.getServiceName() + "] ... ");
            JSONObject m_Return = m_ServiceHandle.DoService();
            if (m_Return.has("error_code"))
            {
                if (!"0".equals(m_Return.getString("error_code")))
                {
                    this.appendLog("[Service Fail] error_code=<" + m_Return.getString("error_code") + "> error_msg=<" + m_Return.getString("error_msg") + ">" );
                }
            }
            this.appendLog("[Worker-" + this.getJobID() + "]: Done. [" + this.getServiceName() + "]");
            Logger.getLogger("SSHAgentServer").log(Level.INFO, "[Worker-{0}]: Done. [{1}] ", new Object[]{this.getJobID(), this.getServiceName()});
            EndTime = new Date();
            EndTime.setTime(System.currentTimeMillis());
        } catch (InterruptedException ex) 
        {
            java.util.logging.Logger.getLogger(Worker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

