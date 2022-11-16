package sample.processing;



import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Imagine extends ImagineAbstract {
    public Imagine() {
        image = new BufferedImage(1,1,BufferedImage.TYPE_INT_RGB );
    }
    public Imagine(BufferedImage img) {
        image = img;
    }
    @Override
    public void afiseaza() {
        //are rolul de a afisa intr-un frame imaginea
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // seteaza titlul imaginii
                JFrame frame = new JFrame("Mirror");
                //oprire thread la inchidere
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                //setare imagine ca obiect de tip ImageIcon
                ImageIcon imgIcon = new ImageIcon(image);
                //setare eticheta
                JLabel label = new JLabel();
                label.setIcon(imgIcon);
                //adaugare imagine in cadru(frame) ci centreaza
                frame.getContentPane().add(label, BorderLayout.CENTER);
                frame.pack();
                //nu ne intereseaza sa fie pozitionata fata de ceva anume deci lasam null
                frame.setLocationRelativeTo(null);
                //facem cadrul vizibil
                frame.setVisible(true);
            }
        });
    }
}
