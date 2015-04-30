/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jifi.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.swing.JTree;
import jifi.algorithm.parser.Parser;
import jifi.algorithm.procedure.Function;
import jifi.gui.GUI;
import jifi.gui.panels.RobotEditorPanel;
import jifi.gui.panels.robot.RobotControlPanel;
import jifi.robot.Robot;
import jifi.robot.device.Device;

/**
 *
 * @author antunes
 */
public class Project {

    private static final String tmpdir = System.getProperty("java.io.tmpdir");
    public static final String FILE_EXTENSION = "proj";

    public static void main(String[] args) {
//        Project a = new Project("teste");
//        a.save("teste.zip");
        Project a = new Project();
        a.robotToFile(new Robot());

    }
    private ArrayList<Function> functions;

    public Project() {
        functions = new ArrayList<>();
    }

    public Project(String name, ArrayList<Function> functions) {
        functions = new ArrayList<>();
        this.functions.addAll(functions);
    }

    public ArrayList<Function> getFunctions() {
        return functions;
    }

    public void setJTree(JTree tree) {
        tree.setEditable(true);

//        DefaultMutableTreeNode root = tree.getModel().getRoot();
    }

    public boolean save(String path) {
        boolean result = false;
        ZipOutputStream zip;
        FileOutputStream fileWriter;

        try {
//            System.out.println("Program Start zipping");

            /*
             * create the output stream to zip file result
             */
            fileWriter = new FileOutputStream(path);
            zip = new ZipOutputStream(fileWriter);
            /*
             * add the folder to the zip
             */
            addFolderToZip("", "functions", zip);

            for (Function f : functions) {
                addFileToZip("functions", functionToFile(f), zip, false);
            }

            addFolderToZip("", "robot", zip);

            addFileToZip("robot", robotToFile(RobotControlPanel.getRobot()), zip, false);

            addFolderToZip("", "environment", zip);

            {
                File file = null;
                try {
                    //file = new File("environment.env");
                    file = new File(tmpdir, "environment.env");

                    GUI.getInstance().getSimulationPanel().getEnv().saveFile(file);

                    addFileToZip("environment", file, zip, false);

                    file.delete();

                } catch (Exception e) {
                    //do stuff with exception
                    e.printStackTrace();
                }
            }

            /*
             * close the zip objects
             */
            zip.flush();
            zip.close();

            result = true;
//            System.out.println("Given files are successfully zipped");
        } catch (Exception e) {
            System.out.println("Some Errors happned during the zip process");
            e.printStackTrace();
        }

        return result;
    }

    private File robotToFile(Robot r) {
        try {
            List<List<Object>> devices = new ArrayList<>();
            for (Device d : r.getDevices()) {
                System.out.println(d);
                List<Object> descriptionData = d.getDescriptionData();
                descriptionData.add(0, d.getClassID());
                devices.add(descriptionData);
            }
            File file = new File(tmpdir, "robot.rob");
            FileOutputStream fout = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(devices);
            oos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private File functionToFile(Function f) {
        File file = null;
        try {
            String str = Parser.encode(f);
//            file = new File(f.getName() + ".func");
            file = new File(tmpdir, f.getName() + ".func");

            FileWriter fw = new FileWriter(file);
            fw.write(str);
            fw.close();

        } catch (Exception e) {
            //do stuff with exception
            e.printStackTrace();
        }
        return file;
    }

    /*
     * recursively add files to the zip files
     */
    private void addFileToZip(String path, File file, ZipOutputStream zip, boolean flag) throws Exception {

        /*
         * if the folder is empty add empty folder to the Zip file
         */
        if (flag == true) {
            zip.putNextEntry(new ZipEntry(path + "/" + file.getName() + "/"));
        } else { /*
             * if the current name is directory, recursively traverse it
             * to get the files
             */

            if (file.isDirectory()) {
                /*
                 * if folder is not empty
                 */
                addFolderToZip(path, file.getPath(), zip);
            } else {
                /*
                 * write the file to the output
                 */
                byte[] buf = new byte[1024];
                int len;
                FileInputStream in = new FileInputStream(file.getPath());
                zip.putNextEntry(new ZipEntry(path + "/" + file.getName()));
                while ((len = in.read(buf)) > 0) {
                    /*
                     * Write the Result
                     */
                    zip.write(buf, 0, len);
                }

                file.delete();
//                if (file.delete()) {
////                    System.out.println(file.getName() + " is deleted!");
//                } else {
//                    System.out.println("Delete operation is failed: " + file);
//                }
            }
        }
    }

    /*
     * add folder to the zip file
     */
    private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) throws Exception {
        File folder = new File(srcFolder);

        /*
         * check the empty folder
         */
        if (folder.list() == null || folder.list().length == 0) {
//            System.out.println(folder.getName());
            addFileToZip(path, new File(srcFolder), zip, true);
        } else {
            /*
             * list the files in the folder
             */
            for (String fileName : folder.list()) {
                if (path.equals("")) {
                    addFileToZip(folder.getName(), new File(srcFolder + "/" + fileName), zip, false);
                } else {
                    addFileToZip(path + "/" + folder.getName(), new File(srcFolder + "/" + fileName), zip, false);
                }
            }
        }
    }

    public void importFile(String path) {
        importZip(path, functions);
    }

    private static void importZip(String path, Collection<Function> functions) {
        try {
            ZipFile zipFile = new ZipFile(path);

            Enumeration<? extends ZipEntry> entries = zipFile.entries();

//            System.out.println("open");
            InputStream streamFunc = null;
            InputStream streamRob = null;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();

//                System.out.println("*" + entry);
                InputStream stream = zipFile.getInputStream(entry);

                if (entry.getName().startsWith("robot/") && entry.getName().endsWith(".rob")) {
                    streamRob = stream;
                }

                if (entry.getName().startsWith("functions/") && entry.getName().endsWith(".func")) {
                    streamFunc = stream;
                }

                if (entry.getName().startsWith("environment/") && entry.getName().endsWith(".env")) {
//                    System.out.println("Convertendo: " + entry);
                    GUI.getInstance().getSimulationPanel().getEnv().loadFile(stream);
                }
            }

            if (streamRob != null) {
                List<List<Object>> robotDescription = null;
                ObjectInputStream objectInputStream = new ObjectInputStream(streamRob);
                robotDescription = (List<List<Object>>) objectInputStream.readObject();
                objectInputStream.close();
                RobotControlPanel.getRobot().removeAllDevices();
                for (List<Object> data : robotDescription) {
                    Object o = data.get(0);
                    int sid = -1;
                    if (o instanceof Byte){
                        sid = (byte) o;
                    } else if (o instanceof Integer){
                        sid = (int) o;
                    } else {
                        throw new RuntimeException("Arquivo com problema!");
                    }
                    Device d = null;
                    for (Class< ? extends Device> c : RobotControlPanel.getAvailableDevices()) {
                        int csid = 0;
                        try {
                            d = c.newInstance();
                            csid = d.getClassID();
                        } catch (Exception ex) {
                        }
                        if (csid == sid && d != null) {
                            data.remove(0);
                            d = d.createDevice(data);
                            RobotControlPanel.getRobot().add(d);
                            break;
                        }
                    }
                }
            } else {
                RobotControlPanel.buildDefaultRobot();
            }
            GUI.getInstance().getRobotEditorPanel().updateSidePanel();

            if (streamFunc != null) {
                Function function = Parser.decode(streamFunc);
                if (function != null) {
                    functions.add(function);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Project load(String path) {
        Project p = new Project();
        importZip(path, p.functions);

        return p;
    }
}
