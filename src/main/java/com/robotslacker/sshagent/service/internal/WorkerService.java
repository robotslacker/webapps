/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robotslacker.sshagent.service.internal;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author shi.zhao
 */
public class WorkerService 
{
    private static final HashMap<String, Worker> WORKERLISTS = new HashMap<>();

    static class WorkerCleaner extends Thread
    {
        static boolean m_isStarted = false;
        public static boolean isStarted()
        {
            return m_isStarted;
        }
        
        @Override
        public void run() 
        {
            m_isStarted = true;
            Logger.getLogger(WorkerService.class.getName()).log(Level.INFO, "WorkerCleaner started ...");
            while (true)
            {
                try 
                {
                    synchronized (this)
                    {
                        for (Map.Entry entry : WORKERLISTS.entrySet()) 
                        {
                            Worker m_Worker = (Worker)entry.getValue();
                            Date      m_CurrentDate = new Date();
                            m_CurrentDate.setTime(System.currentTimeMillis());
                            if (m_Worker.getExpiredTime() != null)
                            {
                                if (m_CurrentDate.compareTo(m_Worker.getExpiredTime()) > 0)
                                {
                                    Logger.getLogger(WorkerService.class.getName()).log(Level.INFO, 
                                            "WorkerCleaner remove expired job information <{0}>", (String)entry.getKey());
                                    WORKERLISTS.remove((String)entry.getKey());
                                    m_Worker.interrupt();
                                }
                            }                            
                        }
                    }
                    Thread.sleep(30*1000);
                } catch (InterruptedException ex) 
                {
                    Logger.getLogger(WorkerService.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static Worker startNewWorker()
    {
        if (!WorkerCleaner.isStarted())
        {
            WorkerCleaner   m_WorkerCleaner = new WorkerCleaner();
            m_WorkerCleaner.start();
        }
        Worker m_Worker = new Worker();
        WORKERLISTS.put(String.valueOf(m_Worker.getJobID()), m_Worker);
        return m_Worker;
    }
    
    public static void removeWorker(String p_szJobID) throws Exception
    {
        if (!WorkerCleaner.isStarted())
        {
            WorkerCleaner   m_WorkerCleaner = new WorkerCleaner();
            m_WorkerCleaner.start();
        }
        if (WORKERLISTS.containsKey(p_szJobID))
        {
            WORKERLISTS.remove(p_szJobID);
        }
        else
        {
            throw new Exception("[" + p_szJobID + "] does not exist!");
        }
    }
    
    public static Worker getWorker(String p_szWorkerID)
    {
        return WORKERLISTS.get(p_szWorkerID);
    }
    
    public static HashMap<String, Worker> getAllWorkers()
    {
        return WORKERLISTS;
    }
}

