package tpo.mediaplayer.app_tv;


import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

abstract class ConnectionSMB_SFTP {
    abstract void connect();

    abstract boolean test_con();

    abstract List <String> ls();

    abstract boolean cd(String directory);

    abstract public String pwd();

    abstract public boolean isDirectory(String file_name);

    //abstract public String[] fileInfo(String file_name);

    abstract public void disconect();

    abstract public InputStream get_stream(String file_name);



}






class Connection_SFTP extends ConnectionSMB_SFTP {

    static String username;
    static String password;
    static String privateKey;
    static String remote_host;

    JSch jsch;
    static int remote_port = 22;

    Session session;
    Channel channel;
    ChannelSftp channelSftp;


    public Connection_SFTP(String host, String username, String password, String privateKey){
        this.username = username;
        this.password = password;
        this.privateKey = privateKey;
        this.remote_host = host;

    }

    @Override
    public void connect(){
        try {
            jsch = new JSch();
            //jsch.setKnownHosts("/home/mkyong/.ssh/known_hosts");
            session = jsch.getSession(username, remote_host, remote_port);

            session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect();

            channel = session.openChannel("sftp");

            channelSftp = (ChannelSftp)channel;
            channelSftp.connect();
            channelSftp.cd("/");//go to root of file system

            System.out.println("Connected");
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean test_con(){
        return channel.isConnected();
    }

    @Override
    public List <String> ls() {
        Vector filelist;
        List <String> out = new ArrayList<String>();
        try {
            filelist = channelSftp.ls(".");
            for (int i = 0; i < filelist.size(); i++) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) filelist.get(i);
                out.add(entry.getFilename());
            }
            return out;
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean cd(String directory) {
        if (directory.isEmpty()){
            return false;
        }

        SftpATTRS attrs = null;

        if (directory.charAt(0) == '/'){//if string is absolute path

            try {
                String dir = channelSftp.pwd() + "/";
                try {
                    channelSftp.cd("/");
                    attrs = channelSftp.stat(directory.substring(1) + "/");
                    if (attrs != null) {
                        channelSftp.cd(directory.substring(1) + "/");
                        return true;
                    }
                } catch (Exception e) {
                    System.out.println(directory.substring(1) + " is not a directory");
                    channelSftp.cd(dir);
                }
            } catch (SftpException e) {
                e.printStackTrace();
            }
        }else{//if string is abstract path
            try {
                String dir = channelSftp.pwd();
                try {
                    attrs = channelSftp.stat(dir + "/" + directory + "/");
                } catch (Exception e) {
                    System.out.println(dir + "/" + directory + "/" + " not found");
                }
                if (attrs != null) {
                    dir = dir + "/" + directory + "/";
                    channelSftp.cd(dir);
                    return true;
                }
            } catch (SftpException e) {
                e.printStackTrace();
            }
        }


        return false;
    }

    @Override
    public String pwd(){
        try {
            return channelSftp.pwd();
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean isDirectory(String file_name ){
        SftpATTRS attrs = null;
        try {
            attrs = channelSftp.stat(channelSftp.pwd() + "/" + file_name);
            if(attrs != null && attrs.isDir()){
                return true;
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return false;
    }
/*
    public String[] fileInfo(String file_name){
        SftpATTRS attrs = null;
        try {
            attrs = channelSftp.stat(channelSftp.pwd() + "/" + file_name);
            if(attrs != null && !attrs.isDir()){
                return attrs.getExtended();
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return null;
    }
*/

    @Override
    public void disconect(){
        channelSftp.disconnect();
        channel.disconnect();
        session.disconnect();
    }

    @Override
    public InputStream get_stream(String file_name){
        try {
            SftpATTRS attrs = null;
            try {
                attrs = channelSftp.stat(channelSftp.pwd() + "/" + file_name);
            } catch (Exception e) {
                System.out.println(channelSftp.pwd() + "/" + file_name + " not found");
            }
            if (attrs != null) {
                System.out.println(channelSftp.pwd() + "/" + file_name + "     " + attrs);
                InputStream s = channelSftp.get(channelSftp.pwd() + "/" + file_name);
                return s;
            }
        } catch (SftpException e) {
            e.printStackTrace();
        }
        return null;
    }

}
/*
con = new Connection_SFTP("","", "","");

        con.connect();

        System.out.println(con.cd("./mnt/md0"));
        System.out.println(con.cd("/mnt/md0/Media"));
        System.out.println(con.pwd());
        StringBuilder a = new StringBuilder();
        System.out.println(con.cd("Filmi"));
        System.out.println(con.pwd());
        System.out.println(con.cd(".."));
        System.out.println(con.isDirectory("Screen.mp4"));
        System.out.println(con.pwd());
        List<String> ls = con.ls();
        for (int i = 0; i < ls.size(); i++) {
            a.append(ls.get(i)).append("\n");
        }

        System.out.println(a.toString());
        System.out.println(con.get_stream("Screen.mp4"));



        con.disconect();
 */