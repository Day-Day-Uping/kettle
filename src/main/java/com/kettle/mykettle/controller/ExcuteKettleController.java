package com.kettle.mykettle.controller;

import com.kettle.mykettle.advice.BizException;
import com.kettle.mykettle.dto.DataSourceInfo;
import com.kettle.mykettle.service.DataSourceService;
import com.kettle.mykettle.utils.RSAUtil;
import com.sun.xml.internal.stream.buffer.sax.SAXBufferCreator;
import jdk.nashorn.internal.ir.CallNode;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.JndiUtil;
import org.pentaho.di.core.KettleClientEnvironment;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.RowMetaAndData;
import org.pentaho.di.core.database.CacheDatabaseMeta;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleMissingPluginsException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.lifecycle.KettleLifecycleListener;
import org.pentaho.di.core.lifecycle.LifecycleException;
import org.pentaho.di.core.logging.ConsoleLoggingEventListener;
import org.pentaho.di.core.logging.KettleLogStore;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.KettleLoggingEventListener;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.plugins.PluginFolder;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.core.util.EnvUtil;
import org.pentaho.di.core.vfs.configuration.KettleGenericFileSystemConfigBuilder;
import org.pentaho.di.job.Job;
import org.pentaho.di.job.JobMeta;
import org.pentaho.di.job.entries.job.JobEntryJob;
import org.pentaho.di.job.entries.trans.JobEntryTrans;
import org.pentaho.di.job.entry.JobEntryCopy;
import org.pentaho.di.repository.filerep.KettleFileRepository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.steps.infobrightoutput.KettleValueConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.channels.Channel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.kettle.mykettle.utils.RSAUtil.encrypt;

/**
 * @program: mykettle
 * @description:
 * @author: Mr.HuangDaDa
 * @create: 2022-06-28 16:21
 **/
@RestController
public class ExcuteKettleController extends DefaultHandler {

    @GetMapping(value = "/excuteKtr")
    public Map<String, String> excuteKtr() {
        StringBuilder portLinkError = new StringBuilder();
        StringBuilder portExcutrError = new StringBuilder();
        String pathHead = System.getProperty("user.dir");
        Map<String, String> map = portUp(pathHead + "\\port.properties", null, false);
//        String flagPort = map.get("flagPort");
        String excutePort = map.get("excutePort");
        String ip = map.get("ip");
//        String oldPort = flagPort;
        String[] excuteport = excutePort.split(",");
        //??????????????????
        long l = System.currentTimeMillis();
        String fileName = pathHead + "\\log\\log_" + l + ".txt";
        BufferedWriter out = null;
        Path path = null;
        try {
            File file=new File(fileName);
            if (!file.exists() && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            path = Paths.get(fileName);
            out = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //????????????
        for (int i = 0; i < excuteport.length; i++) {
            if (!isHostConnectable(ip, Integer.valueOf(excuteport[i]))) {
                portLinkError.append(excuteport[i] + "\n");
                continue;
            } else {
                String s = excuteStr(out, pathHead, null, excuteport[i]);
                portExcutrError.append(s);
//                oldPort = excuteport[i];
            }
        }

        try {
            out.write("?????????????????????\t\n"+portLinkError.toString()+"\t\n");
            out.write("?????????????????????\t\n"+portExcutrError.toString()+"\t\n");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Map m = new HashMap();
        m.put("?????????????????????", portLinkError.toString());
        m.put("?????????????????????", portExcutrError.toString());
        return m;
    }

    /**
     * @Description: ????????????????????????
     * @Param: [host, port]
     * @return: boolean
     * @Author: Mr.HuangDaDa
     * @Date: 2022/7/5
     */

    public static boolean isHostConnectable(String host, int port) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(host, port));
        } catch (IOException e) {
            return false;
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * ????????????????????????
     *
     * @param port
     */
    public static Map<String, String> portUp(String path, String port, boolean flag) {
        Map<String, String> map = new HashMap();
        Properties prop = new Properties();// ??????????????????
        FileInputStream fis = null;// ?????????????????????
        try {
            fis = new FileInputStream(path);
            prop.load(fis);// ???????????????????????????Properties?????????
            fis.close();// ?????????
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            throw new BizException(500, "??????????????????");
        } catch (IOException e) {
            e.printStackTrace();
            throw new BizException(500, "???????????????????????????Properties???????????????");
        } finally {
            try {
                fis.close();// ?????????
            } catch (IOException e) {
                throw new BizException(500, "?????????????????????");
            }
        }
        String flagPort = prop.getProperty("flagPort");
        String excutePort = prop.getProperty("excutePort");
        String ip = prop.getProperty("ip");
        map.put("flagPort", flagPort);
        map.put("excutePort", excutePort);
        map.put("ip", ip);
        if (flag) {
            //??????port
            prop.setProperty("flagPort", port);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path);
                prop.store(fos, "----update---\nexcutePort:Port to execute,Separated by commas\nip:DB2 Data source IP");
                fos.flush();
                fos.close();// ?????????
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                throw new BizException(500, "????????????kettle.properties??????");
            } catch (IOException e) {
                e.printStackTrace();
                throw new BizException(500, "???????????????????????????Properties???????????????");
            } finally {
                try {
                    fos.close();// ?????????
                } catch (IOException e) {
                    throw new BizException(500, "?????????????????????");
                }
            }
        }
        return map;
    }


