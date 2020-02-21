/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.robotslacker.sshagent.service;

import java.io.File;
import java.util.HashMap;
import org.json.JSONObject;
import com.robotslacker.sshagent.service.internal.Service;
/**
 *
 * @author shizhao
 */
public class SetUploadFileProperties extends Service
{
    @Override
    public boolean isbgService() 
    {
        return false;
    }

    @Override
    public JSONObject DoService() throws InterruptedException 
    {
        JSONObject  m_Return = new JSONObject();
        HashMap     m_Parameters = getParameters();
        String      p_FileName = (String)m_Parameters.get("FileName");
        String      p_Execute = (String)m_Parameters.get("Executable");
        String      p_ExecuteOwnerOnly = (String)m_Parameters.get("ExecutableOwnerOnly");
        String      p_Write = (String)m_Parameters.get("Writable");
        String      p_WriteOwnerOnly = (String)m_Parameters.get("WritableOwnerOnly");
        String      p_Read = (String)m_Parameters.get("Readable");
        String      p_ReadOwnerOnly = (String)m_Parameters.get("ReadableOwnerOnly");
        
        if (p_FileName == null)
        {
            m_Return.put("error_code", -10003);
            m_Return.put("error_msg", "FileName can not be null.");            
            return m_Return;
        }
        
        File    m_file = new File(p_FileName);
        if (p_Execute != null)
        {
            boolean m_Execute;
            boolean m_ExecuteOwnerOnly = false;
            m_Execute = p_Execute.equalsIgnoreCase("true");
            if (p_ExecuteOwnerOnly != null)
            {
                m_ExecuteOwnerOnly = p_ExecuteOwnerOnly.equalsIgnoreCase("true");
            }
            m_file.setExecutable(m_Execute, m_ExecuteOwnerOnly);
        }
        if (p_Write != null)
        {
            boolean m_Write;
            boolean m_WriteOwnerOnly = false;
            m_Write = p_Write.equalsIgnoreCase("true");
            if (p_WriteOwnerOnly != null)
            {
                m_WriteOwnerOnly = p_WriteOwnerOnly.equalsIgnoreCase("true");
            }
            m_file.setWritable(m_Write, m_WriteOwnerOnly);
        }        
        if (p_Read != null)
        {
            boolean m_Read;
            boolean m_ReadOwnerOnly = false;
            m_Read = p_Read.equalsIgnoreCase("true");
            if (p_ReadOwnerOnly != null)
            {
                m_ReadOwnerOnly = p_ReadOwnerOnly.equalsIgnoreCase("true");
            }
            m_file.setReadable(m_Read, m_ReadOwnerOnly);
        }
        return m_Return;
    }
}

