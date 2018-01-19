public class Main {

    public static void main(String[] args) {
        for (int i = 0; i < 4; i++)
            new BasicThread().start();
    }
}

class BasicThread extends Thread {

    public void run() {
        new SingleLaneAVSim("Straight road AV ", 0.1, "00:20:00", new double[]{0.15}).simulate();
    }


}