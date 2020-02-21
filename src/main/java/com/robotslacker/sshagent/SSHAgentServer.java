/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robotslacker.sshagent;

import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import org.json.JSONObject;
import com.robotslacker.sshagent.service.internal.Worker;
import com.robotslacker.sshagent.service.internal.WorkerService;
import java.util.logging.FileHandler;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author shizhao
 */
public class SSHAgentServer  extends HttpServlet 
{
    private static String  m_AppLogFileName;
    
    private static void setAppLogFileName(String p_AppLogFileName)
    {
        m_AppLogFileName = p_AppLogFileName;
    }
    private static String getAppLogFileName()
    {
        return m_AppLogFileName;
    }    
    public static void StartLogger()
    {
        int LOGFILE_SIZE = 10240;

        try
        {
            String  m_szLogFileName = getAppLogFileName();
            Logger logger = Logger.getLogger(SSHAgentServer.class.getName());
            FileHandler handler = new FileHandler(m_szLogFileName, LOGFILE_SIZE, 5, true);
            handler.setFormatter(new SimpleFormatter());
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);
        }
        catch (IOException ie)
        {
            System.out.println("RobotSlacker logger start fail, will redirect to [console] ... ");
            ie.printStackTrace();
            Logger logger = Logger.getLogger(SSHAgentServer.class.getName());
            logger.addHandler(new ConsoleHandler());
        }
        java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);
        java.util.logging.Logger.getLogger("org.apache.http.headers").setLevel(java.util.logging.Level.FINEST);                
    }    
    
    @Override
    public void init(ServletConfig config) throws ServletException 
    {
        super.init(config);
        setAppLogFileName(config.getInitParameter("appLogFileName"));
        StartLogger();
    }
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        try 
        {
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
        } 
        catch (UnsupportedEncodingException e) 
        {
            e.printStackTrace();
        }
        try 
        {
            doService_Get(request,response);
        } 
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
    
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException 
    {
        try 
        {
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");
            doService_Post(request,response);
        } 
        catch (Exception ex) 
        {
            java.util.logging.Logger.getLogger("SSHAgentServer").log(Level.SEVERE, null, ex);
        }
    }
    
    public static void doService_Get(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception
    {
        String      m_szServiceName = p_request.getParameter("ServiceName"); 
        JSONObject  m_Return = new JSONObject();
        if (m_szServiceName == null)
        {
            m_Return.put("error_code", -10001);
            m_Return.put("error_msg", "ServiceName is null");
        }
        else
        {
            Worker  m_Worker;
            HashMap<String,String> m_Parameters = new HashMap<>();  
            Enumeration paramNames = p_request.getParameterNames();  
            while (paramNames.hasMoreElements()) 
            {  
                String paramName = (String) paramNames.nextElement();    
                String[] paramValues = p_request.getParameterValues(paramName);  
                if (paramValues.length == 1) 
                {  
                    String paramValue = paramValues[0];  
                    if (paramValue.length() != 0) 
                    {  
                        m_Parameters.put(paramName, paramValue);  
                    }  
                }  
            }  

            m_Worker        = WorkerService.startNewWorker();
            try
            {
                m_Worker.setServiceName(m_szServiceName);
                m_Worker.setParameters(m_Parameters);
                if (m_Worker.getService().isbgService())
                {
                    m_Return.put("JobID", m_Worker.getJobID());
                    m_Worker.start();
                }
                else
                {
                    m_Return = m_Worker.RunOnline();
                    WorkerService.removeWorker(String.valueOf(m_Worker.getJobID()));
                }
            }
            catch (ClassNotFoundException ex)
            {
                m_Return.put("error_code", -999);
                m_Return.put("error_msg", "Service [" + m_szServiceName + "] does not Exist!");
            }
        }   
        
        // Return result
        try 
        {
            PrintWriter out = p_response.getWriter();
            out.write(m_Return.toString());
        }
        catch (IOException ex)
        {
            Logger.getLogger("SSHAgentServer").log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public static void doService_Post(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception
    {
        String      m_szServiceName = p_request.getParameter("ServiceName");
        JSONObject  m_Return = new JSONObject();
        if (m_szServiceName == null)
        {
            m_Return.put("error_code", -10001);
            m_Return.put("error_msg", "ServiceName is null");
        }
        else
        {
            Worker  m_Worker;
            HashMap<String,String> m_Parameters = new HashMap<>();  
            JSONObject m_JsonParameters = ParseJSONHttpRequest(p_request);
            for (Iterator iter = m_JsonParameters.keys();  iter.hasNext();)
            {
                String paramName = (String) iter.next();
                String paramValue = m_JsonParameters.getString(paramName);
                m_Parameters.put(paramName, paramValue);  
            }

            m_Worker        = WorkerService.startNewWorker();
            m_Worker.setServiceName(m_szServiceName);
            m_Worker.setParameters(m_Parameters);
            if (m_Worker.getService().isbgService())
            {
                m_Return.put("JobID", m_Worker.getJobID());
                m_Worker.start();
            }
            else
            {
                m_Return = m_Worker.RunOnline();
                WorkerService.removeWorker(m_Return.getString("JobID"));
            }                    
        }   
        
        // Return result
        try 
        {
            PrintWriter out = p_response.getWriter();
            out.write(m_Return.toString());
        }
        catch (IOException ex)
        {
            Logger.getLogger("SSHAgentServer").log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    public static JSONObject ParseJSONHttpRequest(HttpServletRequest request) throws Exception 
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(request.getInputStream()));        
        StringBuilder sb = new StringBuilder();
        
        String line;
        while((line = br.readLine())!=null)
        {
            sb.append(line);
        }
        JSONObject m_Return =  new JSONObject(URLDecoder.decode(sb.toString(), "UTF-8"));
        return m_Return;
    }
}

