import java.io.*;
import java.util.ArrayList;

/**
 * [Add your documentation here]
 *
 * @author Yu Nie & Lesi He, Lab 03
 * @version 4/27/2020
 */
public class ChatFilter {
    ArrayList<String> badWords = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {

        try {
            File f = new File(badWordsFileName);
            BufferedReader br = new BufferedReader(new FileReader(f));
            while (true) {
                String s = br.readLine();
                if (s == null)
                    break;
                badWords.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String filter(String msg) {
        String[] filter = msg.split(" ");
        for (int j = 0; j < filter.length; j++) {
            for (int i = 0; i < badWords.size(); i++) {
                int chars = badWords.get(i).length();
                if (filter[j].equalsIgnoreCase(badWords.get(i))) {
                    filter[j] = "";
                    for (int n = 0; n < chars; n++) {
                        filter[j] += "*";
                    }
                } else if (filter[j].substring(0, filter[j].length() - 1).equalsIgnoreCase(badWords.get(i))) {
                    String mask = "";
                    for (int m = 0; m < chars; m++) {
                        mask += "*";
                    }
                    filter[j] = mask + filter[j].substring(chars);
                }

            }
        }

        msg = "";
        for (int i = 0; i < filter.length; i++) {
            msg += filter[i];
            if (i < filter.length - 1)
                msg += " ";
        }
        return msg;
    }
}
