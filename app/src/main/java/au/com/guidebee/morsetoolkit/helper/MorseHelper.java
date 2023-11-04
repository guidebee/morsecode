package au.com.guidebee.morsetoolkit.helper;

import java.util.ArrayList;
import java.util.HashMap;

import au.com.guidebee.morsetoolkit.ConfigInfo;


public class MorseHelper {

    public static HashMap<Character, String> morseCodeData = new HashMap<>();

    public static HashMap<String, Character> morseCodeReverseData = new HashMap<>();
    private static char[] allPunctuates = new char[]{
            '.', ',', '?', '\'', '!', '/', '(', ')', '&', ':', ';', '=', '+', '-', '_', '"', '$', '@'
    };

    static {
        morseCodeReverseData.put(".-", 'a');
        morseCodeReverseData.put("-...", 'b');
        morseCodeReverseData.put("-.-.", 'c');
        morseCodeReverseData.put("-..", 'd');
        morseCodeReverseData.put(".", 'e');
        morseCodeReverseData.put("..-.", 'f');
        morseCodeReverseData.put("--.", 'g');
        morseCodeReverseData.put("....", 'h');
        morseCodeReverseData.put("..", 'i');
        morseCodeReverseData.put(".---", 'j');
        morseCodeReverseData.put("-.-", 'k');
        morseCodeReverseData.put(".-..", 'l');
        morseCodeReverseData.put("--", 'm');
        morseCodeReverseData.put("-.", 'n');
        morseCodeReverseData.put("---", 'o');
        morseCodeReverseData.put(".--.", 'p');
        morseCodeReverseData.put("--.-", 'q');
        morseCodeReverseData.put(".-.", 'r');
        morseCodeReverseData.put("...", 's');
        morseCodeReverseData.put("-", 't');
        morseCodeReverseData.put("..-", 'u');
        morseCodeReverseData.put("...-", 'v');
        morseCodeReverseData.put(".--", 'w');
        morseCodeReverseData.put("-..-", 'x');
        morseCodeReverseData.put("-.--", 'y');
        morseCodeReverseData.put("--..", 'z');

        morseCodeReverseData.put("-----", '0');
        morseCodeReverseData.put(".----", '1');
        morseCodeReverseData.put("..---", '2');
        morseCodeReverseData.put("...--", '3');
        morseCodeReverseData.put("....-", '4');
        morseCodeReverseData.put(".....", '5');
        morseCodeReverseData.put("-....", '6');
        morseCodeReverseData.put("--...", '7');
        morseCodeReverseData.put("---..", '8');
        morseCodeReverseData.put("----.", '9');

        morseCodeReverseData.put(".-.-.-", '.');
        morseCodeReverseData.put("--..--", ',');
        morseCodeReverseData.put("..--..", '?');
        morseCodeReverseData.put(".----.", '\'');
        morseCodeReverseData.put("-.-.--", '!');
        morseCodeReverseData.put("-..-.", '/');
        morseCodeReverseData.put("-.--.", '(');
        morseCodeReverseData.put("-.--.-", ')');
        morseCodeReverseData.put(".-...", '&');
        morseCodeReverseData.put("---...", ':');
        morseCodeReverseData.put("-.-.-.", ';');
        morseCodeReverseData.put("-...-", '=');
        morseCodeReverseData.put(".-.-.", '+');
        morseCodeReverseData.put("-....-", '-');
        morseCodeReverseData.put("..--.-", '_');
        morseCodeReverseData.put(".-..-.", '\"');
        morseCodeReverseData.put("...-..-", '$');
        morseCodeReverseData.put(".--.-.", '@');

        morseCodeData.put('a', ".-");
        morseCodeData.put('b', "-...");
        morseCodeData.put('c', "-.-.");
        morseCodeData.put('d', "-..");
        morseCodeData.put('e', ".");
        morseCodeData.put('f', "..-.");
        morseCodeData.put('g', "--.");
        morseCodeData.put('h', "....");
        morseCodeData.put('i', "..");
        morseCodeData.put('j', ".---");
        morseCodeData.put('k', "-.-");
        morseCodeData.put('l', ".-..");
        morseCodeData.put('m', "--");
        morseCodeData.put('n', "-.");
        morseCodeData.put('o', "---");
        morseCodeData.put('p', ".--.");
        morseCodeData.put('q', "--.-");
        morseCodeData.put('r', ".-.");
        morseCodeData.put('s', "...");
        morseCodeData.put('t', "-");
        morseCodeData.put('u', "..-");
        morseCodeData.put('v', "...-");
        morseCodeData.put('w', ".--");
        morseCodeData.put('x', "-..-");
        morseCodeData.put('y', "-.--");
        morseCodeData.put('z', "--..");

        morseCodeData.put('0', "-----");
        morseCodeData.put('1', ".----");
        morseCodeData.put('2', "..---");
        morseCodeData.put('3', "...--");
        morseCodeData.put('4', "....-");
        morseCodeData.put('5', ".....");
        morseCodeData.put('6', "-....");
        morseCodeData.put('7', "--...");
        morseCodeData.put('8', "---..");
        morseCodeData.put('9', "----.");


        morseCodeData.put('.', ".-.-.-");
        morseCodeData.put(',', "--..--");
        morseCodeData.put('?', "..--..");
        morseCodeData.put('\'', ".----.");
        morseCodeData.put('!', "-.-.--");
        morseCodeData.put('/', "-..-.");
        morseCodeData.put('(', "-.--.");
        morseCodeData.put(')', "-.--.-");
        morseCodeData.put('&', ".-...");
        morseCodeData.put(':', "---...");
        morseCodeData.put(';', "-.-.-.");
        morseCodeData.put('=', "-...-");
        morseCodeData.put('+', ".-.-.");
        morseCodeData.put('-', "-....-");
        morseCodeData.put('_', "..--.-");
        morseCodeData.put('\"', ".-..-.");
        morseCodeData.put('$', "...-..-");
        morseCodeData.put('@', ".--.-.");


    }

    public static ArrayList<Character> initTestLetters(int type) {
        ArrayList<Character> allTestLetters = new ArrayList<>();
        if ((type & ConfigInfo.TYPE_LETTER_LETTER) > 0) {
            for (int i = 0; i < 26; i++) {
                allTestLetters.add((char) ('a' + i));
            }
        }
        if ((type & ConfigInfo.TYPE_LETTER_NUMBER) > 0) {
            for (int i = 0; i < 10; i++) {
                allTestLetters.add((char) ('0' + i));
            }
        }
        if ((type & ConfigInfo.TYPE_LETTER_PUNCTUATION) > 0) {

            for (int i = 0; i < allPunctuates.length; i++) {
                allTestLetters.add(allPunctuates[i]);
            }
        }
        return allTestLetters;
    }
}
