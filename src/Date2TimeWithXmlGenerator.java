import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by xiangshuai on 16/1/23.
 */
public class Date2TimeWithXmlGenerator {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static Items generateItems(String[] args) {
        Items items = new Items();


        if (args.length == 0) {

            Date time = Calendar.getInstance().getTime();
            String readableDate = sdf.format(time);
            Item dateItem = new Item("", readableDate, true, "date", "", "date", "date", "", "");
            items.addItem(dateItem);
            long timestamp = time.getTime();
            Item timestampItem = new Item("", String.valueOf(timestamp), true, "timestamp", "", "timestamp", "timestamp", "", "");
            items.addItem(timestampItem);


        }


        return items;


    }

    public static void main(String[] args) throws IOException {
        Items ret = generateItems(args);
        System.out.println(ret);
    }

    public static List<Item> csvReader(String path) throws IOException {

        List<Item> items = new ArrayList<>();

        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String s = null;
        Stream<String> lines = bufferedReader.lines();
        lines.forEach(line -> {
            String[] split = line.split(",");
            String projectName = split[0];
            if (projectName.equals("服务")) {
                return;
            }
            String git = split[1];
            String developBranch = split[2];
            String hosts = split[3];
            Item project = new Item(projectName, hosts, true, projectName, "", projectName, hosts, "", "");

            items.add(project);
            String[] hostArray = hosts.split(";");
            if (hostArray.length > 1) {
                for (int i = 0; i < hostArray.length; i++) {
                    int flag = i + 1;
                    String host = hostArray[i];
                    String projectWithFlag = projectName + "-" + flag;
                    Item projectPerHost = new Item(projectWithFlag, host, true, projectWithFlag, "", projectWithFlag, host, "", "");
                    items.add(projectPerHost);

                }
            }


        });

        return items;
    }

    static Document document;

    static {
        DocumentBuilderFactory factory = DocumentBuilderFactory
                .newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        document = builder.newDocument();
    }

    /**
     * <item uid="desktop" arg="~/Desktop" valid="YES" autocomplete="Desktop" type="file">
     * <title>Desktop</title>
     * <subtitle>~/Desktop</subtitle>
     * <icon type="fileicon">~/Desktop</icon>
     * </item>
     */
    static class Item {
        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public String getArg() {
            return arg;
        }

        public void setArg(String arg) {
            this.arg = arg;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getAutocomplete() {
            return autocomplete;
        }

        public void setAutocomplete(String autocomplete) {
            this.autocomplete = autocomplete;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getIcon() {
            return icon;
        }

        public void setIcon(String icon) {
            this.icon = icon;
        }

        public String getIconType() {
            return iconType;
        }

        public void setIconType(String iconType) {
            this.iconType = iconType;
        }

        private String uid;
        private String arg;
        private boolean valid = true;
        private String autocomplete;
        private String type;
        private String title;
        private String subtitle;
        private String icon;
        private String iconType;


        public Item(String uid, String arg, boolean valid, String autocomplete, String type, String title, String subtitle, String icon, String iconType) {
            this.uid = uid;
            this.arg = arg;
            this.valid = valid;
            this.autocomplete = autocomplete;
            this.type = type;
            this.title = title;
            this.subtitle = subtitle;
            this.icon = icon;
            this.iconType = iconType;
        }

        public Element toElement() {

            Element item = document.createElement("item");
            item.setAttribute("uid", uid);
            item.setAttribute("arg", arg);
            item.setAttribute("valid", valid ? "yes" : "no");
            item.setAttribute("autocomplete", autocomplete);
            item.setAttribute("type", type);


            Element titleElement = document.createElement("title");
            titleElement.setTextContent(title);

            item.appendChild(titleElement);
            Element subtitleElement = document.createElement("subtitle");
            subtitleElement.setTextContent(subtitle);


            item.appendChild(subtitleElement);
            Element iconElement = document.createElement("icon");
            iconElement.setAttribute("type", iconType);
            iconElement.setTextContent(icon);
            item.appendChild(iconElement);
            return item;
        }


    }

    static class Items {

        private List<Item> itemList = new ArrayList<>();


        public void addItem(Item item) {
            itemList.add(item);
        }

        public String out(Document document) throws TransformerException {
            TransformerFactory tf = TransformerFactory.newInstance();

            Transformer transformer = null;
            try {
                transformer = tf.newTransformer();
            } catch (TransformerConfigurationException e) {
                e.printStackTrace();
            }
            DOMSource source = new DOMSource(document);
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            StringWriter stringWriter = new StringWriter();
            StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);
            return stringWriter.toString();


        }

        @Override
        public String toString() {

            Element items = document.createElement("items");
            document.appendChild(items);

            itemList.forEach(item -> items.appendChild(item.toElement()));

            try {
                return out(document);
            } catch (TransformerException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
