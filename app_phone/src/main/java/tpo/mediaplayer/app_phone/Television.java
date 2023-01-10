package tpo.mediaplayer.app_phone;

import com.google.android.material.slider.Slider;

import java.util.ArrayList;
import java.util.Arrays;

public class Television {

    public String ime;
    public String ip_naslov;
    public String server_name;
    public ArrayList<String> files = new ArrayList<>();
    public Slider slider;

    public String currentMovie = "";

    public Television(String ime, String ip_naslov) {
        this.ime = ime;
        this.ip_naslov = ip_naslov;
        this.files.add("Movies");
    }

    public String connect() {
        return "Povezava s " + this.ime + " je vzpostavljena.";
    }

    public String connectToServer(String imeServer){
        this.server_name = imeServer;
        return "Povezano s " + imeServer + ".";
    }

    public void addSlider(Slider slider) {
        this.slider = slider;
    }

    public boolean isDirectory(String choosen) {
        return !choosen.endsWith(".mkv");
    }

    public void cd(String choosen) {
        ArrayList<String> arr1 = new ArrayList(Arrays.asList("..", "Film1", "Film2", "Serija1"));
        ArrayList<String> arr2 = new ArrayList(Arrays.asList("..", "Film1.mkv"));
        ArrayList<String> arr3 = new ArrayList(Arrays.asList("..", "Film2.mkv"));
        ArrayList<String> arr4 = new ArrayList(Arrays.asList("..", "del1.mkv", "del2.mkv", "del3.mkv", "del4.mkv", "del5.mkv", "del6.mkv", "del7.mkv", "del8.mkv"));
        switch (choosen) {
            case "..":
                if (this.files.get(this.files.size()-1).equals("Serija1")) {
                    this.files = new ArrayList(Arrays.asList("Movies"));
                }
                else {
                    this.files = arr1;
                }
                break;
            case "Film1":
                this.files = arr2;
                break;
            case "Film2":
                this.files = arr3;
                break;
            case "Serija1":
                this.files = arr4;
                break;
            case "Movies":
                this.files = arr1;
        }
    }

    public void play() {
        System.out.println("Play");
    }

    public void pause() {
        System.out.println("pause");
    }

    public void stop() {
        System.out.println("stop");
    }

    public void volumeUp() {
        System.out.println("volumeUp");
    }

    public void volumeDown() {
        System.out.println("volumeDown");
    }

    public void subtitles() {
        System.out.println("subtitles");
    }

    public void options() {
        System.out.println("options");
    }

    public void updateSlider() {
        this.slider.setValue((float) 0.5);
    }

}
