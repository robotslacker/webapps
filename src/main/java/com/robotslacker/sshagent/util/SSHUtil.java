
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.

 */
package com.robotslacker.sshagent.util;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author shizhao
 */
public class SSHUtil 
{
    public static abstract class RobotJulieSSHJschUserInfo
                          implements UserInfo, UIKeyboardInteractive
    {
        @Override
        public String getPassword(){ return null; }
        @Override
        public boolean promptYesNo(String str){ return true; }
        @Override
        public String getPassphrase(){ return null; }
        @Override
        public boolean promptPassphrase(String message){ return false; }
        @Override
        public boolean promptPassword(String message){ return false; }
        @Override
        public void showMessage(String message){ }
        @Override
        public String[] promptKeyboardInteractive(String destination,
                                              String name,
                                              String instruction,
                                              String[] prompt,
                                              boolean[] echo){
            return null;
        }
    }

    static int checkAck(InputStream Jschin) throws IOException
    {
        int b=Jschin.read();
        // b may be 0 for success,
        //          1 for error,
        //          2 for fatal error,
        //          -1
        if(b==0) return b;
        if(b==-1) return b;
     
        if(b==1 || b==2)
        {
        	StringBuilder sb=new StringBuilder();
        	int c;
        	do {
        		c=Jschin.read();
        		sb.append((char)c);
        	}
        	while(c!='\n');
        	if(b==1)
        	{ // error
        		System.out.print(sb.toString());
        	}
        	if(b==2)
        	{ // fatal error
        		System.out.print(sb.toString());
        	}
        }
        return b;
    }    
    
    public static void executeCommand(String p_RemoteHost, String p_RemoteUser, String p_RemotePass, String p_RemoteCommand, List<String> p_ConsoleLog)
    {
        try
        {
            p_ConsoleLog.add("Will execute command [" + p_RemoteCommand + "] on " + p_RemoteHost + " with user " + p_RemoteUser);
            
            JSch jsch = new JSch();
            Session m_RExecSession = jsch.getSession(p_RemoteUser, p_RemoteHost, 22);
            m_RExecSession.setPassword(p_RemotePass);

            UserInfo m_RExecUserInfo = new RobotJulieSSHJschUserInfo(){};
            m_RExecSession.setUserInfo(m_RExecUserInfo);
            m_RExecSession.connect();

            Channel m_RExecChannel = m_RExecSession.openChannel("exec");
            ((ChannelExec)m_RExecChannel).setCommand(p_RemoteCommand);
            m_RExecChannel.setInputStream(null);
            ((ChannelExec)m_RExecChannel).setErrStream(System.err);
            InputStream JschRExecin = m_RExecChannel.getInputStream();
            m_RExecChannel.connect();

            String output = "";
            while(true)
            {
                byte[] tmp=new byte[1024];
                while(JschRExecin.available()>0)
                {
                    int i = JschRExecin.read(tmp, 0, 1024);
                    if ( i < 0 )
                    {
                        break;
                    }
                    output = output + new String(tmp, 0, i);
                    String[] m_OutputLines = output.split("\n");                    
                    if ( i < 1024 )
                    {                        
                        p_ConsoleLog.addAll(Arrays.asList(m_OutputLines));
                        output = "";
                    }
                    else
                    {                        
                        for ( int m_nPos = 0; m_nPos < m_OutputLines.length - 1; m_nPos ++)
                        {
                            p_ConsoleLog.add(m_OutputLines[m_nPos]);
                        }
                        output = m_OutputLines[m_OutputLines.length - 1];
                    }
                }
                if(m_RExecChannel.isClosed())
                {
                    if (m_RExecChannel.getExitStatus() != 0)
                    {
                        p_ConsoleLog.add("Command exit with <" + m_RExecChannel.getExitStatus() + ">");
                    }
                    break;
                }
                try{Thread.sleep(1000);}catch(InterruptedException ee){}
            }
            m_RExecChannel.disconnect();
            m_RExecSession.disconnect();
        }
        catch (JSchException | IOException ex)
        {
            p_ConsoleLog.add(ex.getMessage());
        }
    }

