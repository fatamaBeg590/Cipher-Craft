import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;

public class CipherCraft
 {

    
    private static SecretKey secretKey = new SecretKeySpec("1234567890abcdef".getBytes(), "AES");  

    
    private static String textToBinary(String text)
     {
        StringBuilder binary = new StringBuilder();
        for (char c : text.toCharArray())
         {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    
    private static String binaryToText(String binary)
     {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 8)
         {
            String byteStr = binary.substring(i, i + 8);
            text.append((char) Integer.parseInt(byteStr, 2));
        }
        return text.toString();
    }

    
    public static String encrypt(String message) throws Exception 
    {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes); 
    }

    
    public static String decrypt(String encryptedMessage) throws Exception 
    {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedBytes = Base64.getDecoder().decode(encryptedMessage); 
        byte[] decryptedBytes = cipher.doFinal(decodedBytes);
        return new String(decryptedBytes);
    }

    
    public static void embedData(String imagePath, String message, String outputImagePath, boolean encrypt) throws Exception 
    {
        BufferedImage image = ImageIO.read(new File(imagePath));
        String data = encrypt ? encrypt(message) : message;
        String binaryData = textToBinary(data) + "1111111111111110"; 

        int dataIndex = 0;
        outer:
        for (int y = 0; y < image.getHeight(); y++)
         {
            for (int x = 0; x < image.getWidth(); x++)
             {
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = pixel & 0xFF;

                if (dataIndex < binaryData.length()) {
                    blue = (blue & 0xFE) | (binaryData.charAt(dataIndex++) - '0'); 
                }

                int newPixel = (red << 16) | (green << 8) | blue;
                image.setRGB(x, y, newPixel);

                if (dataIndex >= binaryData.length()) 
                {
                    break outer;
                }
            }
        }
        ImageIO.write(image, "png", new File(outputImagePath));
        System.out.println("Data embedded successfully into " + outputImagePath);
    }

    
    public static String extractData(String imagePath, boolean decrypt) throws Exception 
    {
        BufferedImage image = ImageIO.read(new File(imagePath));
        StringBuilder binaryData = new StringBuilder();

        outer:
        for (int y = 0; y < image.getHeight(); y++)
         {
            for (int x = 0; x < image.getWidth(); x++)
             {
                int pixel = image.getRGB(x, y);
                int blue = pixel & 0xFF;
                binaryData.append(blue & 1); 

                
                if (binaryData.length() >= 16 && binaryData.substring(binaryData.length() - 16).equals("1111111111111110")) {
                    break outer;
                }
            }
        }

        String messageBinary = binaryData.substring(0, binaryData.length() - 16); 
        String message = binaryToText(messageBinary);
        return decrypt ? decrypt(message) : message;
    }

    
    public static void main(String[] args) 
    {
        
        SwingUtilities.invokeLater(new Runnable()
         {
            public void run() 
            {
                try 
                {
                    JFrame frame = new JFrame("CipherCraft App");
                    frame.setSize(600, 600);
                    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    
                    JPanel imagePanel = new JPanel(new GridLayout(2, 2));
                    JLabel imageLabel = new JLabel("Original Image");
                    JLabel stegoLabel = new JLabel("Stego Image");

                    JLabel originalImageLabel = new JLabel();
                    JLabel stegoImageLabel = new JLabel();

                    JButton browseOriginalButton = new JButton("Browse Cover Image");
                    JButton browseStegoButton = new JButton("Browse Stego Image");

                    browseOriginalButton.addActionListener(new ActionListener() 
                    {
                        
                        public void actionPerformed(ActionEvent e)
                         {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setDialogTitle("Select Cover Image");
                            fileChooser.setAcceptAllFileFilterUsed(false);
                            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG & JPEG Images", "png", "jpg", "jpeg"));

                            int returnValue = fileChooser.showOpenDialog(frame);
                            if (returnValue == JFileChooser.APPROVE_OPTION) {
                                File selectedFile = fileChooser.getSelectedFile();
                                try
                                 {
                                    BufferedImage image = ImageIO.read(selectedFile);
                                    originalImageLabel.setIcon(new ImageIcon(image.getScaledInstance(400, 400, Image.SCALE_SMOOTH)));
                                } 
                                catch (IOException ex) 
                                {
                                    JOptionPane.showMessageDialog(frame, "Error loading image: " + ex.getMessage());
                                }
                            }
                        }
                    });

                    browseStegoButton.addActionListener(new ActionListener()
                     {
                        
                        public void actionPerformed(ActionEvent e) {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setDialogTitle("Select Stego Image");
                            fileChooser.setAcceptAllFileFilterUsed(false);
                            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG & JPEG Images", "png", "jpg", "jpeg"));

                            int returnValue = fileChooser.showOpenDialog(frame);
                            if (returnValue == JFileChooser.APPROVE_OPTION)
                             {
                                File selectedFile = fileChooser.getSelectedFile();
                                try 
                                {
                                    BufferedImage image = ImageIO.read(selectedFile);
                                    stegoImageLabel.setIcon(new ImageIcon(image.getScaledInstance(300, 300, Image.SCALE_SMOOTH)));
                                } 
                                catch (IOException ex) 
                                {
                                    JOptionPane.showMessageDialog(frame, "Error loading image: " + ex.getMessage());
                                }
                            }
                        }
                    });

                    
                    JButton embedButton = new JButton("Embed Data");
                    embedButton.addActionListener(new ActionListener() 
                    {
                        
                        public void actionPerformed(ActionEvent e)
                         {
                            try
                             {
                                
                                String imagePath = JOptionPane.showInputDialog("Enter the path of the cover image:");
                                String message = JOptionPane.showInputDialog("Enter the secret message to embed:");
                                int encryptChoice = JOptionPane.showConfirmDialog(frame, "Encrypt the message?", "Encryption", JOptionPane.YES_NO_OPTION);
                                boolean encrypt = encryptChoice == JOptionPane.YES_OPTION;
                                String outputImagePath = JOptionPane.showInputDialog("Enter the output path for the stego-image:");

                                embedData(imagePath, message, outputImagePath, encrypt);

                                
                                BufferedImage stegoImage = ImageIO.read(new File(outputImagePath));
                                stegoImageLabel.setIcon(new ImageIcon(stegoImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH)));
                            } 
                    catch (Exception ex)
                             {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage());
                            }
                        }
                    });

            
                    JButton extractButton = new JButton("Extract Data");
                    extractButton.addActionListener(new ActionListener()
                    
                    {
                    
                        public void actionPerformed(ActionEvent e)
                         {
                            try
        {
                                
                                String stegoImagePath = JOptionPane.showInputDialog("Enter the path of the stego-image:");
                                int decryptChoice = JOptionPane.showConfirmDialog(frame, "Is the message encrypted?", "Decryption", JOptionPane.YES_NO_OPTION);
                                boolean decrypt = decryptChoice == JOptionPane.YES_OPTION;

                                String message = extractData(stegoImagePath, decrypt);

                                
                                BufferedImage stegoImage = ImageIO.read(new File(stegoImagePath));

                                
                                Graphics2D g2d = stegoImage.createGraphics();
                                g2d.setColor(Color.RED);
                                g2d.setFont(new Font("Arial", Font.BOLD, 150));
                                g2d.drawString(message, 10, stegoImage.getHeight() - 10); 

                                
                                stegoImageLabel.setIcon(new ImageIcon(stegoImage.getScaledInstance(300, 300, Image.SCALE_SMOOTH)));

                                
                                g2d.dispose();
                            } 
                            
        catch (Exception ex) 
                            {
          ex.printStackTrace();
          
          JOptionPane.showMessageDialog(frame, "An error occurred: " + ex.getMessage());
                            }
                        }
                    });

            
                    frame.setLayout(new BorderLayout());
                    JPanel controlPanel = new JPanel();
                    controlPanel.setLayout(new GridLayout(2, 2));
                    controlPanel.add(browseOriginalButton);
                    controlPanel.add(browseStegoButton);
                    controlPanel.add(embedButton);
                    controlPanel.add(extractButton);
                    frame.add(controlPanel, BorderLayout.SOUTH);
                    frame.add(imagePanel, BorderLayout.CENTER);
                    imagePanel.add(originalImageLabel);
                    imagePanel.add(stegoImageLabel);

        
                    frame.setVisible(true);

                } 
                catch (Exception e)
                 {
                    e.printStackTrace();
                }
            }
        });
    }
}