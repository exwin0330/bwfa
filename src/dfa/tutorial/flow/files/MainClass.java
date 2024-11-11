package dfa.tutorial.flow.files;

public class MainClass {
    public static void main(String[] args) {
        System.out.println(message3());
        HelperClass helper = new HelperClass();
        helper.printMessage();  // HelperClassのメソッドを呼び出し
    }

    public static String message3() {
        return message2();
    }

    public static String message2() {
        return "MainClass is starting.";
    }
}