    private static String excuteStr(BufferedWriter out, String rootPath, String oldPort, String newPort) {
        String errorPort = null;
        Map<String, List<String>> stringListMap = traverseFolder2(rootPath);
        List<String> ktr = stringListMap.get("ktr");
        List<String> kjb = stringListMap.get("kjb");
        if (ktr.size() <= 0) {
            throw new BizException(500, "??????????????????ktr??????");
        }
        if (kjb.size() <= 0) {
            throw new BizException(500, "??????????????????kjb??????");
        }
        for (String o : ktr) {
            operationFile(o, null, newPort);
//            operationFile(o, "<port>" + oldPort + "</port>", "<port>" + newPort + "</port>");
//            operationFile(o, "<attribute>" + oldPort + "</attribute>", "<attribute>" + newPort + "</attribute>");
        }
        for (String s : kjb) {
            try {
                KettleEnvironment.init();
                JobMeta jm = new JobMeta(s, null);
                Job job = new Job(null, jm);
                out.write("-----------[port]:"+newPort+"   [job_adr]:"+s+ "----------\r\n");
                KettleLogStore.getAppender().addLoggingEventListener(new KettleLoggingEventListener() {
                    @Override
                    public void eventAdded(KettleLoggingEvent kettleLoggingEvent) {
                        try {
                            out.write(kettleLoggingEvent.getMessage() + "  \r\n");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
                job.start();
                job.waitUntilFinished();
                if (job.getErrors() >= 1) {
                    errorPort = newPort;
                    continue;
                }
            } catch (KettleException e) {
                errorPort = newPort;
                throw new BizException(500, "????????????:[?????????:" + newPort + "]");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return errorPort;
    }

    /***
     * @Description: ??????????????????ktr/kjb????????????
     * @Param: [path]
     * @return: void
     * @Author: Mr.HuangDaDa
     * @Date: 2022/7/4
     */

    public static Map<String, List<String>> traverseFolder2(String path) {
        File file = new File(path);
        Map<String, List<String>> map = new HashMap();
        List ktrList = null;
        List kjbList = null;
        if (file.exists()) {
            File[] files = file.listFiles();
            if (null == files || files.length == 0) {
                throw new BizException(500, "???????????????");
            } else {
                ktrList = new ArrayList();
                kjbList = new ArrayList();
                for (File file2 : files) {
                    String absolutePath = file2.getAbsolutePath();
                    if (absolutePath.endsWith(".ktr")) {
                        ktrList.add(absolutePath);
                    } else if (absolutePath.endsWith(".kjb")) {
                        kjbList.add(absolutePath);
                    }
                }
            }
        } else {
            throw new BizException(500, "???????????????");
        }
        map.put("ktr", ktrList);
        map.put("kjb", kjbList);
        return map;
    }

    public static void operationFile(String path, String target, String newContent) {
        File file = null;//????????????file????????????????????????FileReader
        try {
            file = new File(path);
            InputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is, "utf-8"));
            String filename = file.getName();
            // tmpfile?????????????????????????????????????????????????????????????????????????????????
            File tmpfile = new File(file.getParentFile().getAbsolutePath()
                    + "\\" + filename + ".tmp");
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tmpfile), "utf-8"));
            boolean flag = false;
            boolean flag_db = false;
            int flag_attr = 0;
            String str = null;
            while (true) {
                str = reader.readLine();
                if (str == null) {
                    break;
                } else if (str.contains("<database>infodms</database>")) {
                    writer.write(str + "\n");
                    flag_db = true;
                    flag_attr = flag_attr + 1;
                } else if (str.contains("<port>") && flag_db) {
                    writer.write("<port>" + newContent + "</port>" + "\n");
                    flag = true;
                    flag_db = false;
                } else if (str.contains("<code>PORT_NUMBER</code>")) {
                    writer.write(str + "\n");
                    flag_attr = flag_attr + 1;
                } else if (str.contains("<attribute>") && flag_attr == 2) {
                    writer.write("<attribute>" + newContent + "</attribute>" + "\n");
                    flag = true;
                    flag_attr = 0;
                } else if (str.contains("</connection>")) {
                    writer.write(str + "\n");
                    flag_attr = 0;
                } else {
                    writer.write(str + "\n");
                }
            }
            is.close();
            writer.flush();
            writer.close();
            if (flag) {
                file.delete();
                tmpfile.renameTo(new File(file.getAbsolutePath()));
            } else
                tmpfile.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Autowired
//    private DataSourceService dataSourceService;
//    @Value("${spring.priKey}")
//    private String priKey;
//    @Value("${spring.pubKey}")
//    private String pubKey;
//
//    Log log = LogFactory.getLog(ExcuteKettleController.class);
//
//    @GetMapping(value = "/proPassword/{password}")
//    public String proPassword(@PathVariable String password) {
//        try {
//            return encrypt(password, pubKey);
//        } catch (Exception e) {
//            throw new BizException(500, "????????????????????????");
//        }
//
//    }
//
//    /***
//     * @Description: ??????ktr??????
//     * @Param: []
//     * @return: java.lang.String
//     * @Author: Mr.HuangDaDa
//     * @Date: 2022/6/30
//     */
//
//    @GetMapping(value = "/excuteKtr")
//    public String excuteKtr() {
//        //???????????????????????????????????????
//        List<DataSourceInfo> dataSource = dataSourceService.getDataSource();
//        if (dataSource.size() <= 0) {
//            throw new BizException(500, "???????????????????????????????????????????????????dm_data_source");
//        }
//        String pathHead = System.getProperty("user.dir");
//        String path = null;
//        //????????????????????????ktr?????????
//        List<String> ktrName = dataSourceService.getKtrName();
//        if (ktrName.size() <= 0) {
//            throw new BizException(500, "?????????ktr?????????????????????????????????dm_ktr_process");
//        }
//        // ??????ktr??????
//        for (int i = 0; i < ktrName.size(); i++) {
//            path = pathHead + "\\" + ktrName.get(i);
//            // ????????????????????????
//            for (DataSourceInfo dataSourceInfo : dataSource) {
//                excuteKtr(path, dataSourceInfo);
//            }
//        }
//        return "success";
//    }
//
//    /**
//     * @Description: ????????????????????????ktr??????
//     * @Param: [ktr???????????????, ???????????????]
//     * @return: void
//     * @Author: Mr.HuangDaDa
//     * @Date: 2022/6/28
//     */
//
//    public void excuteKtr(String path, DataSourceInfo dataSourceInfo) {
//        Log log = LogFactory.getLog(ExcuteKettleController.class);
//        try {
//            KettleEnvironment.init();
//            EnvUtil.environmentInit();
//            TransMeta transMeta = new TransMeta(path);
//            List<DatabaseMeta> dmlist = transMeta.getDatabases();
//            if (dmlist.size() > 0) {
//                for (DatabaseMeta dm : dmlist) {
//                    String connection_name = dm.getName() == null ? "" : dm.getName();
//                    log.info("????????????" + connection_name);
//                    //?????????????????????????????????????????????
//                    if (connection_name.endsWith(dataSourceInfo.getLinkName())) {
//                        dm.setHostname(dataSourceInfo.getHostName());
//                        dm.setDBName(dataSourceInfo.getDBName());
//                        dm.setDBPort(dataSourceInfo.getDBPort());
//                        dm.setUsername(dataSourceInfo.getUserName());
//                        //rsa????????????
//                        String password = RSAUtil.decrypt(dataSourceInfo.getPassWord(), priKey);
//                        dm.setPassword(password);
//                    }
//                }
//                Trans trans = new Trans(transMeta);
//                //?????????????????????????????????????????????
//                String variable = trans.getVariable("connectname.port");
//                log.info(variable);
//                //????????????
//                trans.prepareExecution(null);
//                //????????????
//                trans.startThreads();
//                //??????????????????
//                trans.waitUntilFinished();
//                if (trans.getErrors() > 0) {
//                    log.info("???????????????");
//                    throw new BizException(500, "????????????");
//                }
//            }
//        } catch (KettleXMLException e) {
//            log.info(e.getMessage());
//            throw new BizException(500, "????????????");
//        } catch (KettleMissingPluginsException e) {
//            log.info(e.getMessage());
//            throw new BizException(500, "????????????");
//        } catch (KettleException e) {
//            log.info(e.getMessage());
//            throw new BizException(500, "????????????");
//        } catch (Exception e) {
//            log.info(e.getMessage());
//            throw new BizException(500, "????????????");
//        }
//    }


}
