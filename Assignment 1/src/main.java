public class Main {
    public static void main(String[] args) {
        MasterThread master = new MasterThread("dataset/vm1.txt");
        master.start();
    }
}