    public static void ScpFile(String p_LocalPath, String p_LocalFile, String p_RemoteHost, String p_RemoteUser, String p_RemotePass, String p_RemotePath, String p_RemoteFile, List<String> p_ConsoleLog) throws JSchException, IOException
    {
        JSch jsch=new JSch();            

        // Transfer file
        com.jcraft.jsch.Session 	m_ScpToSession = jsch.getSession(p_RemoteUser, p_RemoteHost, 22);
        m_ScpToSession.setPassword(p_RemotePass);
        UserInfo 	m_ScpToUserInfo = new RobotJulieSSHJschUserInfo() {};
        m_ScpToSession.setUserInfo(m_ScpToUserInfo);
        m_ScpToSession.connect();
        
        String 		m_szScpToCommand = "scp -t " + p_RemotePath + "/" + p_RemoteFile;                    
        Channel 	m_ScpTochannel = m_ScpToSession.openChannel("exec");
        ((ChannelExec)m_ScpTochannel).setCommand(m_szScpToCommand);
        InputStream JschScpToin = m_ScpTochannel.getInputStream();                    
        OutputStream JschScpToout = m_ScpTochannel.getOutputStream();
        m_ScpTochannel.connect();
        int m_ScpToAckRet = checkAck(JschScpToin);
        if( m_ScpToAckRet != 0)
        {
            if (p_ConsoleLog == null)
            {
                System.out.println("Failed to transfer file!. error_code =<" + m_ScpToAckRet + ">");
            }
            else
            {
                p_ConsoleLog.add("Failed to transfer file!. error_code =<" + m_ScpToAckRet + ">");
            }
            return;
        }

        File m_HandleLocalFile = new File(p_LocalPath + File.separator + p_LocalFile);
        
        // send "C0644 file size filename", where filename should not include '/'
        long m_lnFileSize = m_HandleLocalFile.length();
        m_szScpToCommand = "C0644 " + m_lnFileSize + " " + p_RemoteFile + "\n";
        JschScpToout.write(m_szScpToCommand.getBytes()); 
        JschScpToout.flush();
        m_ScpToAckRet = checkAck(JschScpToin);
        if( m_ScpToAckRet != 0)
        {
            if (p_ConsoleLog == null)
            {
                System.out.println("Failed to transfer file!. error_code =<" + m_ScpToAckRet + ">");
            }
            else
            {
                p_ConsoleLog.add("Failed to transfer file!. error_code =<" + m_ScpToAckRet + ">");
            }
            return;
        }
        // send a content of local file
        FileInputStream m_LocalFileInputStream = new FileInputStream(m_HandleLocalFile);
        byte[] buf=new byte[1024];
        while(true)
        {
            int len = m_LocalFileInputStream.read(buf, 0, buf.length);
            if(len<=0) break;
            JschScpToout.write(buf, 0, len);
        }
        m_LocalFileInputStream.close();
        // send '\0'
        buf[0]=0;
        JschScpToout.write(buf, 0, 1);
        JschScpToout.flush();
        m_ScpToAckRet = checkAck(JschScpToin);
        if( m_ScpToAckRet != 0)
        {
            if (p_ConsoleLog == null)
            {
                System.out.println("Failed to transfer file!. error_code =<" + m_ScpToAckRet + ">");
            }
            else
            {
                p_ConsoleLog.add("Failed to transfer file!. error_code =<" + m_ScpToAckRet + ">");
            }
            return;
        }
        JschScpToout.close();          
        m_ScpToSession.disconnect();
        m_ScpTochannel.disconnect();
    }

    public static Session getSSHSessionHandle(String p_RemoteHost, String p_RemoteUser, String p_RemotePass) throws JSchException
    {
        try
        {
            JSch jsch = new JSch();
            Session m_RExecSession = jsch.getSession(p_RemoteUser, p_RemoteHost, 22);
            m_RExecSession.setPassword(p_RemotePass);

            UserInfo m_RExecUserInfo = new RobotJulieSSHJschUserInfo(){};
            m_RExecSession.setUserInfo(m_RExecUserInfo);
            m_RExecSession.connect();
            return m_RExecSession;
        }
        catch (JSchException je)
        {
            //System.out.println("FAIL:  getSSHSessionHandle() fail with [" + p_RemoteUser + "/" + p_RemotePass + "@" + p_RemoteHost + "]");
            throw je;
        }
    }

    public static int executeCommand(Session p_RExecSession, String p_RemoteCommand, List<String> p_ConsoleLog) throws JSchException, IOException
    {
            Channel m_RExecChannel = p_RExecSession.openChannel("exec");
            ((ChannelExec)m_RExecChannel).setCommand(p_RemoteCommand);
            
            ((ChannelExec)m_RExecChannel).setInputStream(null);

            InputStream JschRExecin = ((ChannelExec)m_RExecChannel).getInputStream();
            InputStream JschRExecerr = ((ChannelExec)m_RExecChannel).getErrStream();

            m_RExecChannel.connect();

            String output = "";
            while(true)
            {
                byte[] tmp=new byte[1024];
                while ((JschRExecin.available() > 0) || (JschRExecerr.available() > 0))
                {
                    if (JschRExecin.available() > 0)
                    {
                        int i = JschRExecin.read(tmp, 0, 1024);
                        if ( i < 0 )
                        {
                            break;
                        }
                        output = output + new String(tmp, 0, i);
                        String[] m_OutputLines = output.split("\n");                    
                        if ( i < 1024 )
                        {
                            p_ConsoleLog.addAll(Arrays.asList(m_OutputLines));
                            output = "";
                        }
                        else
                        {                        
                            for ( int m_nPos = 0; m_nPos < m_OutputLines.length - 1; m_nPos ++)
                            {
                                p_ConsoleLog.add(m_OutputLines[m_nPos]);
                            }
                            output = m_OutputLines[m_OutputLines.length - 1];
                        }
                    }
                    if (JschRExecerr.available() > 0)
                    {
                        int i = JschRExecerr.read(tmp, 0, 1024);
                        if ( i < 0 )
                        {
                            break;
                        }
                        output = output + new String(tmp, 0, i);
                        String[] m_OutputLines = output.split("\n");                    
                        if ( i < 1024 )
                        {
                            p_ConsoleLog.addAll(Arrays.asList(m_OutputLines));
                            output = "";
                        }
                        else
                        {                        
                            for ( int m_nPos = 0; m_nPos < m_OutputLines.length - 1; m_nPos ++)
                            {
                                p_ConsoleLog.add(m_OutputLines[m_nPos]);
                            }
                            output = m_OutputLines[m_OutputLines.length - 1];
                        }
                    }
                }
                if(m_RExecChannel.isClosed())
                {
                    if (m_RExecChannel.getExitStatus() != 0)
                    {
                        p_ConsoleLog.add("Command exit with <" + m_RExecChannel.getExitStatus() + ">");
                    }
                    break;
                }
                try{Thread.sleep(1000);}catch(InterruptedException ee){}
            }
            int nReturnStatus = m_RExecChannel.getExitStatus();
            
            
            m_RExecChannel.disconnect();
            
            return nReturnStatus;
    }
    
    public static void closeSSHSessionHandle(Session p_RExecSession) throws JSchException
    {
        p_RExecSession.disconnect();
    }
}

